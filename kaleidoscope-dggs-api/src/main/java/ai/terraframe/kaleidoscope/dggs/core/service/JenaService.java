/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.config.AppProperties;
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;

@Service
public class JenaService
{
  public static final String OBJECT_PRFIX     = "https://localhost:4200/lpg/graph_801104/0/rdfs#";

  public static final String PREFIXES         = """
      	PREFIX lpgs: <https://localhost:4200/lpg/rdfs#>
      	PREFIX lpg: <https://localhost:4200/lpg#>
      	PREFIX lpgv: <https://localhost:4200/lpg/graph_801104/0#>
      	PREFIX lpgvs: <https://localhost:4200/lpg/graph_801104/0/rdfs#>
      	PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      	PREFIX geo: <http://www.opengis.net/ont/geosparql#>
      	PREFIX spatialF: <http://jena.apache.org/function/spatial#>
      """;

  public static String       FULL_TEXT_LOOKUP = PREFIXES + """
      		PREFIX   ex: <https://localhost:4200/lpg/graph_801104/0/rdfs#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX text: <http://jena.apache.org/text#>
            PREFIX lpgs: <https://localhost:4200/lpg/rdfs#>

            SELECT ?uri ?type ?code ?label ?wkt
            FROM <https://localhost:4200/lpg/graph_801104/0#>
            WHERE {{
              (?uri ?score) text:query (rdfs:label ?query) .
              ?uri lpgs:GeoObject-code ?code .
              ?uri rdfs:label ?label .
              ?uri a ?type .
              OPTIONAL {
                  ?uri geo:hasGeometry ?g .
                  ?g geo:asWKT ?wkt .
              }
            }}
            ORDER BY DESC(?score)
      """;

  @Autowired
  private AppProperties      properties;

  public List<Location> query(String statement)
  {
    return this.query(statement, 0, 1000);
  }

  public List<Location> query(String statement, int offset, int limit)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create() //
        .destination(properties.getJenaUrl());

    String sparql = new String(statement);

    // Remove existing LIMIT and OFFSET clauses (case-insensitive)
    sparql = sparql.replaceAll("(?i)LIMIT\\s+\\d+", "");
    sparql = sparql.replaceAll("(?i)OFFSET\\s+\\d+", "");

    // Append ORDER BY, which must come before the limit
    if (!sparql.toUpperCase().contains("ORDER BY"))
    {
      sparql += " ORDER BY ASC(?label)";
    }

    // Append new LIMIT and OFFSET
    sparql += " LIMIT " + limit + " OFFSET " + offset;

    try (RDFConnection conn = builder.build())
    {
      LinkedList<Location> results = new LinkedList<>();

      System.out.println("JenaService.query - EXECUTING QUERY");
      System.out.println(sparql);

      conn.querySelect(sparql, (qs) -> {
        String uri = qs.getResource("uri").getURI();
        String type = readString(qs, "type");
        String code = readString(qs, "code");
        String label = readString(qs, "label");
        String wkt = readString(qs, "wkt");

        WKTReader reader = WKTReader.extract(wkt);
        Geometry geometry = reader.getGeometry();

        results.add(new Location(uri, type, code, label, geometry));

      });

      return results;
    }
  }

  private String readString(QuerySolution qs, String name)
  {
    if (qs.contains(name))
    {
      RDFNode node = qs.get(name);

      if (node.isLiteral())
      {
        return node.asLiteral().getString();
      }
      else
      {
        return node.asResource().getURI();
      }
    }

    return "";
  }

  public Long getCount(String statement)
  {
    Map<String, Long> holder = new HashMap<>();

    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create() //
        .destination(properties.getJenaUrl());

    StringBuilder sparql = new StringBuilder();

    int selectIndex = statement.toUpperCase().indexOf("SELECT");
    int fromIndex = statement.toUpperCase().indexOf("FROM");
    int groupByIndex = statement.toUpperCase().indexOf("GROUP BY");

    // Prefix section
    sparql.append(statement.substring(0, selectIndex));
    sparql.append("SELECT (COUNT(distinct ?uri) AS ?count)\n");

    if (groupByIndex != -1)
    {
      sparql.append(statement.substring(fromIndex, groupByIndex));
    }
    else
    {
      sparql.append(statement.substring(fromIndex));
    }

    try (RDFConnection conn = builder.build())
    {
      conn.querySelect(sparql.toString(), (qs) -> {
        holder.put("count", qs.getLiteral("count").getLong());
      });

      return holder.getOrDefault("count", 0L);
    }
  }

  public LocationPage fullTextLookup(String query, int offset, int limit)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create().destination(properties.getJenaUrl());

    List<Location> results = new ArrayList<Location>();

    try (RDFConnection conn = builder.build())
    {
      var sparql = FULL_TEXT_LOOKUP;
      sparql += " LIMIT " + limit + " OFFSET " + offset;

      // Use ParameterizedSparqlString to inject the URI safely
      ParameterizedSparqlString pss = new ParameterizedSparqlString();
      pss.setCommandText(sparql);

      pss.setLiteral("query", query);

      try (QueryExecution qe = conn.query(pss.asQuery()))
      {
        ResultSet rs = qe.execSelect();

        while (rs.hasNext())
        {
          QuerySolution qs = rs.next();

          String uri = qs.getResource("uri").getURI();
          String type = qs.getResource("type").getURI();
          String code = qs.getLiteral("code").getString();
          String label = qs.getLiteral("label").getString();
          String wkt = qs.getLiteral("wkt").getString();

          WKTReader reader = WKTReader.extract(wkt);
          Geometry geometry = reader.getGeometry();

          results.add(new Location(uri, type, code, label, geometry));
        }
      }
    }

    LocationPage page = new LocationPage();
    page.setLocations(results);
    page.setCount(results.size());
    page.setLimit(100);
    page.setOffset(0);

    return page;
  }

}