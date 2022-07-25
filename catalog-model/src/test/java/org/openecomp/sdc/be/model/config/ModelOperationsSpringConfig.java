/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.config;

import org.mockito.Mockito;
import org.openecomp.sdc.be.model.validation.ToscaFunctionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({"org.openecomp.sdc.be.dao.cassandra", "org.openecomp.sdc.be.model.cache",
    "org.openecomp.sdc.be.client",
    "org.openecomp.sdc.be.model.jsonjanusgraph.operations",
    "org.openecomp.sdc.be.model.jsonjanusgraph.utils",
    "org.openecomp.sdc.be.model.jsonjanusgraph.config",
        "org.openecomp.sdc.be.model.operations.impl"})
@PropertySource("classpath:dao.properties")
public class ModelOperationsSpringConfig {

    @Bean
    public ToscaFunctionValidator toscaFunctionValidator() {
        return Mockito.mock(ToscaFunctionValidator.class);
    }

}
