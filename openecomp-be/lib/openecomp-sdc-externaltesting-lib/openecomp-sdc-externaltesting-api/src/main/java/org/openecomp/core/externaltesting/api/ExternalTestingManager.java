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

package org.openecomp.core.externaltesting.api;


import java.util.List;

public interface ExternalTestingManager {

  /**
   * Return the configuration of this feature that we want to
   * expose to the client.  Treated as a JSON blob for flexibility.
   */
  ClientConfiguration getConfig();

  /**
   * For testing purposes, set the client configuration.
   */
  ClientConfiguration setConfig(ClientConfiguration config);

  /**
   * Build a tree of all test cases for the client including all
   * defined endpoints, scenarios, and test suites.
   * @return test case tree.
   */
  TestTreeNode getTestCasesAsTree();

  /**
   * Get a list of testing endpoints.
   */
  List<RemoteTestingEndpointDefinition> getEndpoints();


  /**
   * For functional testing purposes, allow the endpoint configuration
   * to be provisioned to the BE.
   */
  List<RemoteTestingEndpointDefinition> setEndpoints(List<RemoteTestingEndpointDefinition> endpoints);

  /**
   * Get a list of scenarios from and endpoint.
   */
  List<VtpNameDescriptionPair> getScenarios(String endpoint);

  /**
   * Get a list of test suites given the endpoint and scenario.
   */
  List<VtpNameDescriptionPair> getTestSuites(String endpoint, String scenario);

  /**
   * Get a list of test cases.
   * @param endpoint endpoint to contact (e.g. VTP)
   * @param scenario test scenario to get tests for
   * @return list of test cases.
   */
  List<VtpTestCase> getTestCases(String endpoint, String scenario);

  /**
   * Get the details about a particular test case.
   * @param endpoint endpoint to contact (e.g. VTP)
   * @param scenario test scenario to get tests for
   * @param testSuite suite to get tests for
   * @param testCaseName test case name to query.
   * @return details about the test case.
   */
  VtpTestCase getTestCase(String endpoint, String scenario, String testSuite, String testCaseName);

  /**
   * Execute a collection of tests where the manager must distribute
   * the tests to the appropriate endpoint and correlate the responses.
   * @param requests collection of request items.
   * @param requestId optional request ID provided from client.
   * @return response from endpoint (don't bother to parse).
   */
  List<VtpTestExecutionResponse> execute(List<VtpTestExecutionRequest> requests, String requestId);

  /**
   * Return a previous results.
   * @param endpoint endpoint to query
   * @param executionId execution to query.
   * @return response from endpoint.
   */
  VtpTestExecutionResponse getExecution(String endpoint, String executionId);

}
