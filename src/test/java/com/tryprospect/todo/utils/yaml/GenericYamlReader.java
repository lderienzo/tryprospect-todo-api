package com.tryprospect.todo.utils.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public interface GenericYamlReader {
    ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    <T> T readObjectFromFile(String yamlFileToRead, Class<T> objectTypeToCreate);
}
