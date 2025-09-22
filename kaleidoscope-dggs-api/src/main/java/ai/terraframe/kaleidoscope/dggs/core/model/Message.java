package ai.terraframe.kaleidoscope.dggs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message
{
  /**
   * Content of the message
   */
  private String content;

  /**
   * Session if of the conversation
   */
  private String sessionId;

}
