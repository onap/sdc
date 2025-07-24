package org.openecomp.sdcrests.common;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
@ComponentScan(basePackages = {
    "org.openecomp.sdcrests",
    "org.openecomp.sdc.vendorsoftwareproduct",
    "org.openecomp.sdc.action",
    "org.openecomp.sdc.applicationconfig",
    "org.openecomp.core.externaltesting",
    "org.openecomp.sdc.validation"
})
public class OpenECompSdcRestConfiguration {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    
}
