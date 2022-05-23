/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.merge.instance;

import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.ANY_ORDER_COMMAND;

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.core.annotation.Order;

@org.springframework.stereotype.Component
@Order(ANY_ORDER_COMMAND)
public class ComponentInstanceAttributesMergeBL {

    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;

    public ComponentInstanceAttributesMergeBL(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    }

    public ActionStatus mergeComponentInstanceAttributes(List<ComponentInstanceAttribute> oldInstAttributes,
                                                         Component newComponent, String instanceId) {
        List<ComponentInstanceAttribute> newInstAttributes = newComponent.safeGetComponentInstanceAttributes(instanceId);
        if (newInstAttributes == null) {
            return ActionStatus.OK;
        }
        List<ComponentInstanceAttribute> commonInstAttributes = new ArrayList<>();
        newInstAttributes.forEach(newAttribute -> oldInstAttributes
                .stream().filter(oldAttribute -> newAttribute.getName().equals(oldAttribute.getName())).forEach(oldInstAttribute -> {
                    if ((oldInstAttribute.getValue() == null || oldInstAttribute.getValue().equals(oldInstAttribute.getDefaultValue()))
                            && newAttribute.getDefaultValue() != null) {
                        oldInstAttribute.setValue(newAttribute.getDefaultValue());
                        oldInstAttribute.setDefaultValue(newAttribute.getDefaultValue());
                    }
                    oldInstAttribute.setDescription(newAttribute.getDescription());
                    commonInstAttributes.add(oldInstAttribute);
                }));
        return updateComponentInstanceAttributes(newComponent, instanceId, commonInstAttributes);
    }

    private ActionStatus updateComponentInstanceAttributes(Component component, String instanceId, List<ComponentInstanceAttribute> newInstAttributes) {
        StorageOperationStatus storageOperationStatus = toscaOperationFacade.updateComponentInstanceAttributes(component, instanceId, newInstAttributes);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            return componentsUtils.convertFromStorageResponse(storageOperationStatus);
        }
        return ActionStatus.OK;
    }
}
