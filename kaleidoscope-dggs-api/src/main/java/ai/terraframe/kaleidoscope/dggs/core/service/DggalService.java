package ai.terraframe.kaleidoscope.dggs.core.service;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequence;
import org.dggal.DGGAL;
import org.dggal.DggalDggrs;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.PropertyData;
import jakarta.annotation.PreDestroy;

@Service
public class DggalService
{
  private static final Logger log = LoggerFactory.getLogger(DggalService.class);

  private final DGGAL         dggal;

  private MemorySegment       module;

  public DggalService()
  {
    this.dggal = DGGAL.global();

    try
    {
      this.module = dggal.init();
    }
    catch (Throwable e)
    {
      throw new RuntimeException(e);
    }

  }

  @PreDestroy
  public void destroy()
  {
    if (this.module != null)
    {
      try
      {
        this.dggal.terminate(this.module);
      }
      catch (Throwable e)
      {
        throw new RuntimeException(e);
      }
    }
  }

  public List<SimpleFeature> dggsjsonToFeatures(DggsJsonData dggsjson)
  {
    SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
    typeBuilder.setName("PolygonFeature");
    typeBuilder.add("geometry", Polygon.class); // Geometry attribute
    typeBuilder.add("value", Double.class); // Example attribute
    typeBuilder.add("zone", String.class); // Example attribute

    SimpleFeatureType featureType = typeBuilder.buildFeatureType();

    List<SimpleFeature> features = new LinkedList<>();
    try
    {
      final DggalDggrs dggrs = dggal.newDggrs(module, dggsjson.getDggrs());

      try
      {
        log.trace("Processing zone response: " + dggsjson.getZoneId());
        // const low = BigInt.asUintN(64, 12130488n);

        // log.trace("Max depth", dggrs.getMaxDepth());

        long parent = dggrs.getZoneFromTextID(dggsjson.getZoneId());

        // log.trace("Parent level", dggrs.getZoneLevel(parent));

        // const first = dggrs.getFirstSubZone(parent, 5);

        // log.trace("First subzone", first);

        // log.trace("Index", dggrs.getSubZoneIndex(parent, first));
        // log.trace("At Index", dggrs.getSubZoneAtIndex(parent, 5,
        // BigInt("0")));

        if (dggsjson.getDepths().size() > 0)
        {
          Map<String, List<PropertyData>> propertyMap = dggsjson.getValues().getProperties();
          int relativeDepth = Integer.parseInt(dggsjson.getDepths().get(0));

          if (!propertyMap.isEmpty())
          {
            String key = propertyMap.keySet().iterator().next();

            List<PropertyData> propertyData = propertyMap.get(key);
            List<Object> data = propertyData.get(0).getData();

            long[] zones = dggrs.getSubZones(parent, relativeDepth);

            if (data.size() != zones.length)
            {
              log.trace("Subzone count mismatch for zone [" + dggsjson.getZoneId() + "]: " + data.size() + ", " + zones.length);

              log.trace("Data: " + data);
              log.trace("Zones: " + zones);

              log.trace("First subzone" + dggrs.getFirstSubZone(parent, 5));
              log.trace("At Index" + dggrs.getSubZoneAtIndex(parent, 5, 0));
            }

            for (int i = 0; i < data.size(); i++)
            {

              if (data.get(i) != null)
              {
                long zone = dggrs.getSubZoneAtIndex(parent, 5, i);

                log.trace("Creating geometry for zone: " + dggrs.getZoneTextID(zone));

                double[] vertices = dggrs.getZoneRefinedWGS84Vertices(zone, 0);

                LinkedList<Coordinate> coordinates = new LinkedList<>();

                for (int j = 0; j < vertices.length; j += 2)
                {
                  coordinates.add(new Coordinate(vertices[j + 1] * 180 / Math.PI, vertices[j] * 180 / Math.PI));
                }

                if (coordinates.size() > 0)
                {
                  Coordinate first = coordinates.getFirst();
                  Coordinate last = coordinates.getLast();

                  if (first.x != last.x || first.y != last.y)
                  {
                    coordinates.add(new Coordinate(first.x, first.y));
                  }
                }
                else
                {
                  log.trace("No vertices returned for zone");
                }

                // Must use the jena custom coordinate sequence because its WKT
                // writer assumes that is the implementation of the coordinate
                // sequence
                CustomCoordinateSequence sequence = new CustomCoordinateSequence(coordinates.toArray(new Coordinate[coordinates.size()]));

                GeometryFactory geometryFactory = new GeometryFactory();
                Polygon polygon = geometryFactory.createPolygon(sequence);

                if (!polygon.isValid())
                {
                  log.trace("Not valid");
                }

                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
                featureBuilder.add(polygon); // Add geometry
                featureBuilder.add(data.get(i)); // Add attribute

                features.add(featureBuilder.buildFeature(null));
              }
            }
          }
        }
      }
      finally
      {
        dggrs.close();
      }
    }
    catch (Throwable e)
    {
      throw new GenericRestException("Unable to generate features from DGGS json response", e);
    }

    return features;
  }

  public List<String> zones(String dggrsId, Integer depth, Envelope envelope)
  {
    try
    {
      // todo a simple demo case
      final DggalDggrs dggrs = dggal.newDggrs(module, dggrsId);

      try
      {
        long[] zoneIds = dggrs.listZones(depth, new double[] { envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY() });

        return Arrays.stream(zoneIds).mapToObj(zoneId -> {
          try
          {

            return dggrs.getZoneTextID(zoneId);
          }
          catch (Throwable t)
          {
            throw new RuntimeException(t);
          }
        }).toList();
      }
      finally
      {
        dggrs.close();
      }
    }
    catch (Throwable e)
    {
      throw new GenericRestException("Unable to generate features from DGGS json response", e);
    }
  }
}
