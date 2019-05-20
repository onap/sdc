package org.openecomp.sdc.be.model.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.dao.cassandra", "org.openecomp.sdc.be.model.cache",
    "org.openecomp.sdc.be.model.jsonjanusgraph.operations",
    "org.openecomp.sdc.be.model.jsonjanusgraph.utils",
        "org.openecomp.sdc.be.model.operations.impl"})
@PropertySource("classpath:dao.properties")
public class ModelOperationsSpringConfig { }
