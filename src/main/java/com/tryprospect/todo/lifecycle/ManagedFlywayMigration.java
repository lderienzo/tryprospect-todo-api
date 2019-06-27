package com.tryprospect.todo.lifecycle;

import io.dropwizard.lifecycle.Managed;
import org.flywaydb.core.Flyway;

import javax.inject.Inject;

public class ManagedFlywayMigration implements Managed {

  private final Flyway flyway;

  @Inject
  public ManagedFlywayMigration(Flyway flyway) {
    this.flyway = flyway;
  }

  @Override
  public void start() {
    flyway.migrate();
  }

  @Override
  public void stop() {
    // Nothing to do
  }

}
