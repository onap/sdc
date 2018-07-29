package org.openecomp.sdc.asdctool.migration.config;

import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.resolver.SpringBeansMigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.CatalogModelSpringConfig;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.openecomp.sdc.config.CatalogBESpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Import({DAOSpringConfig.class, CatalogBESpringConfig.class, CatalogModelSpringConfig.class})
@ComponentScan({"org.openecomp.sdc.asdctool.migration.tasks",//migration tasks
        "org.openecomp.sdc.asdctool.migration.config.mocks"
                })
public class MigrationSpringConfig {

    @Autowired(required=false)
    private List<Migration> migrations = new ArrayList<>();
    
    @Autowired(required=false)
    private List<PostMigration> postMigrations = new ArrayList<>();

    @Bean(name = "sdc-migration-tool")
    public SdcMigrationTool sdcMigrationTool(MigrationResolver migrationResolver, SdcRepoService sdcRepoService) {
        return new SdcMigrationTool(migrationResolver, sdcRepoService);
    }

    @Bean(name = "spring-migrations-resolver")
    public SpringBeansMigrationResolver migrationResolver(SdcRepoService sdcRepoService) {
        return new SpringBeansMigrationResolver(migrations, postMigrations, sdcRepoService);
    }

    @Bean(name = "sdc-repo-service")
    public SdcRepoService sdcRepoService(MigrationTasksDao migrationTasksDao) {
        return new SdcRepoService(migrationTasksDao);
    }

    @Bean(name = "sdc-migration-tasks-cassandra-dao")
    public MigrationTasksDao migrationTasksDao() {
        return new MigrationTasksDao();
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

    @Bean(name = "componentsCleanBusinessLogic")
    public ComponentsCleanBusinessLogic componentsCleanBusinessLogic() {return  new ComponentsCleanBusinessLogic(); }

}
