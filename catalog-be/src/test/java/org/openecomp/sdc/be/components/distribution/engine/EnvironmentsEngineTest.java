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

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.info.OperationalEnvInfo;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.http.client.api.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class EnvironmentsEngineTest {

    @InjectMocks
    private EnvironmentsEngine envEngine;
    @Mock
    private OperationalEnvironmentDao operationalEnvironmentDao;
    @Mock
    private ConfigurationManager configurationManager;
    @Mock
    private DistributionEngineConfiguration distributionEngineConfiguration;
    @Mock
    private AaiRequestHandler aaiRequestHandler;

    @Before
    public void preStart() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInit() {
        envEngine.setConfigurationManager(configurationManager);
        Configuration config = Mockito.mock(Configuration.class);
        DmaapConsumerConfiguration dmaapConf = Mockito.mock(DmaapConsumerConfiguration.class);
        List<OperationalEnvironmentEntry> entryList = Arrays.asList(createOpEnvEntry("Env1"), createOpEnvEntry("Env2"));
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> successEither = Either.left(entryList);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(successEither);
        when(configurationManager.getDistributionEngineConfiguration()).thenReturn(distributionEngineConfiguration);
        when(distributionEngineConfiguration.getEnvironments()).thenReturn(Collections.singletonList("Env Loaded From Configuration"));
        when(distributionEngineConfiguration.getUebPublicKey()).thenReturn("Dummy Public Key");
        when(distributionEngineConfiguration.getUebSecretKey()).thenReturn("Dummy Private Key");
        when(distributionEngineConfiguration.getUebServers()).thenReturn(
                Arrays.asList("uebsb91kcdc.it.com:3904", "uebsb92kcdc.it.com:3904", "uebsb91kcdc.it.com:3904"));
        when(configurationManager.getConfiguration()).thenReturn(config);
        when(config.getDmaapConsumerConfiguration()).thenReturn(dmaapConf);
        when(dmaapConf.isActive()).thenReturn(false);
        envEngine.init();

        Map<String, OperationalEnvironmentEntry> mapEnvs = envEngine.getEnvironments();
        assertEquals("unexpected size of map",3, mapEnvs.size());
    }


    @Test
    public void testGetFullOperationalEnvByIdSuccess() {
        String json = getFullOperationalEnvJson();

        HttpResponse restResponse = new HttpResponse(json, HttpStatus.SC_OK, "Successfully completed");
        when(aaiRequestHandler.getOperationalEnvById(Mockito.anyString())).thenReturn(restResponse);

        Either<OperationalEnvInfo, Integer> response = envEngine.getOperationalEnvById("DummyId");
        assertTrue("The operational environment request ran as not expected", response.isLeft());

        OperationalEnvInfo operationalEnvInfo = response.left().value();

        assertEquals("The operational environment json is not as expected", operationalEnvInfo.toString(), json);
    }

    @Test
    public void testGetPartialOperationalEnvByIdSuccess() {
        String json = getPartialOperationalEnvJson();

        HttpResponse<String> restResponse = new HttpResponse<String>(json, HttpStatus.SC_OK, "Successfully completed");
        when(aaiRequestHandler.getOperationalEnvById(Mockito.anyString())).thenReturn(restResponse);

        Either<OperationalEnvInfo, Integer> response = envEngine.getOperationalEnvById("DummyId");
        assertTrue("The operational environment request ran as not expected", response.isLeft());

        OperationalEnvInfo operationalEnvInfo = response.left().value();

        assertEquals("The operational environment json is not as expected", operationalEnvInfo.toString(), json);
    }


    @Test
    public void testGetOperationalEnvByIdFailedByJsonConvert() {
        String jsonCorrupted = getCorruptedOperationalEnvJson();

        HttpResponse<String> restResponse = new HttpResponse<String>(jsonCorrupted, HttpStatus.SC_OK, "Successfully Completed");
        when(aaiRequestHandler.getOperationalEnvById(Mockito.anyString())).thenReturn(restResponse);

        Either<OperationalEnvInfo, Integer> response = envEngine.getOperationalEnvById("DummyId");
        assertTrue("The operational environment request ran as not expected", response.isRight());
        assertEquals("The operational environment request status code is not as expected", (Integer)HttpStatus.SC_INTERNAL_SERVER_ERROR, response.right().value());
    }

    @Test
    public void testGetOperationalEnvByIdFailed404() {
        String json = getFullOperationalEnvJson();
        HttpResponse<String> restResponse = new HttpResponse<String>(json, HttpStatus.SC_NOT_FOUND, "Not Found");
        when(aaiRequestHandler.getOperationalEnvById(Mockito.anyString())).thenReturn(restResponse);

        Either<OperationalEnvInfo, Integer> response = envEngine.getOperationalEnvById("DummyId");
        assertTrue("The operational environment request ran as not expected", response.isRight());
        assertEquals("The operational environment request status code is not as expected", (Integer)HttpStatus.SC_NOT_FOUND, response.right().value());
    }


    @Test(expected = IOException.class)
    public void testCorruptedOperationalEnvJson() throws IOException {
        String jsonCorrupted = getCorruptedOperationalEnvJson();
        OperationalEnvInfo.createFromJson(jsonCorrupted);
    }

    @Test
    public void getEnvironmentById() {
        OperationalEnvironmentEntry oe = new OperationalEnvironmentEntry();
        oe.setEnvironmentId("mock");
        envEngine.addToMap(oe);
        assertTrue(envEngine.isInMap("mock"));
        assertTrue(envEngine.isInMap(oe));
        OperationalEnvironmentEntry returnedOe = envEngine.getEnvironmentById("mock");
        assertTrue(oe == returnedOe);
    }

    @Test
    public void getEnvironmentByDmaapUebAddressNoProperEnvironment() {
        OperationalEnvironmentEntry opEnvEntry = createOpEnvEntry("1");
        opEnvEntry.setDmaapUebAddress(new HashSet<>());
        envEngine.addToMap(opEnvEntry);
        assertThatThrownBy(() -> {
            envEngine.getEnvironmentByDmaapUebAddress(Arrays.asList("11", "22"));})
                .isInstanceOf(ComponentException.class);
    }

    @Test
    public void getEnvironmentByDmaapUebAddressListWithEmptyList() {
        OperationalEnvironmentEntry opEnvEntry = createOpEnvEntry("1");
        opEnvEntry.setDmaapUebAddress(new HashSet<>(Arrays.asList("11","22")));
        OperationalEnvironmentEntry opEnvEntry2 = createOpEnvEntry("2");
        opEnvEntry2.setDmaapUebAddress(new HashSet<>(Arrays.asList("33","44","55")));
        envEngine.addToMap(opEnvEntry);
        envEngine.addToMap(opEnvEntry2);
        assertThatThrownBy(() -> {
            envEngine.getEnvironmentByDmaapUebAddress(new ArrayList<>());})
                .isInstanceOf(ComponentException.class);
    }

    @Test
    public void getEnvironmentByDmaapUebAddressList() {
        OperationalEnvironmentEntry opEnvEntry = createOpEnvEntry("1");
        opEnvEntry.setDmaapUebAddress(new HashSet<>(Arrays.asList("11","22")));
        OperationalEnvironmentEntry opEnvEntry2 = createOpEnvEntry("2");
        opEnvEntry2.setDmaapUebAddress(new HashSet<>(Arrays.asList("33","44","55")));
        envEngine.addToMap(opEnvEntry);
        envEngine.addToMap(opEnvEntry2);
        assertThat(envEngine.getEnvironmentByDmaapUebAddress(Arrays.asList("77","22"))
                .getEnvironmentId()).isEqualTo("1");

        assertThat(envEngine.getEnvironmentByDmaapUebAddress(Arrays.asList("77","55"))
                .getEnvironmentId()).isEqualTo("2");

        assertThat(envEngine.getEnvironmentByDmaapUebAddress(Arrays.asList("11","44"))
                .getEnvironmentId()).isEqualTo("1");
    }




    private String getCorruptedOperationalEnvJson() {
        return "{\"OPERATIONAL-environment-name\":\"Op Env Name\","
                + "\"OPERATIONAL-environment-type\":\"VNF\","
                + "\"OPERATIONAL-environment-status\":\"Activate\","
                + "\"tenant-context\":\"Test\"}";
    }

    private String getPartialOperationalEnvJson() {
        return "{" +
                "\"operational-environment-id\":\"UUID of Operational Environment\"," +
                "\"operational-environment-name\":\"Op Env Name\"," +
                "\"operational-environment-type\":\"VNF\"," +
                "\"operational-environment-status\":\"Activate\"," +
                "\"tenant-context\":\"Test\"," +
                "\"workload-context\":\"VNF_Development\"," +
                "\"resource-version\":\"1505228226913\"," +
                "\"relationship-list\":{" +
                "\"relationship\":[]" +
                "}" +
                "}";
    }

    private String getFullOperationalEnvJson() {
        return  "{" +
                "\"operational-environment-id\":\"OEid1\"," +
                "\"operational-environment-name\":\"OEname1\"," +
                "\"operational-environment-type\":\"OEtype1\"," +
                "\"operational-environment-status\":\"OEstatus1\"," +
                "\"tenant-context\":\"OEtenantcontext1\"," +
                "\"workload-context\":\"OEworkloadcontext1\"," +
                "\"resource-version\":\"1511363173278\"," +
                "\"relationship-list\":{" +
                "\"relationship\":[" +
                "{" +
                "\"related-to\":\"operational-environment\"," +
                "\"relationship-label\":\"managedBy\"," +
                "\"related-link\":\"/aai/v12/cloud-infrastructure/operational-environments/operational-environment/OEid3\"," +
                "\"relationship-data\":[" +
                "{" +
                "\"relationship-key\":\"operational-environment.operational-environment-id\"," +
                "\"relationship-value\":\"OEid3\"" +
                "}" +
                "]," +
                "\"related-to-property\":[" +
                "{" +
                "\"property-key\":\"operational-environment.operational-environment-name\"," +
                "\"property-value\":\"OEname3\"" +
                "}]}]}}";
    }

    private OperationalEnvironmentEntry createOpEnvEntry(String name) {
        OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
        entry.setEnvironmentId(name);
        return entry;
    }

    public void testHandleMessageLogic() throws Exception {
        String notification = "";
        boolean result;

        // default test
        result = envEngine.handleMessageLogic(notification);
    }

    @Test
    public void testValidateNotification() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        errorWrapper.setInnerElement(true);
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
        IDmaapAuditNotificationData auditNotificationData = Mockito.mock(IDmaapAuditNotificationData.class);

        // default test
        Deencapsulation.invoke(envEngine, "validateNotification", errorWrapper, notificationData,
                auditNotificationData);
    }

    @Test
    public void testSaveEntryWithFailedStatus() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

        // default test
        Deencapsulation.invoke(envEngine, "saveEntryWithFailedStatus", errorWrapper, opEnvEntry);
    }

    @Test
    public void testRetrieveUebAddressesFromAftDme() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

        // default test
        Deencapsulation.invoke(envEngine, "retrieveUebAddressesFromAftDme", errorWrapper, opEnvEntry);
    }

    @Test
    public void testRetrieveOpEnvInfoFromAAI() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
        opEnvEntry.setEnvironmentId("mock");
        Mockito.when(aaiRequestHandler.getOperationalEnvById(Mockito.nullable(String.class))).thenReturn(new HttpResponse<String>("{}", 200));
        // default test
        Deencapsulation.invoke(envEngine, "retrieveOpEnvInfoFromAAI", new Wrapper<>(), opEnvEntry);
    }

    @Test
    public void testRetrieveOpEnvInfoFromAAIError() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
        opEnvEntry.setEnvironmentId("mock");
        Mockito.when(aaiRequestHandler.getOperationalEnvById(Mockito.nullable(String.class))).thenReturn(new HttpResponse<String>("{}", 500));
        // default test
        Deencapsulation.invoke(envEngine, "retrieveOpEnvInfoFromAAI", new Wrapper<>(), opEnvEntry);
    }

    @Test
    public void testSaveEntryWithInProgressStatus() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        Wrapper<OperationalEnvironmentEntry> opEnvEntryWrapper = new Wrapper<>();
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

        Deencapsulation.invoke(envEngine, "saveEntryWithInProgressStatus", errorWrapper, opEnvEntryWrapper,
                notificationData);
    }

    @Test
    public void testValidateStateGeneralError() throws Exception {
        Wrapper<Boolean> errorWrapper = null;
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

        Mockito.when(operationalEnvironmentDao.get(Mockito.nullable(String.class)))
                .thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

        Deencapsulation.invoke(envEngine, "validateState", new Wrapper<>(), notificationData);
    }

    @Test
    public void testValidateState() throws Exception {
        Wrapper<Boolean> errorWrapper = null;
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

        OperationalEnvironmentEntry a = new OperationalEnvironmentEntry();
        a.setStatus(EnvironmentStatusEnum.IN_PROGRESS.getName());
        Mockito.when(operationalEnvironmentDao.get(Mockito.nullable(String.class))).thenReturn(Either.left(a));

        Deencapsulation.invoke(envEngine, "validateState", new Wrapper<>(), notificationData);
    }

    @Test
    public void testValidateActionType() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
        Mockito.when(notificationData.getAction()).thenReturn(IDmaapNotificationData.DmaapActionEnum.DELETE);

        Deencapsulation.invoke(envEngine, "validateActionType", errorWrapper, notificationData);
    }

    @Test
    public void testValidateActionType2() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
        Mockito.when(notificationData.getAction()).thenReturn(IDmaapNotificationData.DmaapActionEnum.CREATE);

        Deencapsulation.invoke(envEngine, "validateActionType", errorWrapper, notificationData);
    }

    @Test
    public void testValidateEnvironmentType() throws Exception {
        Wrapper<Boolean> errorWrapper = new Wrapper<>();
        IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
        IDmaapAuditNotificationData auditNotificationData = Mockito.mock(IDmaapAuditNotificationData.class);
        Mockito.when(auditNotificationData.getOperationalEnvironmentName()).thenReturn("mock");
        Mockito.when(notificationData.getOperationalEnvironmentType()).thenReturn(IDmaapNotificationData.OperationaEnvironmentTypeEnum.ECOMP);

        // default test
        Deencapsulation.invoke(envEngine, "validateEnvironmentType", errorWrapper, notificationData,
                auditNotificationData);
    }

    @Test
    public void testMap2OpEnvKey() throws Exception {
        OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
        String result;

        // default test
        result = Deencapsulation.invoke(envEngine, "map2OpEnvKey", entry);
    }
}
