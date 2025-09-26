package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;

import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;

@Component
public interface RemoteDggsServiceIF
{
  JsonArray data(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  String html(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  Zones zones(String collectionId, String dggrsId, Integer zoneLevel, Location location) throws IOException, InterruptedException;

  Zones zones(String collectionId, String dggrsId, Integer zoneLevel, Envelope envelope) throws IOException, InterruptedException;

  String getCollectionId(String category);
}