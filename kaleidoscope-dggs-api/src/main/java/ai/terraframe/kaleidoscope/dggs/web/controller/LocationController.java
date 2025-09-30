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
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;
import ai.terraframe.kaleidoscope.dggs.core.service.CollectionService;
import ai.terraframe.kaleidoscope.dggs.core.service.DggrService;
import ai.terraframe.kaleidoscope.dggs.core.service.JenaService;
import ai.terraframe.kaleidoscope.dggs.core.service.RemoteDggsServiceIF;
import ai.terraframe.kaleidoscope.dggs.web.model.QueryRequest;

@RestController
@Validated
public class LocationController
{

  @Autowired
  private JenaService         jena;

  @Autowired
  private CollectionService   collectionService;

  @Autowired
  private DggrService         dggrService;

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
      @RequestParam(name = "zone-depth", defaultValue = "7") Integer zoneDepth, //
      @RequestParam(name = "datetime" , required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date datetime) throws IOException, InterruptedException
  {
    Collection collection = this.collectionService.getOrThrow(collectionId);
    Dggr dggr = this.dggrService.getOrThrow(collectionId);

    JsonArray data = this.service.data(collection, dggr, zoneId, zoneDepth, datetime);

    return ResponseEntity.ok(data.toString());
  }

}
