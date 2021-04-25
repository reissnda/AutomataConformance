package org.apromore.alignment.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class AppConfig {

  @Bean
  @Primary
  public ObjectMapper mapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    return mapper;
  }

  // Used to prevent manifest scanning on tomcat, stops tomcat from attempting to load some jars off the classpath.
  @Bean
  public TomcatServletWebServerFactory tomcatFactory() {
    return new TomcatServletWebServerFactory() {
      @Override
      protected void postProcessContext(Context context) {
        ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
      }
    };
  }
}
