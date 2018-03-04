package org.openecomp.sdc.be.model.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.model.jsontitan.operations","org.openecomp.sdc.be.model.jsontitan.utils"})
public class ModelOperationsSpringConfig {
}
