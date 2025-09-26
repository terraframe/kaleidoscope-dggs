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
package ai.terraframe.kaleidoscope.dggs.web.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
import ai.terraframe.kaleidoscope.dggs.core.service.JenaService;
import ai.terraframe.kaleidoscope.dggs.core.service.RemoteDggsServiceIF;
import ai.terraframe.kaleidoscope.dggs.web.model.QueryRequest;

@RestController
@Validated
public class LocationController
{

  @Autowired
  private JenaService       jena;

  @Autowired
  private RemoteDggsServiceIF service;

  @PostMapping("/api/full-text-lookup")
  @ResponseBody
  public ResponseEntity<LocationPage> fullTextLookup(@RequestBody QueryRequest request)
  {
    LocationPage locations = this.jena.fullTextLookup(request.getQuery());

    return new ResponseEntity<LocationPage>(locations, HttpStatus.OK);
  }

  @GetMapping("/api/data")
  @ResponseBody
  public ResponseEntity<String> data( //
      @RequestParam(defaultValue = "winnipeg-dem") String collectionId, //
      @RequestParam(defaultValue = "G0-51FC9-A") String zoneId, //
      @RequestParam(defaultValue = "ISEA3H") String dggrsId, //
      @RequestParam(name = "zone-depth", defaultValue = "7") Integer zoneDepth) throws IOException, InterruptedException
  {
    JsonArray data = this.service.data(collectionId, dggrsId, zoneId, zoneDepth);

    return ResponseEntity.ok(data.toString());
  }

}
