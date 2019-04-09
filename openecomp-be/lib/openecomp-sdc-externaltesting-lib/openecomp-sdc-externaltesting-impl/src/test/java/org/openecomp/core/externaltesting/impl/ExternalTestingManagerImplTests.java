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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTestingManagerImplTests {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private VersioningManager versioningManager;

  @Mock
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Mock
  private OrchestrationTemplateCandidateManager candidateManager;

  @InjectMocks
  private ExternalTestingManagerImpl mgr = new ExternalTestingManagerImpl();

  @SuppressWarnings("unchecked")
  private ExternalTestingManagerImpl configTestManager(boolean loadConfig) throws IOException {

    MockitoAnnotations.initMocks(this);

    if (loadConfig) {
      mgr.init();
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

    byte[] csar = IOUtils.toByteArray(new FileInputStream("src/test/data/csar.zip"));
    byte[] heat = IOUtils.toByteArray(new FileInputStream("src/test/data/heat.zip"));

    List<Version> versionList = new ArrayList<>();
    versionList.add(new Version(UUID.randomUUID().toString()));

    Mockito
        .when(candidateManager.get(
            ArgumentMatchers.contains("csar"),
            ArgumentMatchers.any()))
        .thenReturn(Optional.of(Pair.of("Processed.zip", csar)));

    Mockito
        .when(candidateManager.get(
            ArgumentMatchers.contains("heat"),
            ArgumentMatchers.any()))
        .thenReturn(Optional.empty());

    Mockito
        .when(vendorSoftwareProductManager.get(
            ArgumentMatchers.contains("heat"),
            ArgumentMatchers.any()))
        .thenReturn(Optional.of(Pair.of("Processed.zip", heat)));



    Mockito
        .when(vendorSoftwareProductManager.get(
            ArgumentMatchers.contains("missing"),
            ArgumentMatchers.any()))
        .thenReturn(Optional.empty());

    Mockito
        .when(candidateManager.get(
            ArgumentMatchers.contains("missing"),
            ArgumentMatchers.any()))
        .thenReturn(Optional.empty());


    Mockito
        .when(versioningManager.list(
            ArgumentMatchers.contains("missing")))
        .thenReturn(versionList);






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


    HttpStatusCodeException missingException = new HttpServerErrorException(HttpStatus.NOT_FOUND, "Not Found", headers, notFound.getBytes(), Charset.defaultCharset());
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

    Mockito
        .when(restTemplate.exchange(
            ArgumentMatchers.endsWith("throwexception"),
            ArgumentMatchers.eq(HttpMethod.POST),
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(new ParameterizedTypeReference<List<VtpTestExecutionResponse>>() {})))
        .thenThrow(missingException);


    return mgr;
  }

  @Before
  public void setConfigLocation() {
    System.setProperty("config.location", "src/test/data");
  }

  @Test
  public void testManager() throws IOException {
    ExternalTestingManager m = configTestManager(true);

    ClientConfiguration config = m.getConfig();
    Assert.assertNotNull(config);

    List<RemoteTestingEndpointDefinition> endpoints = m.getEndpoints();
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
      Assert.assertNotEquals(0, e.getHttpStatus());
      Assert.assertNotNull(e.getMessageCode());
    }

    // get a particular test case
    try {
      m.getTestCase("repository", "scen", "suite", "missing");
      Assert.fail("not expected to retrieve missing test case");
    }
    catch (ExternalTestingException e) {
      // expecting this exception.
      Assert.assertNotNull(e.getDetail());
      Assert.assertNotEquals(0, e.getHttpStatus());
      Assert.assertNotNull(e.getMessageCode());
    }
  }

  @Test
  public void testManagerExecution() throws IOException {
    ExternalTestingManager m = configTestManager(true);

    // execute a test.
    List<VtpTestExecutionRequest> requests = new ArrayList<>();
    VtpTestExecutionRequest req = new VtpTestExecutionRequest();
    req.setEndpoint("repository");
    requests.add(req);

    // send a request with the endpoint defined.
    List<VtpTestExecutionResponse> responses = m.execute(requests, "rid");
    Assert.assertEquals(1, responses.size());

    // send a request for a prior execution.
    VtpTestExecutionResponse execRsp = m.getExecution("repository", "execId");
    Assert.assertEquals("COMPLETED", execRsp.getStatus());
  }

  @Test
  public void testMissingConfig() throws IOException {
    // directory exists but no config file should be found here.
    System.setProperty("config.location", "src/test");
    ExternalTestingManager m = configTestManager(true);
    Assert.assertFalse("missing config client enabled false", m.getConfig().isEnabled());
    Assert.assertEquals("missing config no endpoints", 0, m.getEndpoints().size());
  }

  @Test
  public void testMissingEndpoint() throws IOException {
    ExternalTestingManager m = configTestManager(true);

    // execute a test.
    List<VtpTestExecutionRequest> requests = new ArrayList<>();
    VtpTestExecutionRequest req = new VtpTestExecutionRequest();
    req.setEndpoint("repository");
    requests.add(req);

    // send a request with the endpoint defined.
    try {
      m.execute(requests, "throwexception");
    }
    catch (ExternalTestingException e) {
      // expected.
    }
  }


  @Test
  public void testManagerConfigOverrides() throws IOException {
    ExternalTestingManager m = configTestManager(false);

    ClientConfiguration cc = new ClientConfiguration();
    cc.setEnabled(true);
    m.setConfig(cc);
    Assert.assertTrue(m.getConfig().isEnabled());

    List<RemoteTestingEndpointDefinition> lst = new ArrayList<>();
    lst.add(new RemoteTestingEndpointDefinition());
    lst.get(0).setEnabled(true);
    m.setEndpoints(lst);
    Assert.assertEquals(1,m.getEndpoints().size());
  }

  @Test
  public void testManagerErrorCases() throws IOException {
    ExternalTestingManager m = configTestManager(false);
    ClientConfiguration emptyConfig = m.getConfig();
    Assert.assertFalse("empty configuration should have client enabled of false", emptyConfig.isEnabled());

    try {
      m.getEndpoints();
      Assert.assertTrue("should have exception here", true);
    }
    catch (ExternalTestingException e) {
      // eat the exception cause this is what should happen.
    }
  }

  @Test
  public void testExecutionDistribution() throws IOException {
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

  @Test
  public void testArchiveProcessing() throws IOException {
    ExternalTestingManagerImpl m = configTestManager(true);
    VtpTestExecutionRequest r1 = new VtpTestExecutionRequest();
    r1.setScenario("scenario1");
    r1.setEndpoint("vtp");
    r1.setParameters(new HashMap<>());
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_ID, "something.with.csar.content");
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_VERSION, UUID.randomUUID().toString());

    LinkedMultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
    m.attachArchiveContent(r1, body);

    r1.setParameters(new HashMap<>());
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_ID, "something.with.heat.content");
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_VERSION, UUID.randomUUID().toString());

    LinkedMultiValueMap<String,Object> body2 = new LinkedMultiValueMap<>();
    m.attachArchiveContent(r1, body2);

    // now, let's handle a missing archive.
    r1.setParameters(new HashMap<>());
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_ID, "something.with.missing.content");
    r1.getParameters().put(ExternalTestingManagerImpl.VSP_VERSION, UUID.randomUUID().toString());

    LinkedMultiValueMap<String,Object> body3 = new LinkedMultiValueMap<>();
    try {
      m.attachArchiveContent(r1, body3);
      Assert.fail("expected to receive an exception here");
    }
    catch (ExternalTestingException ex) {
      Assert.assertEquals(500, ex.getHttpStatus());
    }

  }
}
