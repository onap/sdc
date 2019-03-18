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

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTestingManagerImplTests {

  @Mock
  private RestTemplate restTemplate;

  static {
    System.setProperty("configuration.yaml", "src/test/data/testconfiguration.yaml");
  }

  class JUnitExternalTestingManagerImpl extends ExternalTestingManagerImpl {
    JUnitExternalTestingManagerImpl() {
      super(Collections.singletonList(
        new VariableResolver() {

        @Override
        public boolean resolvesVariablesForRequest(VtpTestExecutionRequest requestItem) {
          return false;
        }

        @Override
        public void resolve(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body) {

        }
      }));
    }
  }

  @InjectMocks
  private ExternalTestingManager mgr = new JUnitExternalTestingManagerImpl();

  @SuppressWarnings("unchecked")
  private ExternalTestingManager configTestManager(boolean loadConfig) throws Exception {
    if (loadConfig) {
      ((ExternalTestingManagerImpl) mgr).loadConfig();
    }

    ObjectMapper mapper = new ObjectMapper();

    // read mock data for API calls.

    File scenarioFile = new File("src/test/data/scenarios.json");
    TypeReference<List<VtpNameDescriptionPair>> typ = new TypeReference<List<VtpNameDescriptionPair>>(){};
    List<VtpNameDescriptionPair> scenarios = mapper.readValue(scenarioFile, typ);

    File testSuitesFile = new File("src/test/data/testsuites.json");
    List<VtpNameDescriptionPair> testSuites = mapper.readValue(testSuitesFile, new TypeReference<List<VtpNameDescriptionPair>>(){});

    File testCasesFile = new File("src/test/data/testcases.json");
    List<VtpTestCase> testCases = mapper.readValue(testCasesFile, new TypeReference<List<VtpTestCase>>(){});

    File testCaseFile = new File("src/test/data/testcase-sriov.json");
    VtpTestCase testCase = mapper.readValue(testCaseFile, VtpTestCase.class);

    File runResultFile = new File("src/test/data/runresult.json");
    List<VtpTestExecutionResponse> runResults = mapper.readValue(runResultFile, new TypeReference<List<VtpTestExecutionResponse>>(){});

    File priorExecutionFile = new File("src/test/data/priorexecution.json");
    VtpTestExecutionResponse priorExecution = mapper.readValue(priorExecutionFile, VtpTestExecutionResponse.class);

    // create an error response as well
    String notFound = FileUtils.readFileToString(new File("src/test/data/notfound.json"), "UTF-8");

    ParameterizedTypeReference<List<VtpNameDescriptionPair>> listOfPairType = new ParameterizedTypeReference<List<VtpNameDescriptionPair>>() {};
    ParameterizedTypeReference<List<VtpTestCase>> listOfCasesType = new ParameterizedTypeReference<List<VtpTestCase>>() {};
    ParameterizedTypeReference<VtpTestCase> caseType = new ParameterizedTypeReference<VtpTestCase>() {};

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/problem+json"));
    HttpStatusCodeException missingException = new HttpServerErrorException(HttpStatus.NOT_FOUND, "Not Found", headers, notFound.getBytes(), Charset.defaultCharset());

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("/scenarios"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(listOfPairType)))
        .thenReturn(new ResponseEntity(scenarios, HttpStatus.OK));

    Mockito
        .when(restTemplate.exchange(
        ArgumentMatchers.endsWith("/testsuites"),
        ArgumentMatchers.eq(HttpMethod.GET),
        ArgumentMatchers.any(),
        ArgumentMatchers.eq(listOfPairType)))
        .thenReturn(new ResponseEntity(testSuites, HttpStatus.OK));

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("/testcases"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(listOfCasesType)))
        .thenReturn(new ResponseEntity(testCases, HttpStatus.OK));

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("/sriov"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(caseType)))
        .thenReturn(new ResponseEntity(testCase, HttpStatus.OK));


    // POST for execution

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.contains("executions"),
            ArgumentMatchers.eq(HttpMethod.POST),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(new ParameterizedTypeReference<List<VtpTestExecutionResponse>>() {})))
        .thenReturn(new ResponseEntity(runResults, HttpStatus.OK));


    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.contains("/executions/"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(new ParameterizedTypeReference<VtpTestExecutionResponse>() {})))
        .thenReturn(new ResponseEntity(priorExecution, HttpStatus.OK));

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("/missing"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(caseType)))
        .thenThrow(missingException);

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("/sitedown"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(caseType)))
        .thenThrow(new ResourceAccessException("Remote site is down"));

    return mgr;
  }

  @Test
  public void testManager() throws Exception {
    System.setProperty("configuration.yaml", "src/test/data/managertestconfiguration.yaml");
    ExternalTestingManager m = configTestManager(true);

    String config = m.getConfig();
    Assert.assertNotNull(config);

    List<VtpNameDescriptionPair> endpoints = m.getEndpoints();
    Assert.assertEquals("two endpoints", 2, endpoints.size());


    // this will exercise the internal APIs as well.
    TestTreeNode root = m.getTestCasesAsTree();
    Assert.assertEquals("two scenarios", 2, root.getChildren().size());


    // handle case where remote endpoint is down.
    try {
      m.getTestCase("repository", "scen", "suite", "sitedown");
      Assert.fail("not expected to retrieve sitedown test case");
    }
    catch (ExternalTestingException e) {
      // expecting this exception.
      Assert.assertNotNull(e.getDetail());
      Assert.assertNotEquals(0, e.getCode());
      Assert.assertNotNull(e.getTitle());
    }

    // get a particular test case
    try {
      m.getTestCase("repository", "scen", "suite", "missing");
      Assert.fail("not expected to retrieve missing test case");
    }
    catch (ExternalTestingException e) {
      // expecting this exception.
      Assert.assertNotNull(e.getDetail());
      Assert.assertNotEquals(0, e.getCode());
      Assert.assertNotNull(e.getTitle());
    }


    // execute a test.
    List<VtpTestExecutionRequest> requests = new ArrayList<>();
    VtpTestExecutionRequest req = new VtpTestExecutionRequest();
    req.setEndpoint("repository");
    requests.add(req);

    // send a request with the endpoint defined.
    List<VtpTestExecutionResponse> responses = m.execute( requests, "rid");
    Assert.assertEquals(1,responses.size());

    // send a request for a prior execution.
    VtpTestExecutionResponse execRsp = m.getExecution("repository", "execId");
    Assert.assertEquals("COMPLETED", execRsp.getStatus());
  }

  @Test
  public void testManagerErrorCases() throws Exception {
    ExternalTestingManager m = configTestManager(false);
    try {
      Map<String,Object> expectedEmptyConfig = new HashMap<>();
      expectedEmptyConfig.put("enabled", false);
      String expected = new ObjectMapper().writeValueAsString(expectedEmptyConfig);
      String emptyConfig = m.getConfig();
      Assert.assertEquals(expected, emptyConfig);

      try {
        m.getEndpoints();
        Assert.assertTrue("should have exception here", true);
      }
      catch (ExternalTestingException e) {
        // eat the exception cause this is what should happen.
      }
    }
    catch (Exception ex) {
      Assert.fail("outer exception caught " + ex.getMessage());
    }
  }

  @Test
  public void testExecutionDistribution() throws Exception {
    System.setProperty("configuration.yaml", "src/test/data/managertestconfiguration.yaml");
    ExternalTestingManager m = configTestManager(true);

    VtpTestExecutionRequest r1 = new VtpTestExecutionRequest();
    r1.setScenario("scenario1");
    r1.setEndpoint("vtp");

    VtpTestExecutionRequest r2 = new VtpTestExecutionRequest();
    r2.setScenario("scenario2");
    r2.setEndpoint("vtp");

    VtpTestExecutionRequest r3 = new VtpTestExecutionRequest();
    r3.setScenario("scenario3");
    r3.setEndpoint("repository");

    List<VtpTestExecutionResponse> results = m.execute(Arrays.asList(r1,r2,r3), "rid");
    Assert.assertEquals("three in two out merged", 2, results.size());
  }
}
