package ai.terraframe.kaleidoscope.dggs.core.model.message;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientMessage
{
  public static enum SenderRole {
    USER, SYSTEM
  }

  public static enum MessageType {
    BASIC, NAME_RESOLUTION, LOCATION_RESOLVED, ERROR
  }

  private String              id;

  private SenderRole          role;

  private MessageType         messageType;

  private String              text;

  private Map<String, Object> data;

  public ClientMessage(String content)
  {
    this(SenderRole.USER, MessageType.BASIC, content);
  }

  public ClientMessage(SenderRole role, MessageType messageType, String text)
  {
    super();
    this.role = role;
    this.messageType = messageType;
    this.text = text;
    this.data = new HashMap<>();
  }

  public ClientMessage put(String key, String value)
  {
    this.data.put(key, value);

    return this;
  }

}
