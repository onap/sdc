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

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.info.DistributionStatusInfo;
import org.openecomp.sdc.be.info.DistributionStatusListResponse;
import org.openecomp.sdc.be.info.DistributionStatusOfServiceInfo;
import org.openecomp.sdc.be.info.DistributionStatusOfServiceListResponce;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("distributionMonitoringBusinessLogic")
public class DistributionMonitoringBusinessLogic extends BaseBusinessLogic {
    private static final String DEPLOYED = "Deployed";

    private static final String ERROR = "Error";

    private static final String DISTRIBUTED = "Distributed";

    private static final String IN_PROGRESS = "In Progress";

    private static final Logger log = Logger.getLogger(ArtifactsBusinessLogic.class.getName());

    private final AuditCassandraDao cassandraDao;

    @Autowired
    public DistributionMonitoringBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
        AuditCassandraDao cassandraDao,
        ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.cassandraDao = cassandraDao;
    }

    public Either<DistributionStatusListResponse, ResponseFormat> getListOfDistributionStatus(String did, String userId) {

        validateUserExists(userId);

        log.trace("getListOfDistributionStatus for did {}", did);
        Either<List<DistributionStatusEvent>, ActionStatus> distributionStatus = cassandraDao.getListOfDistributionStatuses(did);
        if (distributionStatus.isRight()) {
            log.debug("not found distribution statuses for did {}   status is {} ", did, distributionStatus.right().value());
            return Either.right(componentsUtils.getResponseFormat(distributionStatus.right().value(), did));
        }
        List<DistributionStatusInfo> distribStatusInfoList = new ArrayList<>();
        List<DistributionStatusEvent> distributionStatusEventList = distributionStatus.left().value();
        if (distributionStatusEventList != null) {
            for (AuditingGenericEvent distributionStatusEvent : distributionStatusEventList) {
                distribStatusInfoList.add(new DistributionStatusInfo(distributionStatusEvent));
            }
        }

        DistributionStatusListResponse distributionStatusListResponse = new DistributionStatusListResponse();
        distributionStatusListResponse.setDistributionStatusList(distribStatusInfoList);
        log.trace("list statuses for did {} is {} ", did, distribStatusInfoList);
        return Either.left(distributionStatusListResponse);
    }

    public Either<DistributionStatusOfServiceListResponce, ResponseFormat> getListOfDistributionServiceStatus(String serviceUuid, String userId) {
        validateUserExists(userId);

        log.trace("getListOfDistributionServiceStatus for serviceUUID {}", serviceUuid);
        Either<List<? extends AuditingGenericEvent>, ActionStatus> status = cassandraDao.getServiceDistributionStatusesList(serviceUuid);
        if (status.isRight()) {
            log.debug("failed to find service distribution statuses. error: {}", status);
            return Either.right(componentsUtils.getResponseFormat(status.right().value(), serviceUuid));
        }
        List<DistributionStatusOfServiceInfo> distribStatusInfoList;
        List<? extends AuditingGenericEvent> distributionStatusEventList = status.left().value();
        distribStatusInfoList = handleAuditingDaoResponse(distributionStatusEventList);
        DistributionStatusOfServiceListResponce distributionStatusListResponse = new DistributionStatusOfServiceListResponce();
        distributionStatusListResponse.setDistributionStatusOfServiceList(distribStatusInfoList);
        return Either.left(distributionStatusListResponse);
    }

    private List<DistributionStatusOfServiceInfo> handleAuditingDaoResponse(List<? extends AuditingGenericEvent> distribStatusInfoList) {
        List<DistributionStatusOfServiceInfo> reslist = new ArrayList<>();
        Map<String, List<AuditingGenericEvent>> serviceDidMap = createServiceDidMap(distribStatusInfoList);
        Set<String> didSet = serviceDidMap.keySet();
        for (String did : didSet) {
            DistributionStatusOfServiceInfo distributionStatusOfServiceInfo = new DistributionStatusOfServiceInfo();
            distributionStatusOfServiceInfo.setDistributionID(did);
            String dReguestStatus = "";
            String dNotifyStatus = "";
            boolean isResult = false;
            List<? extends AuditingGenericEvent> auditingGenericEventList = serviceDidMap.get(did);
            AuditingGenericEvent resAuditingGenericEvent = null;
            for (AuditingGenericEvent auditingGenericEvent : auditingGenericEventList) {
                auditingGenericEvent.fillFields();

                String action = (String) auditingGenericEvent.getFields().get(AuditingFieldsKey.AUDIT_ACTION.getDisplayName());
                Object modifierUserId = auditingGenericEvent.getFields().get(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName());
                if (modifierUserId != null) {
                    distributionStatusOfServiceInfo.setUserId((String) modifierUserId);
                }

                if (action.equals(AuditingActionEnum.DISTRIBUTION_DEPLOY.getName())) {

                    isResult = true;
                    resAuditingGenericEvent = auditingGenericEvent;
                    break;
                } else if (action.equals(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST.getName())) {
                    dReguestStatus = getStatusFromAuditEvent(auditingGenericEvent);
                } else if (action.equals(AuditingActionEnum.DISTRIBUTION_NOTIFY.getName())) {
                    dNotifyStatus = getStatusFromAuditEvent(auditingGenericEvent);
                }

                resAuditingGenericEvent = auditingGenericEvent;

            }
            distributionStatusOfServiceInfo.setTimestamp((String) resAuditingGenericEvent.getFields().get(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName()));

            if (!isResult) {
                if (dReguestStatus.equals(String.valueOf(HttpStatus.SC_OK))) {
                    if (dNotifyStatus.isEmpty()) {
                        distributionStatusOfServiceInfo.setDeployementStatus(IN_PROGRESS);

                    } else {
                        if (dNotifyStatus.equals(String.valueOf(HttpStatus.SC_OK)))
                            distributionStatusOfServiceInfo.setDeployementStatus(DISTRIBUTED);
                        else
                            distributionStatusOfServiceInfo.setDeployementStatus(ERROR);
                    }
                } else
                    distributionStatusOfServiceInfo.setDeployementStatus(ERROR);
            } else
                distributionStatusOfServiceInfo.setDeployementStatus(DEPLOYED);
            reslist.add(distributionStatusOfServiceInfo);
        }

        return reslist;
    }

    private String getStatusFromAuditEvent(AuditingGenericEvent auditingGenericEvent) {
        String status = "";
        Object requestStatus = auditingGenericEvent.getFields().get(AuditingFieldsKey.AUDIT_STATUS.getDisplayName());
        if (requestStatus instanceof String) {
            status = (String) requestStatus;
        }
        return status;
    }

    private Map<String, List<AuditingGenericEvent>> createServiceDidMap(List<? extends AuditingGenericEvent> distribStatusInfoList) {

        Map<String, List<AuditingGenericEvent>> serviceDidMap = new HashMap<>();
        for (AuditingGenericEvent auditingGenericEvent : distribStatusInfoList) {
            List<AuditingGenericEvent> auditingGenericEventList = null;
            String did = "";
            auditingGenericEvent.fillFields();

            Object didValue = auditingGenericEvent.getFields().get(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID.getDisplayName());
            if (didValue != null) {
                did = (String) didValue;
            }

            if (!did.isEmpty()) {
                if (serviceDidMap.containsKey(did)) {
                    auditingGenericEventList = serviceDidMap.get(did);
                }
                if (auditingGenericEventList == null) {
                    auditingGenericEventList = new ArrayList<>();

                }
                auditingGenericEventList.add(auditingGenericEvent);
                serviceDidMap.put(did, auditingGenericEventList);
            }
        }
        return serviceDidMap;
    }

}
