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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.javatuples.Pair;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ComponentInstanceForwardingPathMerge implements ComponentInstanceMergeInterface {

    private static Logger log = Logger.getLogger(ComponentInstanceForwardingPathMerge.class);

    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Autowired
    private ComponentsUtils componentsUtils;

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent,
        ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigInstanceCapabilities(getAllInstanceCapabilities(currentResourceInstance));
        dataHolder.setOrigInstanceNode(originComponent);
        dataHolder.setOrigComponentInstId(currentResourceInstance.getName());
    }

    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder,
                                          Component updatedContainerComponent, String newInstanceId) {
        if (!(updatedContainerComponent instanceof Service)) {
            // no need to handle forwarding paths
            return updatedContainerComponent;
        }
        Service service = (Service) updatedContainerComponent;
        ComponentInstance ci = service.getComponentInstanceById(newInstanceId).orElse(null);
        if (ci == null){
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER, newInstanceId);
        }
        Either<Component, StorageOperationStatus> resourceEither = toscaOperationFacade.getToscaFullElement(ci.getComponentUid());
        if (resourceEither.isRight() ) {
            log.debug("Failed to fetch resource with id {} for instance {}",ci.getComponentUid() ,ci.getUniqueId());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(resourceEither.right().value()));
        }

        Component fetchedComponent = resourceEither.left().value();

        Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> pair = new ForwardingPathUtils()
            .updateForwardingPathOnVersionChange(service, dataHolder, fetchedComponent, newInstanceId);
        Map<String, ForwardingPathDataDefinition> updated = pair.getValue0();
        Map<String, ForwardingPathDataDefinition> deleted = pair.getValue1();
        if (deleted != null && !deleted.isEmpty()) {
            Set<String> deleteEither = serviceBusinessLogic
                .deleteForwardingPaths(service.getUniqueId(), new HashSet<>(deleted.keySet()), user, false);
            deleted.keySet().forEach(key -> service.getForwardingPaths().remove(key));
        }
        if (updated != null && !updated.isEmpty()) {
            Service updateFPService = new Service();
            updateFPService.setForwardingPaths(updated);
            Service updateFPEither = serviceBusinessLogic
                .updateForwardingPath(service.getUniqueId(), updateFPService, user, false);
            updated.forEach((key, forwardingPathDataDefinition) -> service.getForwardingPaths().put(key,forwardingPathDataDefinition));
        }
        return updatedContainerComponent;
    }


    private List<CapabilityDefinition> getAllInstanceCapabilities(ComponentInstance currentResourceInstance) {
        if(currentResourceInstance.getCapabilities() == null || currentResourceInstance.getCapabilities().isEmpty()){
            return Collections.EMPTY_LIST;
        }
        return currentResourceInstance.getCapabilities().values().stream().flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
