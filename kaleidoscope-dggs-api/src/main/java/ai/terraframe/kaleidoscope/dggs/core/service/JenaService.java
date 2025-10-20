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

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTWriter;
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
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;

@Service
public class JenaService
{
  public static final String GRAPH            = "http://terraframe.ai/g1";

  public static final String NAMESPACE        = "http://terraframe.ai";

  public static final String PREFIXES         = """
        PREFIX ai: <http://terraframe.ai#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX text: <http://jena.apache.org/text#>
        PREFIX geo: <http://www.opengis.net/ont/geosparql#>
        PREFIX geof: <http://www.opengis.net/def/function/geosparql/>

      """;

  public static String       FULL_TEXT_LOOKUP = PREFIXES + """
            SELECT ?uri ?code ?label ?type ?wkt
            FROM <http://terraframe.ai/g1>
            WHERE {
              (?uri ?score) text:query (rdfs:label ?query) .
              ?uri ai:GeoObject-code ?code .
              ?uri rdfs:label ?label .
              ?uri a ?type .
              OPTIONAL {
                  ?uri geo:hasGeometry ?g .
                  ?g geo:asWKT ?wkt .
              }
            }
            ORDER BY DESC(?score)
            LIMIT 100
      """;

  public static String       URI_LOOKUP       = PREFIXES + """
            SELECT ?code ?label ?type ?wkt
            FROM <http://terraframe.ai/g1>
            WHERE {
              ?uri ai:GeoObject-code ?code .
              ?uri rdfs:label ?label .
              ?uri a ?type .
              OPTIONAL {
                  ?uri geo:hasGeometry ?g .
                  ?g geo:asWKT ?wkt .
              }
            }
            LIMIT 1
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

      conn.querySelect(sparql, (qs) -> {
        String uri = qs.getResource("uri").getURI();
        String code = readString(qs, "code");
        String label = readString(qs, "label");
        String type = readString(qs, "type");
        String wkt = readString(qs, "wkt");

        WKTReader reader = WKTReader.extract(wkt);
        Geometry geometry = reader.getGeometry();

        results.add(new Location(uri, code, type, label, geometry));

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

  public LocationPage fullTextLookup(String locationName)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create().destination(properties.getJenaUrl());

    List<Location> results = new ArrayList<Location>();

    try (RDFConnection conn = builder.build())
    {
      // Use ParameterizedSparqlString to inject the URI safely
      ParameterizedSparqlString pss = new ParameterizedSparqlString();
      pss.setCommandText(FULL_TEXT_LOOKUP);

      pss.setLiteral("query", locationName);

      try (QueryExecution qe = conn.query(pss.asQuery()))
      {
        ResultSet rs = qe.execSelect();

        while (rs.hasNext())
        {
          QuerySolution qs = rs.next();

          String uri = qs.getResource("uri").getURI();
          String code = qs.getLiteral("code").getString();
          String wkt = qs.getLiteral("wkt").getString();
          String label = qs.getLiteral("label").getString();
          String type = readString(qs, "type");

          WKTReader reader = WKTReader.extract(wkt);
          Geometry geometry = reader.getGeometry();

          results.add(new Location(uri, code, type, label, geometry));
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

  public Location getLocation(String uri)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create().destination(properties.getJenaUrl());

    try (RDFConnection conn = builder.build())
    {
      // Use ParameterizedSparqlString to inject the URI safely
      ParameterizedSparqlString pss = new ParameterizedSparqlString();
      pss.setCommandText(URI_LOOKUP);
      pss.setIri("uri", uri);

      try (QueryExecution qe = conn.query(pss.asQuery()))
      {
        ResultSet rs = qe.execSelect();

        while (rs.hasNext())
        {
          QuerySolution qs = rs.next();

          String code = qs.getLiteral("code").getString();
          String wkt = qs.getLiteral("wkt").getString();
          String label = qs.getLiteral("label").getString();
          String type = readString(qs, "type");

          WKTReader reader = WKTReader.extract(wkt);
          Geometry geometry = reader.getGeometry();

          return new Location(uri, code, type, label, geometry);
        }
      }
    }

    throw new GenericRestException("Unable to find an object with the URI [" + uri + "]");
  }

  public List<Location> getWithinGeometry(Geometry envelope, String... types)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create().destination(properties.getJenaUrl());

    List<Location> results = new ArrayList<Location>();

    WKTWriter.write(new GeometryWrapper(envelope, "http://www.opengis.net/ont/geosparql#wktLiteral"));

    try (RDFConnection conn = builder.build())
    {
      StringBuilder statement = new StringBuilder();
      statement.append(PREFIXES + """
          SELECT ?uri ?code ?label ?type ?wkt
          FROM <http://terraframe.ai/g1>
          WHERE {
            ?geom geo:asWKT ?wkt .
            ?uri geo:hasGeometry ?geom .
            ?uri a ?type .
            ?uri rdfs:label ?label .
            ?uri ai:GeoObject-code ?code .
            FILTER (geof:sfContains(?envelope, ?wkt))""");

      if (types.length > 0)
      {
        statement.append("\n FILTER (?type IN (");

        for (int i = 0; i < types.length; i++)
        {
          if (i != 0)
          {
            statement.append(", ");
          }

          statement.append("?type" + i);
        }
        statement.append("))");
      }

      statement.append("} LIMIT 100");

      // Use ParameterizedSparqlString to inject the URI safely
      ParameterizedSparqlString pss = new ParameterizedSparqlString();
      pss.setCommandText(statement.toString());
      pss.setLiteral("envelope", envelope.toText(), new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral"));

      for (int i = 0; i < types.length; i++)
      {
        pss.setIri("type" + i, types[i]);
      }

      try (QueryExecution qe = conn.query(pss.asQuery()))
      {
        ResultSet rs = qe.execSelect();

        while (rs.hasNext())
        {
          QuerySolution qs = rs.next();

          String uri = qs.getResource("uri").getURI();
          String code = qs.getLiteral("code").getString();
          String wkt = qs.getLiteral("wkt").getString();
          String label = qs.getLiteral("label").getString();
          String type = readString(qs, "type");

          WKTReader reader = WKTReader.extract(wkt);
          Geometry geometry = reader.getGeometry();

          results.add(new Location(uri, code, type, label, geometry));
        }
      }
    }

    return results;
  }

  public List<Location> getWithinGeometry(Geometry envelope, String predicate, String... types)
  {
    RDFConnectionRemoteBuilder builder = RDFConnectionRemote.create().destination(properties.getJenaUrl());

    List<Location> results = new ArrayList<Location>();

    WKTWriter.write(new GeometryWrapper(envelope, "http://www.opengis.net/ont/geosparql#wktLiteral"));

    try (RDFConnection conn = builder.build())
    {

      StringBuilder statement = new StringBuilder();
      statement.append(PREFIXES + """
          SELECT ?uri ?code ?label ?type ?wkt ?pop
          FROM <http://terraframe.ai/g1>
          WHERE {
            ?geom geo:asWKT ?sWkt .
            ?sUri geo:hasGeometry ?geom .
            ?sUri a ?sType .
            ?sUri ?pred ?uri .
            ?uri ai:GeoObject-code ?code .
            ?uri rdfs:label ?label .
            ?uri a ?type .
            OPTIONAL {
                ?uri ai:DisseminationArea-population ?pop .
                ?uri geo:hasGeometry ?g .
                ?g geo:asWKT ?wkt .
            }
            FILTER (geof:sfContains(?envelope, ?sWkt))""");

      if (types.length > 0)
      {
        statement.append("\n FILTER (?sType IN (");

        for (int i = 0; i < types.length; i++)
        {
          if (i != 0)
          {
            statement.append(", ");
          }

          statement.append("?type" + i);
        }
        statement.append("))");
      }

      statement.append("} LIMIT 100");

      // Use ParameterizedSparqlString to inject the URI safely
      ParameterizedSparqlString pss = new ParameterizedSparqlString();
      pss.setCommandText(statement.toString());
      pss.setLiteral("envelope", envelope.toText(), new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral"));
      pss.setIri("pred", predicate);

      for (int i = 0; i < types.length; i++)
      {
        pss.setIri("type" + i, types[i]);
      }

      try (QueryExecution qe = conn.query(pss.asQuery()))
      {
        ResultSet rs = qe.execSelect();

        while (rs.hasNext())
        {
          QuerySolution qs = rs.next();

          String uri = qs.getResource("uri").getURI();
          String code = qs.getLiteral("code").getString();
          String wkt = qs.getLiteral("wkt").getString();
          String label = qs.getLiteral("label").getString();
          String type = readString(qs, "type");
          Integer pop = qs.contains("pop") ? qs.getLiteral("pop").getInt() : 0;

          WKTReader reader = WKTReader.extract(wkt);
          Geometry geometry = reader.getGeometry();

          results.add(new Location(uri, code, type, label, geometry).addProperty("population", pop));
        }
      }
    }

    return results;
  }

}