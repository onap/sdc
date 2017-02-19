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

package org.openecomp.sdcrests.applicationconfig.rest.services;

import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.applicationconfig.ApplicationConfigManager;
import org.openecomp.sdcrests.applicationconfig.rest.ApplicationConfiguration;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapApplicationConfigEntityToApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapConfigurationDataToConfigurationDataDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import javax.inject.Named;

import javax.ws.rs.core.Response;

@Named
@Service("applicationConfiguration")
@Scope(value = "prototype")
public class ApplicationConfigurationImpl implements ApplicationConfiguration {

  @Autowired
  private ApplicationConfigManager applicationConfigManager;


  @Override
  public Response insertToTable(String namespace, String key, InputStream fileContainingSchema) {
    String value = new String(FileUtils.toByteArray(fileContainingSchema));

    applicationConfigManager.insertIntoTable(namespace, key, value);

    return Response.ok().build();
  }

  @Override
  public Response getFromTable(String namespace, String key) {
    ConfigurationData value = applicationConfigManager.getFromTable(namespace, key);
    ConfigurationDataDto valueDto = new MapConfigurationDataToConfigurationDataDto()
        .applyMapping(value, ConfigurationDataDto.class);

    return Response.ok(valueDto).build();
  }

  @Override
  public Response getListOfConfigurationByNamespaceFromTable(String namespace) {
    Collection<ApplicationConfigEntity> applicationConfigEntities =
        applicationConfigManager.getListOfConfigurationByNamespace(namespace);
    GenericCollectionWrapper<ApplicationConfigDto> applicationConfigWrapper =
        new GenericCollectionWrapper<>();
    MapApplicationConfigEntityToApplicationConfigDto mapper =
        new MapApplicationConfigEntityToApplicationConfigDto();

    for (ApplicationConfigEntity applicationConfigEntity : applicationConfigEntities) {
      applicationConfigWrapper
          .add(mapper.applyMapping(applicationConfigEntity, ApplicationConfigDto.class));
    }

    return Response.ok(applicationConfigWrapper).build();
  }
}
