package ai.terraframe.kaleidoscope.dggs.core.service;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.stream.Streams;
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
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.PropertyData;

@Service
public class DggalService
{
  private final DGGAL dggal;

  public DggalService()
  {
    this.dggal = DGGAL.global();
  }

  public List<SimpleFeature> dggsjsonToFeatures(DggsJsonData dggsjson)
  {
    SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
    typeBuilder.setName("PolygonFeature");
    typeBuilder.add("geometry", Polygon.class); // Geometry attribute
    typeBuilder.add("value", Double.class); // Example attribute

    SimpleFeatureType featureType = typeBuilder.buildFeatureType();

    List<SimpleFeature> features = new LinkedList<>();
    try
    {
      MemorySegment module = dggal.init();

      // todo a simple demo case
      final DggalDggrs dggrs = dggal.newDggrs(module, dggsjson.getDggrs());

      try
      {
        System.out.println("Processing zone response: " + dggsjson.getZoneId());
        // const low = BigInt.asUintN(64, 12130488n);

        // System.out.println("Max depth", dggrs.getMaxDepth());

        long parent = dggrs.getZoneFromTextID(dggsjson.getZoneId());

        // System.out.println("Parent level", dggrs.getZoneLevel(parent));

        // const first = dggrs.getFirstSubZone(parent, 5);

        // System.out.println("First subzone", first);

        // System.out.println("Index", dggrs.getSubZoneIndex(parent, first));
        // System.out.println("At Index", dggrs.getSubZoneAtIndex(parent, 5,
        // BigInt("0")));

        if (dggsjson.getValues().size() > 0 && dggsjson.getDepths().size() > 0)
        {
          Map<String, List<PropertyData>> propertyMap = dggsjson.getValues().get(0).getProperties();
          int relativeDepth = Integer.parseInt(dggsjson.getDepths().get(0));

          if (!propertyMap.isEmpty())
          {
            String key = propertyMap.keySet().iterator().next();
            List<PropertyData> propertyData = propertyMap.get(key);
            List<Double> data = Streams.of(propertyData.get(0).getData().split(",")).map(value -> value.equals("null") ? null : Double.parseDouble(value)).toList();

            long[] zones = dggrs.getSubZones(parent, relativeDepth);

            if (data.size() != zones.length)
            {
              System.out.println("Subzone count mismatch for zone [" + dggsjson.getZoneId() + "]: " + data.size() + ", " + zones.length);

              System.out.println("Data: " + data);
              System.out.println("Zones: " + zones);

              System.out.println("First subzone" + dggrs.getFirstSubZone(parent, 5));
              System.out.println("At Index" + dggrs.getSubZoneAtIndex(parent, 5, 0));
            }

            for (int i = 0; i < data.size(); i++)
            {

              if (data.get(i) != null)
              {
                long zone = dggrs.getSubZoneAtIndex(parent, 5, i);

                System.out.println("Creating geometry for zone: " + dggrs.getZoneTextID(zone));

                double[] vertices = dggrs.getZoneRefinedWGS84Vertices(zone, 0);

                LinkedList<Coordinate> coordinates = new LinkedList<>();

                for (int j = 0; j < vertices.length; j += 2)
                {
                  coordinates.add(new Coordinate(vertices[j] * 180 / Math.PI, vertices[j + 1] * 180 / Math.PI));
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
                  System.out.println("No vertices returned for zone");
                }

                GeometryFactory geometryFactory = new GeometryFactory();
                Polygon polygon = geometryFactory.createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));

                if (!polygon.isValid())
                {
                  System.out.println("Not valid");
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
        try
        {
          dggrs.close();
        }
        finally
        {
          dggal.terminate(module);
        }
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
      MemorySegment module = dggal.init();

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
        try
        {
          dggrs.close();
        }
        finally
        {
          dggal.terminate(module);
        }
      }
    }
    catch (Throwable e)
    {
      throw new GenericRestException("Unable to generate features from DGGS json response", e);
    }
  }
}
