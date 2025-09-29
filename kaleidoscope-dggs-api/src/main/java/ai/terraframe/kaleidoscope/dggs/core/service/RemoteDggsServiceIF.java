package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
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
  JsonArray data(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  String html(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Location location) throws IOException, InterruptedException;

  Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Envelope envelope) throws IOException, InterruptedException;

  Map<String, CollectionsAndLinks> collections() throws IOException, InterruptedException;

  DggrsAndLinks dggs(String baseUrl, String collectionId) throws IOException, InterruptedException;
}