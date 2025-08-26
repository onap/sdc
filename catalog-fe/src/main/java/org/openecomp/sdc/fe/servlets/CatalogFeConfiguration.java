package org.openecomp.sdc.fe.servlets;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {
    "org.openecomp.sdnc.catalog.backend.dao",
    "org.elasticsearch.mapping",
    "org.openecomp.sdnc.catalog.backend.artifacts",
    "org.openecomp.sdc.fe",
    "org.openecomp.sdc.common"
})
public class CatalogFeConfiguration {

}
