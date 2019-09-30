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

package org.openecomp.sdc.tosca.datatypes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.model.AsdcModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;

/**
 * Tosca service model.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ToscaServiceModel implements AsdcModel {

    private FileContentHandler artifactFiles;
    @Getter(AccessLevel.NONE)
    private Map<String, ServiceTemplate> serviceTemplates;
    private String entryDefinitionServiceTemplate;

    /**
     * Gets service templates.
     *
     * @return the service templates
     */
    public Map<String, ServiceTemplate> getServiceTemplates() {
        return Collections.unmodifiableMap(serviceTemplates);
    }

    public Optional<ServiceTemplate> getServiceTemplate(String serviceTemplateName) {
        return MapUtils.isEmpty(this.serviceTemplates) ? Optional.empty()
                : Optional.of(this.serviceTemplates.get(serviceTemplateName));
    }

    public void addServiceTemplate(String serviceTemplateName,
                                   ServiceTemplate serviceTemplate) {
        if (MapUtils.isEmpty(serviceTemplates)) {
            serviceTemplates = new HashMap<>();
        }

        serviceTemplates.put(serviceTemplateName, serviceTemplate);
    }

    /**
     * Gets cloned service model.
     *
     * @param toscaServiceModel the tosca service model
     * @return the cloned service model
     */
    public static ToscaServiceModel getClonedServiceModel(ToscaServiceModel toscaServiceModel) {
        return ToscaServiceModel.class.cast(DataModelUtil.getClonedObject(toscaServiceModel));
    }
}
