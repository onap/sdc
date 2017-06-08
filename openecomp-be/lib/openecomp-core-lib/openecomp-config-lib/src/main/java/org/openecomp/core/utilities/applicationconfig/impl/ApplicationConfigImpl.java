/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.utilities.applicationconfig.impl;

import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDao;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDaoFactory;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import java.util.Collection;
import java.util.Objects;

public class ApplicationConfigImpl implements ApplicationConfig {
  private static final ApplicationConfigDao applicationConfigDao =
      ApplicationConfigDaoFactory.getInstance().createInterface();

  private static final String CONFIGURATION_SEARCH_ERROR = "CONFIGURATION_NOT_FOUND";
  private static final String CONFIGURATION_SEARCH_ERROR_MSG =
      "Configuration for namespace %s and key %s was not found";

  @Override
  public ConfigurationData getConfigurationData(String namespace, String key) {
    ConfigurationData configurationData = applicationConfigDao.getConfigurationData(namespace, key);

    if (Objects.isNull(configurationData)) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId(CONFIGURATION_SEARCH_ERROR)
          .withMessage(String.format(CONFIGURATION_SEARCH_ERROR_MSG, namespace, key))
          .build());
    }

    return configurationData;
  }

  @Override
  public void insertValue(String namespace, String key, String value) {
    ApplicationConfigEntity applicationConfigEntity =
        new ApplicationConfigEntity(namespace, key, value);
    applicationConfigDao.create(applicationConfigEntity);
  }

  @Override
  public Collection<ApplicationConfigEntity> getListOfConfigurationByNamespace(String namespace) {
    ApplicationConfigEntity applicationConfigEntity =
        new ApplicationConfigEntity(namespace, null, null);
    return applicationConfigDao.list(applicationConfigEntity);
  }
}
