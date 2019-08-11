package com.tryprospect.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TodoConfiguration extends Configuration {
// TODO: look at how to possibly validate these members

  @Valid
  @NotNull
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();

  @Valid
  @NotNull
  @JsonProperty("httpClient")
  private HttpClientConfiguration httpClient = new HttpClientConfiguration();

  // TODO: HealthCheck here? -- Where/how is this used?
  @NotNull
  public HttpClientConfiguration getHttpClientConfiguration() {
    return httpClient;
  }

  // TODO: HealthCheck here?
  @JsonProperty("database")
  public void setDataSourceFactory(DataSourceFactory factory) {
    this.dataSourceFactory = factory;
  }

  // TODO: perhaps rename to 'getDataSource'
  @NotNull
  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
  }

}
