package org.openecomp.sdc.asdctool.migration.config;

import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.resolver.SpringBeansMigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.MigrationTasksDao;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Import(DAOSpringConfig.class)
@ComponentScan({"org.openecomp.sdc.asdctool.migration.tasks",//migration tasks
                "org.openecomp.sdc.be.model.operations.impl",
                "org.openecomp.sdc.be.model.cache",
                "org.openecomp.sdc.be.dao.titan",
                "org.openecomp.sdc.be.dao.cassandra",
                "org.openecomp.sdc.be.model.jsontitan.operations",
                "org.openecomp.sdc.be.dao.jsongraph"})
public class MigrationSpringConfig {

    @Autowired(required=false)
    private List<Migration> migrations = new ArrayList<>();

    @Bean(name = "sdc-migration-tool")
    public SdcMigrationTool sdcMigrationTool(MigrationResolver migrationResolver, SdcRepoService sdcRepoService) {
        return new SdcMigrationTool(migrationResolver, sdcRepoService);
    }

    @Bean(name = "spring-migrations-resolver")
    public SpringBeansMigrationResolver migrationResolver(SdcRepoService sdcRepoService) {
        return new SpringBeansMigrationResolver(migrations, sdcRepoService);
    }

    @Bean(name = "sdc-repo-service")
    public SdcRepoService sdcRepoService(MigrationTasksDao migrationTasksDao) {
        return new SdcRepoService(migrationTasksDao);
    }

    @Bean(name = "sdc-migration-tasks-cassandra-dao")
    public MigrationTasksDao migrationTasksDao() {
        return new MigrationTasksDao();
    }

    @Bean(name = "cassandra-client")
    public CassandraClient cassandraClient() {
        return new CassandraClient();
    }

}
