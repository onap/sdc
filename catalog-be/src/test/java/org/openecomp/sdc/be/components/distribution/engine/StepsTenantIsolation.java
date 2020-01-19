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

package org.openecomp.sdc.be.components.distribution.engine;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.iterator.DME2EndpointIterator;
import com.att.nsa.apiClient.credentials.ApiCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fj.data.Either;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.mockito.*;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.http.client.api.HttpResponse;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mockito.Mockito.*;

public class StepsTenantIsolation {

    // Notification Fields
    private String operationalEnvironmentId = "28122015552391";
    private String operationalEnvironmentName = "Operational Environment Name";
    private String operationalEnvironmentType;
    private String tenantContext ;
    private String workloadContext;
    private String action;

    @Mock
    private DmaapConsumer dmaapConsumer;
    @Mock
    private OperationalEnvironmentDao operationalEnvironmentDao;
    @Mock
    private DME2EndpointIteratorCreator epIterCreator;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private AaiRequestHandler aaiRequestHandler;
    @Mock
    private CambriaHandler cambriaHandler;
    @InjectMocks
    @Spy
    private EnvironmentsEngine envEngine;

    private boolean isSuccessful;
    private boolean cassandraUp;

    @Before
    public void beforeScenario() {
        MockitoAnnotations.initMocks(this);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED))
                .thenReturn(Either.right(CassandraOperationStatus.NOT_FOUND));
        doNothing().when(envEngine).createUebTopicsForEnvironments();
        envEngine.init();
    }

    // ############################# Given - Start #############################
    @Given("^Dmaap consumer recieved notification with fields (.*)$")
    public void dmaap_consumer_recieved_notification_with_fields(String notificationFields) throws Throwable {
        Gson gson = new GsonBuilder().create();
        IDmaapNotificationData notification = gson.fromJson(notificationFields, DmaapNotificationDataImpl.class);
        if (!isNull(notification.getOperationalEnvironmentType())) {
            this.operationalEnvironmentType = notification.getOperationalEnvironmentType().getEventTypenName();
        }
        if( !isEmpty(notification.getOperationalEnvironmentId()) ){
            this.operationalEnvironmentId = notification.getOperationalEnvironmentId();
        }
        if( !isNull(notification.getAction()) ){
            this.action = notification.getAction().getActionName();
        }

    }

    @Given("^Cassandra service status is (.*)$")
    public void cassandra_service_status_is(String status) throws Throwable {
        switch (status) {
        case "UP":
            this.cassandraUp = true;
            break;
        case "DOWN":
            when(operationalEnvironmentDao.get(operationalEnvironmentId))
                    .thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));
            when(operationalEnvironmentDao.save(Mockito.any(OperationalEnvironmentEntry.class)))
                    .thenReturn(CassandraOperationStatus.GENERAL_ERROR);
            break;
        default:
            throw new NotImplementedException();
        }
    }

    @Given("^Record status is (.*)$")
    public void record_status_is(String status) throws Throwable {
        if (!cassandraUp) {
            return;
        }
        Either<OperationalEnvironmentEntry, CassandraOperationStatus> eitherResult;
        final OperationalEnvironmentEntry entryMock = Mockito.mock(OperationalEnvironmentEntry.class);
        switch (status) {
        case "FOUND_IN_PROGRESS":
            when(entryMock.getStatus()).thenReturn(EnvironmentStatusEnum.IN_PROGRESS.getName());
            eitherResult = Either.left(entryMock);
            break;
        case "FOUND_COMPLETED":
            when(entryMock.getStatus()).thenReturn(EnvironmentStatusEnum.COMPLETED.getName());
            eitherResult = Either.left(entryMock);
            break;
        case "FOUND_FAILED":
            when(entryMock.getStatus()).thenReturn(EnvironmentStatusEnum.FAILED.getName());
            eitherResult = Either.left(entryMock);
            break;
        case "NOT_FOUND":
            eitherResult = Either.right(CassandraOperationStatus.NOT_FOUND);
            break;
        default:
            throw new NotImplementedException();
        }

        when(operationalEnvironmentDao.get(operationalEnvironmentId)).thenReturn(eitherResult);
        when(operationalEnvironmentDao.save(Mockito.any(OperationalEnvironmentEntry.class)))
                .thenReturn(CassandraOperationStatus.OK);
    }

    @Given("^AAI service status is (.*) and Tenant returned is (.*) and worload returned is (.*)$")
    public void aai_service_status_is(String aaiServiceStatus, String tenant, String workload) throws Throwable {
        this.tenantContext = tenant;
        this.workloadContext = workload;
        HttpResponse<String> resp = Mockito.mock(HttpResponse.class);
        when(aaiRequestHandler.getOperationalEnvById(operationalEnvironmentId)).thenReturn(resp);
        switch (aaiServiceStatus) {
        case "UP":
            when(resp.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            String aaiResponseTemplate =
                    //@formatter:off
                    "{\r\n"
                    + "     \"operational-environment-id\": \"%s\",\r\n"
                    + "     \"operational-environment-name\": \"%s\",\r\n"
                    + "     \"operational-environment-type\": \"%s\",\r\n"
                    + "     \"operational-environment-status\": \"IN-PROGRESS\",\r\n"
                    + "     \"tenant-context\": \"%s\",\r\n"
                    + "     \"workload-context\": \"%s\"\r\n"
                    + "    }";
                    //@formatter:on
            when(resp.getResponse()).thenReturn(String.format(aaiResponseTemplate, operationalEnvironmentId,
                    operationalEnvironmentName, operationalEnvironmentType, tenantContext, workloadContext));

            break;
        case "DOWN":
            when(resp.getStatusCode()).thenReturn(HttpStatus.SC_REQUEST_TIMEOUT);
            break;
        default:
            throw new NotImplementedException();
        }
    }

    @Given("^AFT_DME service status is (.*)$")
    public void aft_dme_service_status_is(String aftDmeStatus) throws Throwable {
        switch (aftDmeStatus) {
        case "UP":
            DME2EndpointIterator mockItr = Mockito.mock(DME2EndpointIterator.class);
            when(mockItr.hasNext()).thenReturn(false);
            when(epIterCreator.create(Mockito.anyString())).thenReturn(mockItr);
            break;
        case "DOWN":
            when(epIterCreator.create(Mockito.anyString()))
                    .thenThrow(new DME2Exception("dummyCode", new NotImplementedException()));
            break;
        default:
            throw new NotImplementedException();
        }
    }

    @SuppressWarnings("unchecked")
    @Given("^UEB service status is (.*)$")
    public void ueb_service_status_is(String status) throws Throwable {

        Either<ApiCredential, CambriaErrorResponse> response;
        switch (status) {
        case "UP":
            ApiCredential apiCredential = Mockito.mock(ApiCredential.class);
            when(apiCredential.getApiKey()).thenReturn("MockAPIKey");
            when(apiCredential.getApiSecret()).thenReturn("MockSecretKey");
            response = Either.left(apiCredential);
            break;
        case "DOWN":
            CambriaErrorResponse cambriaError = Mockito.mock(CambriaErrorResponse.class);
            response = Either.right(cambriaError);
            break;
        default:
            throw new NotImplementedException();
        }
        when(cambriaHandler.createUebKeys(Mockito.anyList())).thenReturn(response);
    }
    // ############################# Given - End #############################

    // ############################# When - Start #############################

    @When("^handle message is activated$")
    public void handle_message_is_activated() throws Throwable {
        this.isSuccessful = envEngine.handleMessage(buildNotification());
    }
    // ############################# When - End #############################

    // ############################# Then - Start #############################
    @SuppressWarnings("unchecked")
    @Then("^handle message activates validation of eventType (.*)$")
    public void handle_message_activates_validation_of_eventType(boolean isValidated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isValidated)))
                .validateEnvironmentType(Mockito.any(Wrapper.class), Mockito.any(IDmaapNotificationData.class),
                        Mockito.any(IDmaapAuditNotificationData.class));
    }

    @SuppressWarnings("unchecked")
    @Then("^trying to write message to audit log and table (.*)$")
    public void trying_to_write_message_to_audit_log_and_table(boolean isUnsupportedTypeEventRecorded) throws Throwable {
        int count = isUnsupportedTypeEventRecorded ? 2 : 1;
        verify(componentsUtils, Mockito.atLeast(count))
                .auditEnvironmentEngine(Mockito.any(AuditingActionEnum.class), Mockito.eq(operationalEnvironmentId),
                        Mockito.any(String.class), Mockito.any(String.class), Mockito.eq(operationalEnvironmentName), Mockito.eq(tenantContext));
    }

    @SuppressWarnings("unchecked")
    @Then("^handle message activates validation of action (.*)$")
    public void handle_message_activates_validation_of_action(boolean isValidated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isValidated)))
                .validateActionType(Mockito.any(Wrapper.class), Mockito.any(IDmaapNotificationData.class));
    }

    @SuppressWarnings("unchecked")
    @Then("^handle message activates validation of state (.*)$")
    public void handle_message_activates_validation_of_state(boolean isValidated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isValidated)))
                .validateState(Mockito.any(Wrapper.class), Mockito.any(IDmaapNotificationData.class));
    }

    @SuppressWarnings("unchecked")
    @Then("^trying to save in-progress record (.*)$")
    public void trying_to_save_in_progress_record(boolean isActivated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isActivated)))
                .saveEntryWithInProgressStatus(Mockito.any(Wrapper.class), Mockito.any(Wrapper.class), Mockito.any(IDmaapNotificationData.class));
    }

    @SuppressWarnings("unchecked")
    @Then("^trying to get environment info from A&AI API (.*)$")
    public void trying_to_get_environment_info_from_AAI_AP(boolean isActivated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isActivated)))
                .retrieveOpEnvInfoFromAAI(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));
    }

    @SuppressWarnings("unchecked")
    @Then("^trying to retrieve Ueb Addresses From AftDme (.*)$")
    public void trying_to_retrieve_ueb_addresses_from_AftDme(boolean isActivated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isActivated))).discoverUebHosts(
                Mockito.anyString());

    }

    @SuppressWarnings("unchecked")
    @Then("^trying to create Ueb keys (.*)$")
    public void trying_to_create_ueb_keys(boolean isActivated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isActivated)))
                .createUebKeys(Mockito.any(Wrapper.class), Mockito.any(OperationalEnvironmentEntry.class));
    }

    @Then("^trying to create Ueb Topics (.*)$")
    public void trying_to_create_ueb_topics(boolean isActivated) throws Throwable {
        verify(envEngine, Mockito.times(getNumberOfCallsToValidate(isActivated)))
                .createUebTopicsForEnvironment(Mockito.any(OperationalEnvironmentEntry.class));
    }

    @Then("^handle message finished successfully (.*)$")
    public void handle_message_finished_successfully(boolean isSuccessfull) throws Throwable {
        Assert.assertEquals(this.isSuccessful, isSuccessfull);
    }

    // ############################# Then - End #############################

    private String buildNotification() {
        String notificationTemplate = "{ \"operationalEnvironmentId\": \"%s\",\r\n"
                + "             \"operationalEnvironmentName\": \"%s\",\r\n"
                + "             \"operationalEnvironmentType\": \"%s\",\r\n" + "             \"tenantContext\": \"%s\",\r\n"
                + "             \"workloadContext\": \"%s\",\r\n" + "             \"action\": \"%s\"}";

        return String.format(notificationTemplate, operationalEnvironmentId, operationalEnvironmentName,
                operationalEnvironmentType, tenantContext, workloadContext, action);
    }

    private int getNumberOfCallsToValidate(boolean isValidated) {
        return isValidated ? NumberUtils.INTEGER_ONE : NumberUtils.INTEGER_ZERO;
    }

}
