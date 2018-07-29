package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.asdctool.impl.internal.tool.DeleteComponentHandler;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

@Configuration
@Import({DAOSpringConfig.class, CatalogModelSpringConfig.class})
public class InternalToolConfiguration {
    @Bean(name = "elasticsearchConfig")
    public PropertiesFactoryBean mapper() {
        String configHome = System.getProperty("config.home");
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new FileSystemResource(configHome + "/elasticsearch.yml"));
        return bean;
    }
    
    @Bean
    public DeleteComponentHandler deleteComponentHandler() {
        return new DeleteComponentHandler();
    }
   
}
