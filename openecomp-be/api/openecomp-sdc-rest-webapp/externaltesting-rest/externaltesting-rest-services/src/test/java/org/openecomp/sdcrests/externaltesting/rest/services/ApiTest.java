/*
 * Copyright © 2019 iconectiv
 *
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
 */

package org.openecomp.sdcrests.externaltesting.rest.services;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VspManagerFactory.class})
public class ApiTest {

  private static final String EP = "ep";
  private static final String EXEC = "exec";
  private static final String SC = "sc";
  private static final String TS = "ts";
  private static final String TC = "tc";
  private static final String EXPECTED = "Expected";

  @Mock
  private ExternalTestingManager testingManager;
  @Mock
  private VspManagerFactory vspManagerFactory;
  @Mock
  VendorSoftwareProductManager vendorSoftwareProductManager;

  @Before
  public void setUp() {
    try {
      initMocks(this);
      mockStatic(VspManagerFactory.class);
      when(VspManagerFactory.getInstance()).thenReturn(vspManagerFactory);
      when(vspManagerFactory.createInterface()).thenReturn(vendorSoftwareProductManager);
      when(vspManagerFactory.getInstance().createInterface()).thenReturn(vendorSoftwareProductManager);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * At the API level, test that the code does not throw
   * exceptions but there's not much to test.
   */
  @Test
  public void testApi() {


    ExternalTestingImpl testing = new ExternalTestingImpl(testingManager);
    Assert.assertNotNull(testing.getConfig());
    Assert.assertNotNull(testing.getEndpoints());
    Assert.assertNotNull(testing.getExecution(EP, EXEC));
    Assert.assertNotNull(testing.getScenarios(EP));
    Assert.assertNotNull(testing.getTestcase(EP, SC, TS, TC));
    Assert.assertNotNull(testing.getTestcases(EP, SC));
    Assert.assertNotNull(testing.getTestsuites(EP, SC));
    Assert.assertNotNull(testing.getTestCasesAsTree());

    List<VtpTestExecutionRequest> requests =
            Arrays.asList(new VtpTestExecutionRequest(), new VtpTestExecutionRequest());
    Assert.assertNotNull(testing.execute("vspId", "vspVersionId", null, "[]"));


    ClientConfiguration cc = new ClientConfiguration();
    Assert.assertNotNull(testing.setConfig(cc));

    ArrayList<RemoteTestingEndpointDefinition> lst = new ArrayList<>();
    Assert.assertNotNull(testing.setEndpoints(lst));
  }

  class ApiTestExternalTestingManager implements ExternalTestingManager {

    @Override
    public ClientConfiguration getConfig() {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public ClientConfiguration setConfig(ClientConfiguration config) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<RemoteTestingEndpointDefinition> setEndpoints(List<RemoteTestingEndpointDefinition> endpoints) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public TestTreeNode getTestCasesAsTree() {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<RemoteTestingEndpointDefinition> getEndpoints() {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<VtpNameDescriptionPair> getScenarios(String endpoint) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<VtpNameDescriptionPair> getTestSuites(String endpoint, String scenario) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<VtpTestCase> getTestCases(String endpoint, String scenario) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public VtpTestCase getTestCase(String endpoint, String scenario, String testSuite, String testCaseName) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<VtpTestExecutionResponse> execute(List<VtpTestExecutionRequest> requests, String vspId,
            String vspVersionId, String requestId, Map<String, byte[]> fileMap) {

      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public VtpTestExecutionResponse getExecution(String endpoint, String executionId) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public List<VtpTestExecutionOutput> getExecutionIds(String endpoint, String requestId) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }

    @Override
    public void updateVtpResultInDB(List<VtpTestExecutionRequest> tests, String vspId, String vspVersionId,
            String requestId) {
      throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
    }
  }

  /**
   * Test the exception handler logic for configuration get/set.
   */
  @Test()
  public void testConfigExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response getResponse = testingF.getConfig();
    Assert.assertEquals(500, getResponse.getStatus());

    Response setResponse = testingF.setConfig(new ClientConfiguration());
    Assert.assertEquals(500, setResponse.getStatus());
  }

  /**
   * Test the exception handler logic for endpoint get/set.
   */
  @Test()
  public void testEndpointExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response getResponse = testingF.getEndpoints();
    Assert.assertEquals(500, getResponse.getStatus());

    Response setResponse = testingF.setEndpoints(new ArrayList<>());
    Assert.assertEquals(500, setResponse.getStatus());
  }

  /**
   * Test the exception handler logic for executions (invocation and query).
   */
  @Test()
  public void testExecutionExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);
    Response invokeResponse = testingF.execute("vspId", "versionId", null, "[]");
    Assert.assertEquals(500, invokeResponse.getStatus());

    Response getResponse = testingF.getExecution(EP, EXEC);
    Assert.assertEquals(500, getResponse.getStatus());
  }


  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an accessing the scenarios.
   */
  @Test()
  public void testScenarioExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response response = testingF.getScenarios(EP);
    Assert.assertEquals(500, response.getStatus());
  }

  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an accessing a test case.
   */
  @Test()
  public void testTestCaseExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response response = testingF.getTestcase(EP, SC, TS, TC);
    Assert.assertEquals(500, response.getStatus());
  }

  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an accessing the test cases.
   */
  @Test()
  public void testTestCasesExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response response = testingF.getTestcases(EP, SC);
    Assert.assertEquals(500, response.getStatus());
  }

  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an accessing the test suites.
   */
  @Test()
  public void testTestSuitesExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response response = testingF.getTestsuites(EP, SC);
    Assert.assertEquals(500, response.getStatus());
  }

  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an accessing the test tree.
   */
  @Test()
  public void testTreeExceptions() {
    initMocks(this);

    ExternalTestingManager m = new ApiTestExternalTestingManager();
    ExternalTestingImpl testingF = new ExternalTestingImpl(m);

    Response response = testingF.getTestCasesAsTree();
    Assert.assertEquals(500, response.getStatus());
  }
}
