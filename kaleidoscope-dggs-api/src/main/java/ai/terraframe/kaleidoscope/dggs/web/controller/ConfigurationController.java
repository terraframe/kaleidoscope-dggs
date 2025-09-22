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
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ai.terraframe.kaleidoscope.dggs.core.model.Style;
import ai.terraframe.kaleidoscope.dggs.core.model.VectorLayer;
import ai.terraframe.kaleidoscope.dggs.core.service.ConfigurationService;
import ai.terraframe.kaleidoscope.dggs.web.model.Configuration;

@RestController
@Validated
public class ConfigurationController
{
  @Autowired
  private ConfigurationService service;

  @GetMapping("/api/configuration/get")
  @ResponseBody
  public ResponseEntity<Configuration> getConfiguration(CsrfToken token) throws IOException, ParseException
  {
    Configuration configuration = new Configuration();
//    configuration.setStyles(this.service.getStyles());
//    configuration.setLayers(this.service.getVectorLayers());

    if (token != null)
    {
      configuration.setToken(token.getToken());
    }

    return new ResponseEntity<Configuration>(configuration, HttpStatus.OK);
  }

  @GetMapping("/api/configuration/styles")
  @ResponseBody
  public ResponseEntity<Map<String, Style>> getStyles() throws IOException, ParseException
  {
    return new ResponseEntity<Map<String, Style>>(this.service.getStyles(), HttpStatus.OK);
  }

  @GetMapping("/api/configuration/vector-layers")
  @ResponseBody
  public ResponseEntity<List<VectorLayer>> getVectorLayerConfiguration() throws IOException, ParseException
  {
    return new ResponseEntity<List<VectorLayer>>(this.service.getVectorLayers(), HttpStatus.OK);
  }

}
