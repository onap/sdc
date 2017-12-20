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
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdcrests.applicationconfig.rest.ApplicationConfiguration;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapApplicationConfigEntityToApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapConfigurationDataToConfigurationDataDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by Talio on 8/8/2016.
 */

@Named
@Service("applicationConfiguration")
@Scope(value = "prototype")
public class ApplicationConfigurationImpl implements ApplicationConfiguration {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  @Autowired
  private ApplicationConfigManager applicationConfigManager;

  @Override
  public Response insertToTable(String namespace, String key, InputStream fileContainingSchema) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    MDC.put(LoggerConstants.SERVICE_NAME,
        LoggerServiceName.Insert_To_ApplicationConfig_Table.toString());
    String value = new String(FileUtils.toByteArray(fileContainingSchema));

    applicationConfigManager.insertIntoTable(namespace, key, value);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Response.ok().build();
  }

  @Override
  public Response getFromTable(String namespace, String key) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    MDC.put(LoggerConstants.SERVICE_NAME,
        LoggerServiceName.Get_From_ApplicationConfig_Table.toString());
    ConfigurationData value = applicationConfigManager.getFromTable(namespace, key);
    ConfigurationDataDto valueDto = new MapConfigurationDataToConfigurationDataDto()
        .applyMapping(value, ConfigurationDataDto.class);

    mdcDataDebugMessage.debugExitMessage(null, null);

    return Response.ok(valueDto).build();
  }

  @Override
  public Response getListOfConfigurationByNamespaceFromTable(String namespace) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName
        .Get_List_From_ApplicationConfig_Table_By_Namespace.toString());
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

    mdcDataDebugMessage.debugExitMessage(null, null);

    return Response.ok(applicationConfigWrapper).build();
  }
}
