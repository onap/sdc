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

package org.openecomp.core.externaltesting.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTestingManagerImplTests {

  private Logger logger = LoggerFactory.getLogger(ExternalTestingManagerImplTests.class);

  @Mock
  private RestTemplate restTemplate;

  static {
    System.setProperty("configuration.yaml", "src/test/data/testconfiguration.yaml");
  }

  class JUnitExternalTestingManagerImpl extends ExternalTestingManagerImpl {
    @Override
    protected void createVariableResolvers() {
      variableResolvers.add(new VariableResolver() {
        @Override
        public void init() {

        }

        @Override
        public boolean resolvesVariablesForRequest(TestExecutionRequestItem requestItem) {
          return true;
        }

        @Override
        public void resolve(TestExecutionRequestItem requestItem) {

        }
      });
    }
  }

  @InjectMocks
  private ExternalTestingManager mgr = new JUnitExternalTestingManagerImpl();

  @SuppressWarnings("unchecked")
  private ExternalTestingManager configTestManager(boolean loadConfig) throws Exception {
    if (loadConfig) {
      ((ExternalTestingManagerImpl) mgr).loadConfig();
    }

    TestSet ts = new ObjectMapper().readValue(new File("src/test/data/tests.json"), TestSet.class);
    org.openecomp.core.externaltesting.api.Test fullDef = new ObjectMapper().readValue(new File("src/test/data/fulldefinition.json"), org.openecomp.core.externaltesting.api.Test.class);
    String runResult = FileUtils.readFileToString(new File("src/test/data/runresult.json"), "UTF-8");

    // create an error response as well
    String notFound = FileUtils.readFileToString(new File("src/test/data/notfound.json"), "UTF-8");


    Mockito
        .when(restTemplate.getForEntity(
            ArgumentMatchers.endsWith("/tests"),
            ArgumentMatchers.eq(TestSet.class)))
        .thenReturn(new ResponseEntity(ts, HttpStatus.OK));
    Mockito
        .when(restTemplate.postForEntity(
            ArgumentMatchers.endsWith("run"),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(String.class)))
        .thenReturn(new ResponseEntity(runResult, HttpStatus.OK));
    Mockito
        .when(restTemplate.getForEntity(
            ArgumentMatchers.endsWith("/certquery"),
            ArgumentMatchers.eq(org.openecomp.core.externaltesting.api.Test.class)))
        .thenReturn(new ResponseEntity(fullDef, HttpStatus.OK));

    Mockito
        .when(restTemplate.getForEntity(
            ArgumentMatchers.endsWith("/missing"),
            ArgumentMatchers.eq(org.openecomp.core.externaltesting.api.Test.class)))
        .thenReturn(new ResponseEntity(notFound, HttpStatus.NOT_FOUND));

    return mgr;
  }


  @Test
  public void testManagerWithMultipleEndpoints() throws Exception {
    System.setProperty("configuration.yaml", "src/test/data/multiendpointmanagertestconfiguration.yaml");
    ExternalTestingManager m = configTestManager(true);


    TestSet tests = m.listTests();
    Assert.assertEquals("root should be root", "root", tests.getId());

    TestExecutionRequest req = new TestExecutionRequest();
    req.setEndpoint("repository");
    ArrayList<org.openecomp.core.externaltesting.api.TestExecutionRequestItem> inputTests = new ArrayList<>();
    org.openecomp.core.externaltesting.api.TestExecutionRequestItem t = new org.openecomp.core.externaltesting.api.TestExecutionRequestItem();
    t.setTestId("foo");
    inputTests.add(t);
    req.setTests(inputTests);

    String rsp = m.run(req);
    TestExecutionResponse ter2 = new ObjectMapper().readValue(rsp, TestExecutionResponse.class);
    Assert.assertEquals("Status Match", ter2.getStatus(), TestExecutionStatus.Completed);
  }

  @Test
  public void testManager() throws Exception {
    System.setProperty("configuration.yaml", "src/test/data/managertestconfiguration.yaml");
    ExternalTestingManager m = configTestManager(true);

    TestSet tests = m.listTests();
    Assert.assertEquals("root should be all", "all", tests.getId());

    TestExecutionRequest req = new TestExecutionRequest();
    req.setEndpoint("repository");
    req.setTests(new ArrayList<>());
    req.getTests().add(new TestExecutionRequestItem());
    String rsp = m.run(req);
    TestExecutionResponse ter = new ObjectMapper().readValue(rsp, TestExecutionResponse.class);
    Assert.assertEquals("Invocation IDs must match", "12752020-325b-4fda-9c36-d378ccd8928e", ter.getId());

    org.openecomp.core.externaltesting.api.Test t = m.getTestDefinition("certquery");
    Assert.assertEquals("Test IDs must match", "certquery", t.getId());

    org.openecomp.core.externaltesting.api.Test t2 = m.getTestDefinition("repository", "certquery");
    Assert.assertEquals("Test IDs must match with endpoint", "certquery", t2.getId());

    Assert.assertTrue("Client should be enabled", m.getConfig().contains("true"));
  }

  @Test
  public void testManagerErrorCases() throws Exception {
    ExternalTestingManager m = configTestManager(false);
    try {
      TestSet tests = m.listTests();
      Assert.assertNull("with no config, null root", tests.getId());

      String s = m.getConfig();
      ClientConfiguration cc = new ObjectMapper().readValue(s, ClientConfiguration.class);
      Assert.assertTrue("with no config, cc should have enabled false", !cc.isEnabled());

      try {
        m.run(new TestExecutionRequest());
        Assert.assertTrue("should have exception here", true);
      }
      catch (ExternalTestingException e) {
        // eat the exception cause this is what should happen.
      }
      try {
        m.getTestDefinition("foobar");
        Assert.assertTrue("should have exception here", true);
      }
      catch (ExternalTestingException e) {
        // eat the exception cause this is what should happen.
      }
      try {
        m.getTestDefinition("foobar", "baz");
        Assert.assertTrue("should have exception here", true);
      }
      catch (ExternalTestingException e) {
        // eat the exception cause this is what should happen.
      }
    }
    catch (Exception ex) {
      logger.warn("test error", ex);
    }
  }
}
