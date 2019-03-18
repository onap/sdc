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


import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.externaltesting.rest.ExternalTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Named
@Service("externaltesting")
@Scope(value = "prototype")
public class ExternalTestingImpl implements ExternalTesting {

  private ExternalTestingManager testingManager;

  private static final Logger logger =
      LoggerFactory.getLogger(ExternalTestingImpl.class);

  public ExternalTestingImpl(@Autowired ExternalTestingManager testingManager) {
    this.testingManager = testingManager;
  }

  /**
   * Return the configuration of the feature to the client.
   * @return JSON response content.
   */
  @Override
  public Response getConfig() {
    try {
      return Response.ok(testingManager.getConfig()).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  /**
   * Return the test tree structure created by the testing manager.
   * @return JSON response content.
   */
  @Override
  public Response getTestCasesAsTree() {
    try {
      return Response.ok(testingManager.getTestCasesAsTree()).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response getEndpoints() {
    try {
      return Response.ok(testingManager.getEndpoints()).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }

  }
  @Override
  public Response getScenarios(String endpoint) {
    try {
      return Response.ok(testingManager.getScenarios(endpoint)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }

  }

  @Override
  public Response getTestsuites(String endpoint, String scenario) {
    try {
      return Response.ok(testingManager.getTestSuites(endpoint, scenario)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response getTestcases(String endpoint, String scenario) {
    try {
      return Response.ok(testingManager.getTestCases(endpoint, scenario)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response getTestcase(String endpoint, String scenario, String testsuite, String testcase) {
    try {
      return Response.ok(testingManager.getTestCase(endpoint, scenario, testsuite, testcase)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response execute(List<VtpTestExecutionRequest> req, String requestId) {
    try {
      List<VtpTestExecutionResponse> responses = testingManager.execute(req, requestId);
      List<Integer> statuses = responses.stream().map(r-> Optional.ofNullable(r.getHttpStatus()).orElse(200)).distinct().collect(Collectors.toList());
      if (statuses.size() == 1) {
        // 1 status so use it...
        return Response.status(statuses.get(0)).entity(responses).build();
      }
      else {
        return Response.status(207).entity(responses).build();
      }
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response getExecution(String endpoint, String executionId) {
    try {
      return Response.ok(testingManager.getExecution(endpoint, executionId)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  private Response convertTestingException(ExternalTestingException e) {
    if (logger.isErrorEnabled()) {
      logger.error("testing exception {} {} {}", e.getTitle(), e.getCode(), e.getDetail(), e);
    }
    TestErrorBody body = new TestErrorBody(e.getTitle(), e.getCode(), e.getDetail());
    return Response.status(e.getCode()).entity(body).build();
  }
}
