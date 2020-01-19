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

package org.openecomp.sdc.be.components.scheduledtasks;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("componentsCleanBusinessLogic")
public class ComponentsCleanBusinessLogic extends BaseBusinessLogic {

    private final ResourceBusinessLogic resourceBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;

    @VisibleForTesting
    static final String DELETE_LOCKER = "DELETE_LOCKER";

    private static final Logger log = Logger.getLogger(ComponentsCleanBusinessLogic.class.getName());

    @Autowired
    public ComponentsCleanBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ResourceBusinessLogic resourceBusinessLogic,
        ServiceBusinessLogic serviceBusinessLogic,
        ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.resourceBusinessLogic = resourceBusinessLogic;
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    public Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponents(List<NodeTypeEnum> componentsToClean){
        return cleanComponents(componentsToClean, false);
    }

    public Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponents(List<NodeTypeEnum> componentsToClean, boolean isAlreadyLocked) {

        Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents = new HashMap<>();

        boolean isLockSucceeded = false;
        log.trace("start cleanComponents");
        try {
            if (!isAlreadyLocked) {
                //lock if the delete node is not locked yet
                isLockSucceeded = !isDeleteOperationLockFailed();
            }
            for (NodeTypeEnum type : componentsToClean) {
                if (!isAlreadyLocked && !isLockSucceeded) {
                    log.info("{}s won't be deleted as another process is locking the delete operation", type.getName());
                    cleanedComponents.put(type, Either.right(componentsUtils.getResponseFormat(ActionStatus.NOT_ALLOWED)));
                    break;
                }
                switch (type) {
                    case Resource:
                        processDeletionForType(cleanedComponents, NodeTypeEnum.Resource, resourceBusinessLogic);
                        break;
                    case Service:
                        processDeletionForType(cleanedComponents, NodeTypeEnum.Service, serviceBusinessLogic);
                        break;
                    default:
                        log.debug("{} component type does not have cleaning method defined", type);
                        break;
                }
            }
        }
        finally {
            if (!isAlreadyLocked && isLockSucceeded) {
                unlockDeleteOperation();
            }
        }

        log.trace("end cleanComponents");
        return cleanedComponents;
    }

    private void processDeletionForType(Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents, NodeTypeEnum type, ComponentBusinessLogic componentBusinessLogic) {
        Either<List<String>, ResponseFormat> deleteMarkedResources = componentBusinessLogic.deleteMarkedComponents();
        if (deleteMarkedResources.isRight()) {
            log.debug("failed to clean deleted components of type {}. error: {}", type, deleteMarkedResources.right().value().getFormattedMessage());
        } else {
            log.debug("list of deleted components - type {}: {}", type, deleteMarkedResources.left().value());
        }
        cleanedComponents.put(type, deleteMarkedResources);
    }

    public StorageOperationStatus lockDeleteOperation() {
        StorageOperationStatus result = graphLockOperation.lockComponentByName(DELETE_LOCKER, NodeTypeEnum.Component);
        log.info("Lock cleanup operation is done with result = {}", result);
        return result;
    }

    public StorageOperationStatus unlockDeleteOperation() {
        StorageOperationStatus result = graphLockOperation.unlockComponentByName(DELETE_LOCKER, "", NodeTypeEnum.Component);
        log.info("Unlock cleanup operation is done with result = {}", result);
        return result;
    }

    public boolean isDeleteOperationLockFailed() {
        return lockDeleteOperation() != StorageOperationStatus.OK;
    }

}
