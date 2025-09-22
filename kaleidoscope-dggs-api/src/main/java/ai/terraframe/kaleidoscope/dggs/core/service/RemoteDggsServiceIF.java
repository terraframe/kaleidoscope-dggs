package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;

@Component
public interface RemoteDggsServiceIF
{

  JsonArray data(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

  String html(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException;

}