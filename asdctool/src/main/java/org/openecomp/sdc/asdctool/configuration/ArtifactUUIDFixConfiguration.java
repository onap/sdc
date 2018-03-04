package org.openecomp.sdc.asdctool.configuration;


import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.config.CatalogBESpringConfig;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

@Configuration
@Import({DAOSpringConfig.class, CatalogBESpringConfig.class, CatalogModelSpringConfig.class})
@ComponentScan({"org.openecomp.sdc.asdctool.migration.tasks",//migration tasks
        "org.openecomp.sdc.asdctool.migration.config.mocks"
                })
public class ArtifactUUIDFixConfiguration {

    @Bean
    public ArtifactUuidFix artifactUuidFix() {
        return new ArtifactUuidFix();
    }
    

    @Bean(name = "serviceDistributionArtifactsBuilder")
    public ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder() {
        return new ServiceDistributionArtifactsBuilder();
    }

    @Bean(name = "elasticsearchConfig")
    public PropertiesFactoryBean mapper() {
        String configHome = System.getProperty("config.home");
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new FileSystemResource(configHome + "/elasticsearch.yml"));
        return bean;
    }


}
