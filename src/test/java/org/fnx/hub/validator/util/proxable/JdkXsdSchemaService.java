package org.fnx.hub.validator.util.proxable;

import java.text.MessageFormat;
import org.fnx.hub.validator.annotation.ValidatePayload;
import org.fnx.hub.validator.impl.SchemaType;
import org.fnx.hub.validator.util.SchemaService;

public class JdkXsdSchemaService implements SchemaService {
  @Override
  @ValidatePayload(type = SchemaType.XSD, schema = "path/to/schema.xsd")
  public String doWork(String payload) {
    return MessageFormat.format("WORK:{0}", payload);
  }

  @Override
  public String plain(String payload) {
    return MessageFormat.format("PLAIN:{0}", payload);
  }
}
