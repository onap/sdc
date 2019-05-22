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
package org.openecomp.sdc.be.components.impl;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.jsontitan.operations.ArchiveOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ArchiveBusinessLogic {

    private static final Logger log = Logger.getLogger(ArchiveBusinessLogic.class.getName());

    private final TitanDao titanDao;
    private final AccessValidations accessValidations;
    private final ArchiveOperation archiveOperation;
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentUtils;

    public ArchiveBusinessLogic(TitanDao titanDao, AccessValidations accessValidations, ArchiveOperation archiveOperation, ToscaOperationFacade tof, ComponentsUtils componentsUtils) {
        this.titanDao = titanDao;
        this.accessValidations = accessValidations;
        this.archiveOperation = archiveOperation;
        this.toscaOperationFacade = tof;
        this.componentUtils = componentsUtils;
    }

    public void archiveComponent(String containerComponentType, String userId, String componentId) {
        User user = accessValidations.userIsAdminOrDesigner(userId, containerComponentType + "_ARCHIVE");
        Either<List<String>, ActionStatus> result = this.archiveOperation.archiveComponent(componentId);

        if (result.isRight()){
            throw new ByActionStatusComponentException(result.right().value(), componentId);
        }
        this.auditAction(ArchiveOperation.Action.ARCHIVE, result.left().value(), user, containerComponentType);
    }

    public void restoreComponent(String containerComponentType, String userId, String componentId) {
        User user = accessValidations.userIsAdminOrDesigner(userId, containerComponentType + "_RESTORE");
        Either<List<String>, ActionStatus> result = this.archiveOperation.restoreComponent(componentId);
        if (result.isRight()){
            throw new ByActionStatusComponentException(result.right().value(), componentId);
        }
        this.auditAction(ArchiveOperation.Action.RESTORE, result.left().value(), user, containerComponentType);
    }

    public List<String> onVspArchive(String userId, List<String> csarUuids){
        return this.onVspArchiveOrRestore(userId, csarUuids, ArchiveOperation.Action.ARCHIVE);
    }

    public List<String> onVspRestore(String userId, List<String> csarUuids){
        return this.onVspArchiveOrRestore(userId, csarUuids, ArchiveOperation.Action.RESTORE);
    }

    private List<String> onVspArchiveOrRestore(String userId, List<String> csarUuids, ArchiveOperation.Action action) {

        accessValidations.userIsAdminOrDesigner(userId, action.name() + "_VSP");

        ActionStatus actionStatus;
        List<String> failedCsarIDs = new LinkedList<>();

        for (String csarUuid : csarUuids) {
            try {

                if (action.equals(ArchiveOperation.Action.ARCHIVE)) {
                    actionStatus = this.archiveOperation.onVspArchived(csarUuid);
                } else {
                    actionStatus = this.archiveOperation.onVspRestored(csarUuid);
                }

                //If not found VFs with this CSAR ID we still want a success (nothing is actually done)
                if (actionStatus == ActionStatus.RESOURCE_NOT_FOUND) {
                    actionStatus = ActionStatus.OK;
                }

                if (actionStatus != ActionStatus.OK) {
                    failedCsarIDs.add(csarUuid);
                }

            } catch (Exception e) {
                log.error("Failed to handle notification: {} on VSP for csarUuid: {}", action.name(), csarUuid);
                log.error("Exception Thrown:", e);
                failedCsarIDs.add(csarUuid);
            }
        }

        return failedCsarIDs;
    }

    public Map<String, List<CatalogComponent>> getArchiveComponents(String userId, List<OriginTypeEnum> excludeTypes) {
        try {

            accessValidations.validateUserExist(userId, "GET ARCHIVED COMPONENTS");

            Either<List<CatalogComponent>, StorageOperationStatus> components = toscaOperationFacade.getCatalogOrArchiveComponents(false, excludeTypes);
            if (components.isLeft()) {
                List<CatalogComponent> comps = components.left().value();
                return comps.stream().collect(Collectors.groupingBy(cmpt -> ComponentTypeEnum.findParamByType(cmpt.getComponentType())));
            } else {
                log.info("No components found");
                return new HashMap();
            }
        } catch (Exception e){
            log.error("Error fetching archived elements", e);
            throw e;
        }
        finally {
            titanDao.commit();
        }
    }


    @VisibleForTesting
    void auditAction(ArchiveOperation.Action action, List<String> affectedCompIds, User user, String containerComponentType) {
        String comment = String.format("All versions of this component were %s", action == ArchiveOperation.Action.ARCHIVE ? "archived" : "restored");
        HashSet<String> auditDoneUUIDs = new HashSet<>();
        for (String componentId : affectedCompIds){
            Either<Component, StorageOperationStatus> result = toscaOperationFacade.getToscaElement(componentId, new ComponentParametersView());
            if (result.isRight()) {
                log.error(EcompLoggerErrorCode.DATA_ERROR, null, "GetToscaElement",
                        result.right().value().name() + "for component with id {}", componentId);
                continue;
            }
            if (auditDoneUUIDs.add(result.left().value().getUUID())) {
                //a component with this UUID is not added to audit DB/log for current archive/restore operation yet - add to audit DB now
                AuditingActionEnum auditAction = action == ArchiveOperation.Action.ARCHIVE ? AuditingActionEnum.ARCHIVE_COMPONENT : AuditingActionEnum.RESTORE_COMPONENT; //The audit Action
                result.left().foreachDoEffect(
                    c -> {
                        // The archive/restore records have been retrieved from Cassandra using the separate queries.
                        // Setting current version as null allows to avoid appearing same records in ActivityLog twice:
                        // - first time as per current version query
                        //- second type as per archive/restore query
                        c.setVersion(null);
                        componentUtils.auditComponentAdmin(componentUtils.getResponseFormat(ActionStatus.OK), user, c, auditAction, ComponentTypeEnum.findByParamName(containerComponentType), comment);
                    });
            }
        }
    }
}
