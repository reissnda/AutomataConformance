package org.apromore.alignment.web.config;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static springfox.documentation.schema.AlternateTypeRules.newRule;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  private static final String BASE_PACKAGE = "org.apromore.alignment";

  private static final String BASE_PATH = "/";

  private TypeResolver typeResolver;

  @Value("${info.app.version}")
  String version;

  @Autowired
  public SwaggerConfig(TypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Bean
  public Docket athenaApi() {
    ResolvedType wildcard = typeResolver.resolve(WildcardType.class);
    ResolvedType base = typeResolver.resolve(ResponseEntity.class, WildcardType.class);
    ResolvedType deferredResult = typeResolver.resolve(DeferredResult.class, base);

    // @formatter:off
    return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).pathMapping(BASE_PATH)
        .directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class, List.class)
        .alternateTypeRules(newRule(deferredResult, wildcard));
    // @formatter:on
  }

  private ApiInfo apiInfo() {
    // @formatter:off
    String title = "Apromore Alignment API";
    String description = "Apromore Alignment Generation API";

    String name = "Apromore";
    String url = "https://apromore.org";
    String email = "";
    Contact contact = new Contact(name, url, email);

    String terms = "";
    String license = "LGPL";
    String licenseUrl = "";

    return new ApiInfo(title, description, version, terms, contact, license, licenseUrl, Collections.emptyList());
    // @formatter:on
  }
}
