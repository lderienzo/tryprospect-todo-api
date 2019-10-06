package com.tryprospect.todo.db.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Config {
    private Database database;

    @JsonCreator
    public Config(@JsonProperty("database") Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public static class Database {
        private String driverClass;
        private String user;
        private String password;
        private String url;

        @JsonCreator
        Database(@JsonProperty("driverClass") String driverClass,
                 @JsonProperty("user") String user,
                 @JsonProperty("password") String password,
                 @JsonProperty("url") String url) {

            this.driverClass = driverClass;
            this.user = user;
            this.password = password;
            this.url = url;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return url;
        }

        public String getDbName() {
            return getDbNameFromUrl();
        }

        private String getDbNameFromUrl() {
            return url.substring(url.lastIndexOf("/"));
        }
    }
}
