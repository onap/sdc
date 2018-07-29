package org.openecomp.sdc.be.components.distribution.engine;

import fj.data.Either;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.info.OperationalEnvInfo;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.http.client.api.HttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class EnvironmentsEngineMockTest {

    @InjectMocks
    private EnvironmentsEngine envEngine;
    @Mock
    private DmaapConsumer dmaapConsumer;
    @Mock
    private OperationalEnvironmentDao operationalEnvironmentDao;
    @Mock
    private DME2EndpointIteratorCreator epIterCreator;
    @Mock
    private ConfigurationManager configurationManager;
    @Mock
    private DistributionEngineConfiguration distributionEngineConfiguration;
    @Mock
    private AaiRequestHandler aaiRequestHandler;

    @Before
    public void preStart() {
        when(configurationManager.getDistributionEngineConfiguration()).thenReturn(distributionEngineConfiguration);
        envEngine.setConfigurationManager(configurationManager);
    }

    @Test
    public void testInit() {
        List<OperationalEnvironmentEntry> entryList = Arrays.asList(createOpEnvEntry("Env1"), createOpEnvEntry("Env2"));
        Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> successEither = Either.left(entryList);
        when(operationalEnvironmentDao.getByEnvironmentsStatus(EnvironmentStatusEnum.COMPLETED)).thenReturn(successEither);

        when(distributionEngineConfiguration.getEnvironments()).thenReturn(Arrays.asList("Env Loaded From Configuration"));
        when(distributionEngineConfiguration.getUebPublicKey()).thenReturn("Dummy Public Key");
        when(distributionEngineConfiguration.getUebSecretKey()).thenReturn("Dummy Private Key");
        when(distributionEngineConfiguration.getUebServers()).thenReturn(
                Arrays.asList("uebsb91kcdc.it.att.com:3904", "uebsb92kcdc.it.att.com:3904", "uebsb91kcdc.it.att.com:3904"));

        envEngine.init();

        Map<String, OperationalEnvironmentEntry> mapEnvs = envEngine.getEnvironments();
        assertEquals("unexpected size of map",3, mapEnvs.size());
    }


    @Test
    public void testGetFullOperationalEnvByIdSuccess() {
        String json = getFullOperationalEnvJson();
        
        HttpResponse<String> restResponse = new HttpResponse<String>(json, HttpStatus.SC_OK, "Successfully completed");
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

}
