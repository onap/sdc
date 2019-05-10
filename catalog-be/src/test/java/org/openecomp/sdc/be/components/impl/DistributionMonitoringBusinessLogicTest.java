/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.DistributionStatusListResponse;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.exception.ResponseFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DistributionMonitoringBusinessLogicTest {

    private String uId;
    private User user;
    private String ditributionId;
    private String serviceId;

    @InjectMocks
    private DistributionMonitoringBusinessLogic businessLogic;

    @Mock
    private AuditCassandraDao cassandraDao;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ComponentsUtils componentsUtils;

    @Before
    public void setUp() {
        businessLogic = new DistributionMonitoringBusinessLogic();
        MockitoAnnotations.initMocks(this);

        user = new User();
        uId = "userId";
        ditributionId = "did";
        serviceId = "serviceId";

        when(userValidations.validateUserExists(Mockito.eq(uId), eq(ditributionId), anyBoolean()))
                .thenReturn(user);
    }

    @Test
    public void testGetListOfDistribution_givenInvalidDistributionId_thenReturnsError() {
        when(cassandraDao.getListOfDistributionStatuses(ditributionId))
                .thenReturn(Either.right(ActionStatus.DISTRIBUTION_NOT_FOUND));

        assertTrue(businessLogic.getListOfDistributionStatus(ditributionId, uId).isRight());
    }

    @Test
    public void testGetListOfDistribution_givenValidDistributionId_thenReturnsSuccessful() {

        List<DistributionStatusEvent> distributionEvents = new ArrayList<>();
        DistributionStatusEvent event = new DistributionStatusEvent();
        distributionEvents.add(event);

        when(cassandraDao.getListOfDistributionStatuses(ditributionId))
                .thenReturn(Either.left(distributionEvents));

        Either<DistributionStatusListResponse, ResponseFormat> result = businessLogic.getListOfDistributionStatus(ditributionId, uId);

        assertTrue(result.isLeft());
        assertEquals(1, result.left().value().getDistributionStatusList().size());
    }

    @Test
    public void testGetDistributionServiceStatus_givenInvalidServiceIdId_thenReturnsError() {

        when(cassandraDao.getServiceDistributionStatusesList(serviceId))
                .thenReturn(Either.right(ActionStatus.DISTRIBUTION_NOT_FOUND));
        assertTrue(businessLogic.getListOfDistributionServiceStatus(serviceId, uId).isRight());
    }

    @Test
    public void testGetDistributionServiceStatus_givenValidServiceId_thenReturnsSuccessful() {

        List<AuditingGenericEvent> serviceEvents = new ArrayList<>();
        AuditingGenericEvent event1 = new AuditingGenericEvent();
        AuditingGenericEvent event2 = new AuditingGenericEvent();

        Map<String, Object> event1Fields = new HashMap<>();
        Map<String, Object> event2Fields = new HashMap<>();

        event1Fields.put("DID", "event1");
        event1Fields.put("ACTION", "DRequest");
        event1Fields.put("STATUS", "200");
        event1Fields.put("MODIFIER", uId);

        event2Fields.put("DID", "event2");
        event2Fields.put("ACTION", "DNotify");
        event2Fields.put("STATUS", "200");
        event2Fields.put("MODIFIER", uId);

        event1.setFields(event1Fields);
        event2.setFields(event2Fields);

        serviceEvents.add(event1);
        serviceEvents.add(event2);

        when(cassandraDao.getServiceDistributionStatusesList(serviceId))
                .thenReturn(Either.left(serviceEvents));

        assertTrue(businessLogic.getListOfDistributionServiceStatus(serviceId, uId).isLeft());
    }
}