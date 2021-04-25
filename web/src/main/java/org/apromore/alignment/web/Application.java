package org.apromore.alignment.web;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Main Web Application
 */

@EntityScan(basePackages = { "org.apromore.alignment" })
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
public class Application {

  public static final String DEFAULT_TIMEZONE = "UTC";

  public static void main(final String[] args) {
    // timezone set to UTC across the application
    TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
    SpringApplication.run(Application.class, args);
  }
}