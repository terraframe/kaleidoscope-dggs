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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.Message;
import ai.terraframe.kaleidoscope.dggs.core.service.ChatService;

@RestController
@Validated
public class ChatController
{
  @Autowired
  private ChatService service;

  @GetMapping("/api/chat/zones")
  @ResponseBody
  public ResponseEntity<Message> zones(@RequestParam(name = "uri") String uri, //
      @RequestParam(name = "category") String category, //
      @RequestParam(name = "datetime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date datetime,
      @RequestParam(name = "filter", required = false) String filter, //
      @RequestParam(name = "zoneDepth", required = false) Integer zoneDepth //
      )
  {
    Message message = this.service.data(uri, category, datetime, filter, zoneDepth);

    return ResponseEntity.ok(message);
  }

  @PostMapping("/api/chat/query")
  @ResponseBody
  public ResponseEntity<Message> query(@RequestBody List<ClientMessage> messages)
  {
    Message message = this.service.query(messages);

    return ResponseEntity.ok(message);
  }

}
