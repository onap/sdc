package org.openecomp.sdc.ci.tests.execute.devCI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CommonRestUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class HealthCheckAPI extends ComponentBaseTest {

    @Rule
    public static TestName name = new TestName();

    public HealthCheckAPI() {
        super(name, HealthCheckAPI.class.getName());
    }

    @Test
    public void checkAmdocsHealthCheckAPI() throws Exception {
        RestResponse healthCheckInfoResponse = CommonRestUtils.getHealthCheck();
        BaseRestUtils.checkSuccess(healthCheckInfoResponse);

        Gson gson = new Gson();
        HealthCheckWrapper healthCheckInfo = gson.fromJson(healthCheckInfoResponse.getResponse(), HealthCheckWrapper.class);
        assertNotNull("Health check not contains components info", healthCheckInfo.getComponentsInfo());
        HealthCheckInfo amdocsHC = healthCheckInfo.getComponentsInfo().stream().filter(x -> x.getHealthCheckComponent().equals(Constants.HC_COMPONENT_ON_BOARDING)).findFirst().orElse(null);
        assertNotNull("Amdocs health check not exists in Health Check info", amdocsHC);
        assertEquals("Amdocs health check is down", HealthCheckInfo.HealthCheckStatus.UP, amdocsHC.getHealthCheckStatus());
        assertNotNull("Amdocs componentsInfo not exists in health check", amdocsHC.getComponentsInfo());
        Map<String, HealthCheckStatus> amdocsHCComponents = amdocsHC.getComponentsInfo().stream().collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));
        assertNotNull(amdocsHCComponents);
        assertTrue("Amdocs health check ZU component is down or not exists", amdocsHCComponents.get("ZU") != null && amdocsHCComponents.get("ZU").equals(HealthCheckStatus.UP));
        assertTrue("Amdocs health check BE component is down or not exists", amdocsHCComponents.get("BE") != null && amdocsHCComponents.get("BE").equals(HealthCheckStatus.UP));
        assertTrue("Amdocs health check CAS component is down or not exists", amdocsHCComponents.get("CAS") != null && amdocsHCComponents.get("CAS").equals(HealthCheckStatus.UP));
        assertTrue("Amdocs health check FE component is down or not exists", amdocsHCComponents.get("FE") != null && amdocsHCComponents.get("FE").equals(HealthCheckStatus.UP));
    }

    @Test
    public void checkDcaeHealthCheckAPI() throws Exception {
        RestResponse healthCheckInfoResponse = CommonRestUtils.getHealthCheck();
        BaseRestUtils.checkSuccess(healthCheckInfoResponse);

        Gson gson = new Gson();
        HealthCheckWrapper healthCheckInfo = gson.fromJson(healthCheckInfoResponse.getResponse(), HealthCheckWrapper.class);
        assertNotNull("Health check not contains components info", healthCheckInfo.getComponentsInfo());
        HealthCheckInfo dcaeHC = healthCheckInfo.getComponentsInfo().stream().filter(x -> x.getHealthCheckComponent().equals(Constants.HC_COMPONENT_DCAE)).findFirst().orElse(null);
        assertNotNull("DCAE health check not exists in Health Check info", dcaeHC);
        assertEquals("DCAE health check is down", HealthCheckInfo.HealthCheckStatus.UP, dcaeHC.getHealthCheckStatus());
        assertNotNull("DCAE componentsInfo not exists in health check", dcaeHC.getComponentsInfo());
        Map<String, HealthCheckStatus> dcaeHCComponents = dcaeHC.getComponentsInfo().stream().collect(Collectors.toMap(HealthCheckInfo::getHealthCheckComponent, HealthCheckInfo::getHealthCheckStatus));
        assertNotNull(dcaeHCComponents);
        assertTrue("DCAE health check BE component is down or not exists", dcaeHCComponents.get("BE") != null && dcaeHCComponents.get("BE").equals(HealthCheckStatus.UP));
        assertTrue("DCAE health check FE component is down or not exists", dcaeHCComponents.get("FE") != null && dcaeHCComponents.get("FE").equals(HealthCheckStatus.UP));
    }

    @Test
    public void checkCassandraHealthCheck() throws Exception {
    	RestResponse healthCheckInfoResponse = CommonRestUtils.getHealthCheck();
        BaseRestUtils.checkSuccess(healthCheckInfoResponse);

        Gson gson = new Gson();
        HealthCheckWrapper healthCheckInfo = gson.fromJson(healthCheckInfoResponse.getResponse(), HealthCheckWrapper.class);
        assertNotNull("Health check not contains components info", healthCheckInfo.getComponentsInfo());
        HealthCheckInfo cassandraHC = healthCheckInfo.getComponentsInfo().stream().filter(x -> x.getHealthCheckComponent().equals(Constants.HC_COMPONENT_CASSANDRA)).findFirst().orElse(null);
        assertNotNull("Cassandra health check not exists in Health Check info", cassandraHC);
        assertEquals("Cassandra health check is down", HealthCheckInfo.HealthCheckStatus.UP, cassandraHC.getHealthCheckStatus());
    }
    
}