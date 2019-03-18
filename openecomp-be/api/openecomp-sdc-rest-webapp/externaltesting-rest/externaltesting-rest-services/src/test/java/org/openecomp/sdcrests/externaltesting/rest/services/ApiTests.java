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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;

import java.util.Arrays;
import java.util.List;

public class ApiTests {

  private static final String EP = "ep";
  private static final String EXEC = "exec";
  private static final String SC = "sc";
  private static final String TS = "ts";
  private static final String TC = "tc";
  private static final String EXPECTED = "Expected";


  @Mock
  private ExternalTestingManager testingManager;

  /**
   * At the API level, test that the code does not throw
   * exceptions but there's not much to test.
   */
  @Test
  public void testApi() {
    MockitoAnnotations.initMocks(this);

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
    Assert.assertNotNull(testing.execute(requests, "requestId"));
  }

  /**
   * Test the exception handler logic for the cases when the
   * testing manager throws an exception.
   */
  @Test
  public void testExceptions() {
    MockitoAnnotations.initMocks(this);

    ExternalTestingManager m = new ExternalTestingManager() {

      @Override
      public String getConfig() {
        throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
      }

      @Override
      public TestTreeNode getTestCasesAsTree() {
        throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
      }

      @Override
      public List<VtpNameDescriptionPair> getEndpoints() {
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
      public List<VtpTestExecutionResponse> execute(List<VtpTestExecutionRequest> requests, String requestId) {
        throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
      }

      @Override
      public VtpTestExecutionResponse getExecution(String endpoint, String executionId) {
        throw new ExternalTestingException(EXPECTED, 500, EXPECTED);
      }
    };

    ExternalTestingImpl testingF = new ExternalTestingImpl(m);
    testingF.getConfig();


    try {
      testingF.getEndpoints();
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    try {
      testingF.getExecution(EP, EXEC);
    }
    catch (ExternalTestingException e) {
      // expected.
    }
    try {
      testingF.getScenarios(EP);
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    try {
      testingF.getTestcase(EP, SC, TS, TC);
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    try {
      testingF.getTestcases(EP, SC);
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    try {
      testingF.getTestsuites(EP, SC);
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    try {
      testingF.getTestCasesAsTree();
    }
    catch (ExternalTestingException e) {
      // expected.
    }

    List<VtpTestExecutionRequest> requestsF =
        Arrays.asList(new VtpTestExecutionRequest(), new VtpTestExecutionRequest());

    try {
      testingF.execute(requestsF, null);
    }
    catch (ExternalTestingException e) {
      // expected.
    }


    try {
      testingF.execute(requestsF, null);
    }
    catch (ExternalTestingException e) {
      // expected.
    }
  }
}
