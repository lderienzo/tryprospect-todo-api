package com.tryprospect.todo.utils.yaml;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigYamlReader implements GenericYamlReader {
    public static final String CONFIG_YAML_FILE = "config.yml";
    @Override
    public <Config> Config readObjectFromFile(String yamlFileToRead, Class<Config> objectTypeToCreate) {
        Config config = null;
        try {
            config = YAML_MAPPER.readValue(new File(yamlFileToRead), objectTypeToCreate);
        } catch (IOException e) {
            log.error("Error reading "+CONFIG_YAML_FILE+" file.", e);
        }
        return config;
    }
}
