/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.applicationconfig.impl;

import java.util.Collection;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.impl.ApplicationConfigImpl;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.applicationconfig.ApplicationConfigManager;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

/**
 * Created by Talio on 8/8/2016.
 */
public class ApplicationConfigManagerImpl implements ApplicationConfigManager {

    private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR = "SCHEMA_GENERATOR_INITIALIZATION_ERROR";
    private static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG = "Error occurred while loading questionnaire schema templates";
    private ApplicationConfig applicationConfig = new ApplicationConfigImpl();

    @Override
    public void insertIntoTable(String namespace, String key, String value) {
        try {
            applicationConfig.insertValue(namespace, key, value);
        } catch (Exception exception) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId(SCHEMA_GENERATOR_INITIALIZATION_ERROR)
                    .withMessage(SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG).build(), exception);
        }
    }

    @Override
    public ConfigurationData getFromTable(String namespace, String key) {
        return applicationConfig.getConfigurationData(namespace, key);
    }

    @Override
    public Collection<ApplicationConfigEntity> getListOfConfigurationByNamespace(String namespace) {
        return applicationConfig.getListOfConfigurationByNamespace(namespace);
    }
}
