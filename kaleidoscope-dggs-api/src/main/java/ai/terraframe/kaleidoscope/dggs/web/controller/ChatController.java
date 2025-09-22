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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ai.terraframe.kaleidoscope.dggs.core.model.Message;
import ai.terraframe.kaleidoscope.dggs.core.service.ChatService;

@RestController
@Validated
public class ChatController
{
  @Autowired
  private ChatService service;

  @GetMapping("/api/chat/prompt")
  @ResponseBody
  public ResponseEntity<Message> prompt(@RequestParam(name = "sessionId") String sessionId, @RequestParam(name = "prompt") String prompt)
  {
    Message response = this.service.prompt(sessionId, prompt);

    return new ResponseEntity<Message>(response, HttpStatus.OK);
  }

}
