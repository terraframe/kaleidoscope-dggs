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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.model.CollectionMetadata;
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
import ai.terraframe.kaleidoscope.dggs.core.model.ZoneCollection;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.BedrockResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.InformationResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.ToolUseResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;
import ai.terraframe.kaleidoscope.dggs.core.model.message.BasicMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.DggsJsonMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.DisambiguateMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.FeatureMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.Message;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ZoneMessage;
import ai.terraframe.kaleidoscope.dggs.core.serialization.IntervalDeserializer;
import software.amazon.awssdk.core.document.Document;

@Service
public class ChatService
{
  private static final Logger       log = LoggerFactory.getLogger(ChatService.class);

  @Autowired
  private BedrockConverseService    bedrock;

  @Autowired
  private JenaService               jena;

  @Autowired
  private RemoteDggsServiceIF       dggs;

  @Autowired
  private CollectionService         collectionService;

  @Autowired
  private CollectionMetadataService dggrService;

  @Autowired
  private DggalService              dggalService;

  public Message query(List<ClientMessage> messages)
  {
    try
    {
      List<Collection> collections = this.collectionService.getAll();

      BedrockResponse message = this.bedrock.execute(collections, messages);

      return process(collections, messages, message);
    }
    catch (GenericRestException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log.error("Error invoking a remote service: ", e);

      throw new GenericRestException("The chat agent was unable to generate a response. If your chat history is not relevant to the current request, you can try clearing your chat history and sending your message again.", e);
    }
  }

  private Message process(List<Collection> collections, List<ClientMessage> messages, BedrockResponse message) throws IOException, InterruptedException
  {
    if (message.getType().equals(BedrockResponse.Type.TOOL_USE))
    {
      ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

      if (toolUse.getName().equals(BedrockConverseService.LOCATION_DATA))
      {
        return data(toolUse);
      }
      else if (toolUse.getName().equals(BedrockConverseService.NAME_RESOLUTION))
      {
        Map<String, Document> parameters = toolUse.getParameters();
        String locationName = parameters.get("locationName").asString();

        LocationPage page = this.jena.fullTextLookup(locationName);

        if (page.getCount() == 0)
        {
          throw new GenericRestException("Unable to find a location with the name [" + locationName + "]");
        }
        else if (page.getCount() > 1)
        {
          return new DisambiguateMessage(toolUse.getToolUseId(), locationName, page);
        }

        // The location has been resolved to an uri
        Location location = page.getLocations().iterator().next();
        String uri = (String) location.getProperties().get("uri");

        List<ClientMessage> clone = new LinkedList<>(messages);
        clone.add(new ClientMessage("The location uri is: " + uri));

        BedrockResponse toolResponse = this.bedrock.execute(collections, clone);

        return process(collections, clone, toolResponse);
      }
      else if (toolUse.getName().equals(BedrockConverseService.POWER_INFRASTRUCTURE))
      {
        String[] types = new String[] { "http://terraframe.ai#PowerStation", "http://terraframe.ai#PowerSubstation", "http://terraframe.ai#PowerTransformer" };

        List<DggsJsonData> zones = dggsjson(toolUse);

        List<Location> features = zones.stream() //
            .flatMap(dggsjon -> this.dggalService.dggsjsonToFeatures(dggsjon).stream()) //
            .flatMap(feature -> this.jena.getWithinGeometry((Geometry) feature.getDefaultGeometry(), types).stream()).toList();

        return new FeatureMessage(toolUse.getToolUseId(), zones, features);
      }
      else if (toolUse.getName().equals(BedrockConverseService.DISSEMINATION_AREAS))
      {
        String[] types = new String[] { "http://terraframe.ai#PowerStation", "http://terraframe.ai#PowerSubstation", "http://terraframe.ai#PowerTransformer" };

        List<DggsJsonData> zones = dggsjson(toolUse);

        Set<Location> features = zones.stream() //
            .flatMap(dggsjon -> this.dggalService.dggsjsonToFeatures(dggsjon).stream()) //
            .flatMap(feature -> this.jena.getWithinGeometry((Geometry) feature.getDefaultGeometry(), "http://terraframe.ai#ProvidesPower", types).stream()).collect(Collectors.toSet());

        int totalPopulation = features.stream().mapToInt(location -> (int) location.getProperties().get("population")).sum();

        return new FeatureMessage(toolUse.getToolUseId(), zones, features, totalPopulation);
      }
    }
    else if (message.getType().equals(BedrockResponse.Type.INFORMATION))
    {
      String content = message.asType(InformationResponse.class).getContent();

      return new BasicMessage(content);
    }

    throw new UnsupportedOperationException();
  }

