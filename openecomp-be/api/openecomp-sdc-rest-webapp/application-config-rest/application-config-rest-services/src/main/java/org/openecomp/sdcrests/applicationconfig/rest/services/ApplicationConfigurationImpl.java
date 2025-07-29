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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdcrests.applicationconfig.rest.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import javax.inject.Named;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.applicationconfig.ApplicationConfigManager;
import org.openecomp.sdcrests.applicationconfig.rest.ApplicationConfiguration;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapApplicationConfigEntityToApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfig.rest.mapping.MapConfigurationDataToConfigurationDataDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.context.annotation.ScopedProxyMode;
/**
 * Created by Talio on 8/8/2016.
 */
@Named
@Service("applicationConfiguration")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApplicationConfigurationImpl implements ApplicationConfiguration {

    private final ApplicationConfigManager applicationConfigManager;

    @Autowired
    public ApplicationConfigurationImpl(@Qualifier("applicationConfigManager") ApplicationConfigManager applicationConfigManager) {
        this.applicationConfigManager = applicationConfigManager;
    }

    @Override
    public ResponseEntity insertToTable(String namespace, String key, MultipartFile fileContainingSchema) {
        try {
            
            String value = new String(fileContainingSchema.getBytes(), StandardCharsets.UTF_8);

            applicationConfigManager.insertIntoTable(namespace, key, value);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file content");
        }
    }

    @Override
    public ResponseEntity getFromTable(String namespace, String key) {
        ConfigurationData value = applicationConfigManager.getFromTable(namespace, key);
        ConfigurationDataDto valueDto = new MapConfigurationDataToConfigurationDataDto().applyMapping(value, ConfigurationDataDto.class);
        return ResponseEntity.ok(valueDto);
    }

    @Override
    public ResponseEntity getListOfConfigurationByNamespaceFromTable(String namespace) {
        Collection<ApplicationConfigEntity> applicationConfigEntities = applicationConfigManager.getListOfConfigurationByNamespace(namespace);
        GenericCollectionWrapper<ApplicationConfigDto> applicationConfigWrapper = new GenericCollectionWrapper<>();
        MapApplicationConfigEntityToApplicationConfigDto mapper = new MapApplicationConfigEntityToApplicationConfigDto();
        for (ApplicationConfigEntity applicationConfigEntity : applicationConfigEntities) {
            applicationConfigWrapper.add(mapper.applyMapping(applicationConfigEntity, ApplicationConfigDto.class));
        }
        return ResponseEntity.ok(applicationConfigWrapper);
    }
}
