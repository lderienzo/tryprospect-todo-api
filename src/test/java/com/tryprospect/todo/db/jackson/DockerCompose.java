package com.tryprospect.todo.db.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerCompose {
    private Services services;

    @JsonCreator
    public DockerCompose(@JsonProperty("services") Services services) {
        this.services = services;
    }

    public static class Services {
        private Postgres postgres;

        @JsonCreator
        Services(@JsonProperty("postgres") Postgres postgres) {
            this.postgres = postgres;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Postgres {
            private String image;

            @JsonCreator
            Postgres(@JsonProperty("image") String image) {
                this.image = image;
            }

            public String getImage() {
                return image;
            }
        }

        public Postgres getPostgres() {
            return postgres;
        }
    }

    public Services getServices() {
        return services;
    }
}
