package org.openecomp.sdc.be.model.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.dao.cassandra","org.openecomp.sdc.be.model.cache","org.openecomp.sdc.be.model.jsontitan.operations","org.openecomp.sdc.be.model.jsontitan.utils", "org.openecomp.sdc.be.model.operations.impl"})
public class ModelOperationsSpringConfig {
}
