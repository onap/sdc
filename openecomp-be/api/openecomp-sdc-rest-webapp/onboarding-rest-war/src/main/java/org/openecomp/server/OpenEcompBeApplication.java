package org.openecomp.server;
import org.openecomp.sdcrests.common.OpenECompSdcRestConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(OpenECompSdcRestConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class OpenEcompBeApplication extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OpenEcompBeApplication.class);
    }

    public static void main(String[] args) {
        System.out.println(">>> Starting application...");
        SpringApplication.run(OpenEcompBeApplication.class, args);
        System.out.println(">>> Started application.");
    }

}
