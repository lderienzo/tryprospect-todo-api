package com.tryprospect.todo.utils.yaml;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DockerComposeYamlReader implements GenericYamlReader {
    public static final String DOCKER_COMPOSE_FILE = "docker-compose.yml";

    @Override
    public <DockerCompose> DockerCompose readObjectFromFile(String yamlFileToRead, Class<DockerCompose> objectTypeToCreate) {
        DockerCompose dockerCompose = null;
        try {
            dockerCompose = YAML_MAPPER.readValue(new File(yamlFileToRead), objectTypeToCreate);
        } catch (IOException e) {
            log.error("Error reading "+DOCKER_COMPOSE_FILE+" file.", e);
        }
        return dockerCompose;
    }
}
