package org.openecomp.sdcrests.common;


import javax.validation.Validator;

import org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter;
import org.openecomp.sdc.logging.servlet.jaxrs.LoggingResponseFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
     /* Jackson ObjectMapper with NON_NULL serialization */ 
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    /* Bean validation support */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(Validator validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        return processor;
    }

    // Optional: Filters for logging (if reused from CXF, adapt as Spring filters if needed)
    @Bean
    public LoggingRequestFilter loggingRequestFilter() {
        LoggingRequestFilter filter = new LoggingRequestFilter();
        filter.setRequestIdHeaders("X-ONAP-RequestID,X-RequestID,X-TransactionId,X-ECOMP-RequestID");
        filter.setPartnerNameHeaders("USER_ID,X-ONAP-PartnerName,User-Agent");
        return filter;
    }

    @Bean
    public LoggingResponseFilter loggingResponseFilter() {
        return new LoggingResponseFilter();
    }

}