  private Message data(ToolUseResponse toolUse) throws IOException, InterruptedException
  {
    Map<String, Document> parameters = toolUse.getParameters();
    String format = parameters.containsKey("format") ? parameters.get("format").asString() : null;

    if (format != null && format.equals("geojson"))
    {
      String uri = parameters.get("uri").asString();
      String category = parameters.get("category").asString();
      Date datetime = parameters.containsKey("date") ? IntervalDeserializer.parse(parameters.get("date").asString()) : null;
      String filter = parameters.containsKey("filter") ? parameters.get("filter").asString() : null;
      Integer zoneDepth = parameters.containsKey("zone-depth") ? parameters.get("zone-depth").asNumber().intValue() : Integer.valueOf(9);

      return this.geojson(uri, category, datetime, filter, zoneDepth);
    }

    List<DggsJsonData> data = dggsjson(toolUse);

    return new DggsJsonMessage(data);
  }

  private List<DggsJsonData> dggsjson(ToolUseResponse toolUse) throws IOException, InterruptedException
  {
    Map<String, Document> parameters = toolUse.getParameters();
    String uri = parameters.get("uri").asString();
    String collectionId = parameters.get("category").asString();
    Date datetime = parameters.containsKey("date") ? IntervalDeserializer.parse(parameters.get("date").asString()) : null;
    String filter = parameters.containsKey("filter") ? parameters.get("filter").asString() : null;
    Integer zoneDepth = parameters.containsKey("zone-depth") ? parameters.get("zone-depth").asNumber().intValue() : Integer.valueOf(9);

    Location location = this.jena.getLocation(uri);

    // return geojson(category, location, datetime, filter, zoneDepth);
    return dggsjson(collectionId, location, datetime, filter, zoneDepth);
  }

  public Message geojson(String uri, String collectionId, Date datetime, String filter, Integer zoneDepth)
  {
    try
    {
      Location location = this.jena.getLocation(uri);

      return geojson(collectionId, location, datetime, filter, zoneDepth);
    }
    catch (GenericRestException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log.error("Error invoking a remote service: ", e);

      throw new GenericRestException("The chat agent was unable to generate a response. If your chat history is not relevant to the current request, you can try clearing your chat history and sending your message again.", e);
    }
  }

  private Message geojson(String collectionId, Location location, Date datetime, String filter, Integer zoneDepth) throws IOException, InterruptedException
  {
    Collection collection = this.collectionService.getOrThrow(collectionId);

    CollectionMetadata metadata = this.dggrService.get(collectionId).orElseThrow(() -> new GenericRestException("Unabled to retrieve DGGR information for collection [" + collectionId + "]"));

    Zones zones = this.dggs.zones(collection, metadata.getDggrs(), zoneDepth, location, datetime);

    if (zones.getZones().size() > 0)
    {
      Integer subzoneDepth = metadata.getDggs().getMaxRefinementLevel() - zoneDepth;

      JsonArray features = new JsonArray();

      for (String zoneId : zones.getZones())
      {
        features.addAll(this.dggs.geojson(collection, metadata.getDggrs(), zoneId, subzoneDepth, datetime, filter));
      }

      return new ZoneMessage(new ZoneCollection(location.getGeometry().getEnvelopeInternal(), features));
    }

    throw new GenericRestException("Unable to get zone information for the collection [" + collectionId + "] and the bounds of [" + location.getProperties().get("label") + "]");
  }

  private List<DggsJsonData> dggsjson(final String collectionId, final Location location, final Date datetime, final String filter, final Integer zoneDepth) throws IOException, InterruptedException
  {
    Collection collection = this.collectionService.getOrThrow(collectionId);

    CollectionMetadata metadata = this.dggrService.get(collectionId).orElseThrow(() -> new GenericRestException("Unabled to retrieve DGGR information for collection [" + collectionId + "]"));

    Zones zones = this.dggs.zones(collection, metadata.getDggrs(), zoneDepth, location, datetime);

    if (zones.getZones().size() > 0)
    {
//      Integer subzoneDepth = metadata.getDggs().getMaxRefinementLevel() - zoneDepth;
      Integer subzoneDepth = null;

      List<DggsJsonData> data = zones.getZones().stream().map(zoneId -> {
        try
        {
          return this.dggs.json(collection, metadata.getDggrs(), zoneId, subzoneDepth, datetime, filter);
        }
        catch (IOException | InterruptedException e)
        {
          throw new GenericRestException("Unable to get zone information for the collection [" + collectionId + "] and the bounds of [" + location.getProperties().get("label") + "]");
        }
      }).toList();

      return data;
    }

    throw new GenericRestException("Unable to get zone information for the collection [" + collectionId + "] and the bounds of [" + location.getProperties().get("label") + "]");
  }
}