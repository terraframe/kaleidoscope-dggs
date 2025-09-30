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

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ai.terraframe.kaleidoscope.dggs.core.model.message.Message;
import ai.terraframe.kaleidoscope.dggs.core.service.ChatService;

@RestController
@Validated
public class ChatController
{
  @Autowired
  private ChatService service;

  @GetMapping("/api/chat/query")
  @ResponseBody
  public ResponseEntity<Message> query(@RequestParam(name = "inputText") String inputText)
  {
    Message message = this.service.query(inputText);

    return ResponseEntity.ok(message);
  }

  @GetMapping("/api/chat/zones")
  @ResponseBody
  public ResponseEntity<Message> zones(
      @RequestParam(name = "uri") String uri, //
      @RequestParam(name = "category") String category, //
      @RequestParam(name = "datetime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date datetime)
  {
    Message message = this.service.zones(uri, category, datetime);

    return ResponseEntity.ok(message);
  }

}
