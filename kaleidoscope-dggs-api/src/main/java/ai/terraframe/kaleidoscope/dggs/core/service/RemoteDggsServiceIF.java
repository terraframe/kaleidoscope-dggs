package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;

@Component
public interface RemoteDggsServiceIF
{
  Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Location location, Date datetime) throws IOException, InterruptedException;

  Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Envelope envelope, Date datetime) throws IOException, InterruptedException;

  Map<String, CollectionsAndLinks> collections() throws IOException, InterruptedException;

  DggrsAndLinks dggs(String baseUrl, String collectionId) throws IOException, InterruptedException;

  JsonArray geojson(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth, Date datetime, String fiilter) throws IOException, InterruptedException;

  String html(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  JsonArray json(Collection collection, Dggr dggrs, String zoneId, Integer zoneDepth, Date datetime, String filter) throws IOException, InterruptedException;
}