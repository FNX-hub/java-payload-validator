package org.fnx.hub.validator.util.proxable;

import java.text.MessageFormat;
import org.fnx.hub.validator.annotation.ValidatePayload;
import org.fnx.hub.validator.impl.SchemaType;

public class ByteBuddyService {

  @ValidatePayload(type = SchemaType.JSON, schema = "path/to/schemaA.json")
  public String doWork(String payload) {
    return MessageFormat.format("WORK:{0}", payload);
  }

  public String plain(String payload) {
    return MessageFormat.format("PLAIN:{0}", payload);
  }
}
