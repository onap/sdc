package org.openecomp.sdc.asdctool.configuration;

import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.asdctool.migration.config.MigrationSpringConfig;
import org.openecomp.sdc.be.dao.config.DAOSpringConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DAOSpringConfig.class)
@ComponentScan({
        "org.openecomp.sdc.be.model.operations.impl",
        "org.openecomp.sdc.be.model.cache",
        "org.openecomp.sdc.be.dao.titan",
        "org.openecomp.sdc.be.dao.cassandra",
        "org.openecomp.sdc.be.model.jsontitan.operations",
        "org.openecomp.sdc.be.dao.jsongraph",
        })
public class ArtifactUUIDFixConfiguration {

    @Bean
    public ArtifactUuidFix artifactUuidFix() {
        return new ArtifactUuidFix();
    }

}
