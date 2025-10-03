package org.fnx.hub.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.EnumMap;
import java.util.Map;
import org.fnx.hub.validator.impl.SchemaType;

/**
 * Centralized holder of {@link ObjectMapper} instances per supported {@link SchemaType}. Provides
 * convenient access to preconfigured mappers for JSON, XML and YAML processing.
 */
public enum SingletonMapper {
  INSTANCE(new EnumMap<>(SchemaType.class));

  private final Map<SchemaType, ObjectMapper> mappers;

  SingletonMapper(EnumMap<SchemaType, ObjectMapper> map) {
    this.mappers = map;
    mappers.put(SchemaType.JSON, new JsonMapper());
    mappers.put(SchemaType.XSD, new XmlMapper());
    mappers.put(SchemaType.YML, new YAMLMapper());
  }

  /**
   * Returns the {@link ObjectMapper} associated with the given schema type.
   *
   * @param type the schema type for which to retrieve the mapper
   * @return the {@link ObjectMapper} configured for the provided {@link SchemaType}
   */
  public static ObjectMapper getMapper(SchemaType type) {
    return INSTANCE.mappers.get(type);
  }

  /**
   * Serializes provided data objects using the mapper for the given schema type.
   *
   * @param type the schema type to select the serializer
   * @param data the objects to serialize
   * @return the serialized representation as {@link String}
   * @throws JsonProcessingException if serialization fails
   */
  public static String serialize(SchemaType type, Object... data) throws JsonProcessingException {
    return getMapper(type).writeValueAsString(data);
  }
}
