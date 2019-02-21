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
   * Invoke the test manager to retrieve a list of tests available tests to the caller.
   * @return JSON response content.
   */
  @Override
  public Response listTests() {
    try {
      return Response.ok(testingManager.listTests()).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
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

  @Override
  public Response getTestDefinition(String testId) {
    try {
      return Response.ok(testingManager.getTestDefinition(testId)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response getTestDefinition(String endpointId, String testId) {
    try {
      return Response.ok(testingManager.getTestDefinition(endpointId, testId)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  @Override
  public Response run(TestExecutionRequest req) {
    try {
      return Response.ok(testingManager.run(req)).build();
    }
    catch (ExternalTestingException e) {
      return convertTestingException(e);
    }
  }

  private Response convertTestingException(ExternalTestingException e) {
    if (logger.isErrorEnabled()) {
      logger.error("testing exception {} {} {}" + e.getTitle(), e.getCode(), e.getDetail(), e);
    }
    TestErrorBody body = new TestErrorBody(e.getTitle(), e.getCode(), e.getDetail());
    return Response.status(e.getCode()).entity(body).build();
  }

}
