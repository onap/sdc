package org.openecomp.sdc.fe;

import org.openecomp.sdc.fe.servlets.WebConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import(WebConfig.class)
@ComponentScan(basePackages = {
    "org.openecomp.sdnc.catalog.backend.dao",
    "org.elasticsearch.mapping",
    "org.openecomp.sdnc.catalog.backend.artifacts",
    "org.openecomp.sdc.fe",
    "org.openecomp.sdc.common"
})
public class CatalogFeApplication extends SpringBootServletInitializer{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CatalogFeApplication.class);
    }
    public static void main(String[] args) {
        System.out.println(">>> Starting application...");
        SpringApplication.run(CatalogFeApplication.class, args);
        System.out.println(">>> Started application.");
    }

}
