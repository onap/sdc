package org.openecomp.sdc.fe;
import org.openecomp.sdc.fe.servlets.CatalogFeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import(CatalogFeConfiguration.class)
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
