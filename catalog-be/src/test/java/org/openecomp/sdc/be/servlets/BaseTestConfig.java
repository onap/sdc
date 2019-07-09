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

package org.openecomp.sdc.be.servlets;

import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.DefaultExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.StorageExceptionMapper;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

public class BaseTestConfig {

    @Bean
    ComponentsUtils componentsUtils() {return new ComponentsUtils(mock(AuditingManager.class));}

    @Bean
    DefaultExceptionMapper defaultExceptionMapper() {return new DefaultExceptionMapper();}

    @Bean
    ComponentExceptionMapper componentExceptionMapper() {
        return new ComponentExceptionMapper(componentsUtils());
    }

    @Bean
    StorageExceptionMapper storageExceptionMapper() {
        return new StorageExceptionMapper(componentsUtils());
    }

}
