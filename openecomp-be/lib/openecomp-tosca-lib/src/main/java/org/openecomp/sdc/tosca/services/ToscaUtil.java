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
package org.openecomp.sdc.tosca.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileUtils;

/**
 * The type Tosca util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ToscaUtil {

    /**
     * Gets service template file name.
     *
     * @param serviceTemplate the service template
     * @return the service template file name
     */
    public static String getServiceTemplateFileName(ServiceTemplate serviceTemplate) {
        if (serviceTemplate == null) {
            return null;
        }
        if (serviceTemplate.getMetadata() == null) {
            return UUID.randomUUID().toString() + ToscaConstants.SERVICE_TEMPLATE_FILE_POSTFIX;
        }
        return getServiceTemplateFileName(serviceTemplate.getMetadata());
    }

    /**
     * Gets service template file name.
     *
     * @param metadata the file name
     * @return the service template file name
     */
    public static String getServiceTemplateFileName(Map<String, String> metadata) {
        if (metadata.get(ToscaConstants.ST_METADATA_FILE_NAME) != null) {
            return metadata.get(ToscaConstants.ST_METADATA_FILE_NAME);
        } else if (metadata.get(ToscaConstants.ST_METADATA_TEMPLATE_NAME) != null) {
            return metadata.get(ToscaConstants.ST_METADATA_TEMPLATE_NAME) + ToscaConstants.SERVICE_TEMPLATE_FILE_POSTFIX;
        }
        return UUID.randomUUID().toString() + ToscaConstants.SERVICE_TEMPLATE_FILE_POSTFIX;
    }

    public static String getServiceTemplateFileName(String templateName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, templateName);
        return getServiceTemplateFileName(metadata);
    }

    public static Optional<String> getSubstitutableGroupMemberId(String heatFileName, ServiceTemplate serviceTemplate) {
        Map<String, NodeTemplate> nodeTemplates = DataModelUtil.getNodeTemplates(serviceTemplate);
        if (MapUtils.isEmpty(nodeTemplates)) {
            return Optional.empty();
        }
        String heatFileNameWithoutExt = FileUtils.getFileWithoutExtention(heatFileName);
        for (Map.Entry<String, NodeTemplate> nodeTemplateEntry : nodeTemplates.entrySet()) {
            String subServiceTemplateName = getSubstitutionServiceTemplateNameFromProperties(nodeTemplateEntry);
            if (Objects.nonNull(subServiceTemplateName) && isGroupMemberIdSubstitutable(heatFileNameWithoutExt, subServiceTemplateName)) {
                return Optional.of(nodeTemplateEntry.getKey());
            }
        }
        return Optional.empty();
    }

    private static boolean isGroupMemberIdSubstitutable(String heatFileNameWithoutExt, String subServiceTemplateName) {
        return subServiceTemplateName.startsWith(heatFileNameWithoutExt);
    }

    private static String getSubstitutionServiceTemplateNameFromProperties(Map.Entry<String, NodeTemplate> nodeTemplateEntry) {
        Map<String, Object> properties =
            nodeTemplateEntry.getValue().getProperties() == null ? Collections.emptyMap() : nodeTemplateEntry.getValue().getProperties();
        Map<String, Object> serviceTemplateFilter =
            properties.containsKey(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME) ? (Map<String, Object>) properties
                .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME) : Collections.emptyMap();
        return (String) serviceTemplateFilter.get(ToscaConstants.SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME);
    }

    /**
     * Add service template to map with key file name.
     *
     * @param serviceTemplateMap the service template map
     * @param serviceTemplate    the service template
     */
    public static void addServiceTemplateToMapWithKeyFileName(Map<String, ServiceTemplate> serviceTemplateMap, ServiceTemplate serviceTemplate) {
        serviceTemplateMap.put(ToscaUtil.getServiceTemplateFileName(serviceTemplate), serviceTemplate);
    }
}
