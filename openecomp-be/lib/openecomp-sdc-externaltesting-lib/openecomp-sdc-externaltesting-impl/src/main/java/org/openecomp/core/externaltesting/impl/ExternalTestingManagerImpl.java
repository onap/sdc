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

import com.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ExternalTestingManagerImpl implements ExternalTestingManager {

  private Logger logger = LoggerFactory.getLogger(ExternalTestingManagerImpl.class);

  private static final String FILE_URL_PREFIX = "file://";
  private static final String MANIFEST_JSON = "MANIFEST.json";
  private static final String HTTP_STATUS = "httpStatus";
  private static final String CODE = "code";
  private static final String ERROR = "error";
  private static final String MESSAGE = "message";
  private static final String DETAIL = "detail";
  private static final String PATH = "path";

  private static final String VTP_SCENARIOS_URI = "%s/v1/vtp/scenarios";
  private static final String VTP_TESTSUITE_URI = "%s/v1/vtp/scenarios/%s/testsuites";
  private static final String VTP_TESTCASES_URI = "%s/v1/vtp/scenarios/%s/testcases";
  private static final String VTP_TESTCASE_URI = "%s/v1/vtp/scenarios/%s/testsuites/%s/testcases/%s";
  private static final String VTP_EXECUTIONS_URI = "%s/v1/vtp/executions";
  private static final String VTP_EXECUTION_URI = "%s/v1/vtp/executions/%s";

  private static final String INVALIDATE_STATE_ERROR_CODE = "SDC-TEST-001";
  private static final String NO_ACCESS_CONFIGURATION_DEFINED = "No access configuration defined";

  private static final String NO_SUCH_ENDPOINT_ERROR_CODE = "SDC-TEST-002";
  private static final String ENDPOINT_ERROR_CODE = "SDC-TEST-003";
  private static final String TESTING_HTTP_ERROR_CODE = "SDC-TEST-004";
  private static final String SDC_RESOLVER_ERR = "SDC-TEST-005";

  private static final String TOSCA_META = "TOSCA-Metadata/TOSCA.meta";
  private static final String MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME = "MainServiceTemplate.yaml";
  private static final String TOSCA_META_ENTRY_DEFINITIONS="Entry-Definitions";
  static final String VSP_ID = "vspId";
  static final String VSP_VERSION = "vspVersion";

  private static final String SDC_CSAR = "sdc-csar";
  private static final String SDC_HEAT = "sdc-heat";


  private VersioningManager versioningManager;
  private VendorSoftwareProductManager vendorSoftwareProductManager;
  private OrchestrationTemplateCandidateManager candidateManager;

  private TestingAccessConfig accessConfig;
  private List<RemoteTestingEndpointDefinition> endpoints;

  private RestTemplate restTemplate;

  public ExternalTestingManagerImpl() {
    restTemplate = new RestTemplate();
  }

  ExternalTestingManagerImpl(VersioningManager versioningManager,
                               VendorSoftwareProductManager vendorSoftwareProductManager,
                               OrchestrationTemplateCandidateManager candidateManager) {
    this();
    this.versioningManager = versioningManager;
    this.vendorSoftwareProductManager = vendorSoftwareProductManager;
    this.candidateManager = candidateManager;
  }

  /**
   * Read the configuration from the yaml file for this bean.  If we get an exception during load,
   * don't force an error starting SDC but log a warning.  Do no warm...
   */
  @PostConstruct
  public void init() {

    if (versioningManager == null) {
      versioningManager = VersioningManagerFactory.getInstance().createInterface();
    }
    if (vendorSoftwareProductManager == null) {
      vendorSoftwareProductManager =
          VspManagerFactory.getInstance().createInterface();
    }
    if (candidateManager == null) {
      candidateManager =
          OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
    }

    loadConfig();
  }

  private Stream<RemoteTestingEndpointDefinition> mapEndpointString(String ep) {
    RemoteTestingEndpointDefinition rv = new RemoteTestingEndpointDefinition();
    String[] cfg = ep.split(",");
    if (cfg.length < 4) {
      logger.error("invalid endpoint definition {}", ep);
      return Stream.empty();
    }
    else {
      rv.setId(cfg[0]);
      rv.setTitle(cfg[1]);
      rv.setEnabled("true".equals(cfg[2]));
      rv.setUrl(cfg[3]);
      if (cfg.length > 4) {
        rv.setScenarioFilter(cfg[4]);
      }
      if (cfg.length > 5) {
        rv.setApiKey(cfg[5]);
      }
      return Stream.of(rv);
    }
  }

  /**
   * Load the configuration for this component.  When the SDC onboarding backend
   * runs, it gets a system property called config.location.  We can use that
   * to locate the config-externaltesting.yaml file.
   */
  private void loadConfig() {
    String loc = System.getProperty("config.location");
    File file = new File(loc, "externaltesting-configuration.yaml");
    try (InputStream fileInput = new FileInputStream(file)) {
      YamlUtil yamlUtil = new YamlUtil();
      accessConfig = yamlUtil.yamlToObject(fileInput, TestingAccessConfig.class);

      if (logger.isInfoEnabled()) {
        String s = new ObjectMapper().writeValueAsString(accessConfig);
        logger.info("loaded external testing config {}", s);
      }

      endpoints = accessConfig.getEndpoints().stream()
          .flatMap(this::mapEndpointString)
          .collect(Collectors.toList());

      if (logger.isInfoEnabled()) {
        String s = new ObjectMapper().writeValueAsString(endpoints);
        logger.info("processed external testing config {}", s);
      }
    }
    catch (IOException ex) {
      logger.error("failed to read external testing config.  Disabling the feature", ex);
      accessConfig = new TestingAccessConfig();
      accessConfig.setEndpoints(new ArrayList<>());
      accessConfig.setClient(new ClientConfiguration());
      accessConfig.getClient().setEnabled(false);
      endpoints = new ArrayList<>();
    }
  }

  /**
   * Return the configuration of this feature that we want to
   * expose to the client.  Treated as a JSON blob for flexibility.
   */
  @Override
  public ClientConfiguration getConfig() {
    ClientConfiguration cc = null;
    if (accessConfig != null) {
      cc = accessConfig.getClient();
    }
    if (cc == null) {
      cc = new ClientConfiguration();
      cc.setEnabled(false);
    }
    return cc;
  }

  /**
   * To allow for functional testing, we let a caller invoke
   * a setConfig request to enable/disable the client.  This
   * new value is not persisted.
   * @return new client configuration
   */
  @Override
  public ClientConfiguration setConfig(ClientConfiguration cc) {
    if (accessConfig == null) {
      accessConfig = new TestingAccessConfig();
    }
    accessConfig.setClient(cc);
    return getConfig();
  }

  /**
   * To allow for functional testing, we let a caller invoke
   * a setEndpoints request to configure where the BE makes request to.
   * @return new endpoint definitions.
   */
  @Override
  public List<RemoteTestingEndpointDefinition> setEndpoints(List<RemoteTestingEndpointDefinition> endpoints) {
    this.endpoints = endpoints;
    return this.getEndpoints();
  }



  @Override
  public TestTreeNode getTestCasesAsTree() {
    TestTreeNode root = new TestTreeNode("root", "root");

    // quick out in case of non-configured SDC
    if (endpoints == null) {
      return root;
    }

    for (RemoteTestingEndpointDefinition ep : endpoints) {
      if (ep.isEnabled()) {
        buildTreeFromEndpoint(ep, root);
      }
    }
    return root;
  }

  private void buildTreeFromEndpoint(RemoteTestingEndpointDefinition ep, TestTreeNode root) {
    try {
      logger.debug("process endpoint {}", ep.getId());
      getScenarios(ep.getId()).stream().filter(s ->
          ((ep.getScenarioFilter() == null) || ep.getScenarioFilterPattern().matcher(s.getName()).matches()))
          .forEach(s -> {
            addScenarioToTree(root, s);
            getTestSuites(ep.getId(), s.getName()).forEach(suite -> addSuiteToTree(root, s, suite));
            getTestCases(ep.getId(), s.getName()).forEach(tc -> {
              try {
                VtpTestCase details = getTestCase(ep.getId(), s.getName(), tc.getTestSuiteName(), tc.getTestCaseName());
                addTestCaseToTree(root, ep.getId(), s.getName(), tc.getTestSuiteName(), details);
              }
              catch (@SuppressWarnings("squid:S1166") ExternalTestingException ex) {
                // Not logging stack trace on purpose.  VTP was throwing exceptions for certain test cases.
                logger.warn("failed to load test case {}", tc.getTestCaseName());
              }
            });
          });
    }
    catch (ExternalTestingException ex) {
      logger.error("unable to contact testing endpoint {}", ep.getId(), ex);
    }
  }

  private Optional<TestTreeNode> findNamedChild(TestTreeNode root, String name) {
    if (root.getChildren() == null) {
      return Optional.empty();
    }
    return root.getChildren().stream().filter(n->n.getName().equals(name)).findFirst();
  }

  /**
   * Find the place in the tree to add the test case.
   * @param root root of the tree.
   * @param endpointName name of the endpoint to assign to the test case.
   * @param scenarioName scenario to add this case to
   * @param testSuiteName suite in the scenario to add this case to
   * @param tc test case to add.
   */
  private void addTestCaseToTree(TestTreeNode root, String endpointName, String scenarioName, String testSuiteName, VtpTestCase tc) {
    // return quickly.
    if (tc == null) {
      return;
    }
    findNamedChild(root, scenarioName)
        .ifPresent(scenarioNode -> findNamedChild(scenarioNode, testSuiteName)
            .ifPresent(suiteNode -> {
              massageTestCaseForUI(tc, endpointName, scenarioName);
              if (suiteNode.getTests() == null) {
                suiteNode.setTests(new ArrayList<>());
              }
              suiteNode.getTests().add(tc);
            }));
  }

  private void massageTestCaseForUI(VtpTestCase testcase, String endpoint, String scenario) {
    testcase.setEndpoint(endpoint);
    // VTP workaround.
    if (testcase.getScenario() == null) {
      testcase.setScenario(scenario);
    }
  }

  /**
   * Add the test suite to the tree at the appropriate place if it does not already exist in the tree.
   * @param root root of the tree.
   * @param scenario scenario under which this suite should be placed
   * @param suite test suite to add.
   */
  private void addSuiteToTree(final TestTreeNode root, final VtpNameDescriptionPair scenario, final VtpNameDescriptionPair suite) {
    findNamedChild(root, scenario.getName()).ifPresent(parent -> {
      if (parent.getChildren() == null) {
        parent.setChildren(new ArrayList<>());
      }
      if (parent.getChildren().stream().noneMatch(n -> StringUtils.equals(n.getName(), suite.getName()))) {
        parent.getChildren().add(new TestTreeNode(suite.getName(), suite.getDescription()));
      }
    });
  }

  /**
   * Add the scenario to the tree if it does not already exist.
   * @param root root of the tree.
   * @param s scenario to add.
   */
  private void addScenarioToTree(TestTreeNode root, VtpNameDescriptionPair s) {
    logger.debug("addScenario {} to {} with {}", s.getName(), root.getName(), root.getChildren());
    if (root.getChildren() == null) {
      root.setChildren(new ArrayList<>());
    }
    if (root.getChildren().stream().noneMatch(n->StringUtils.equals(n.getName(),s.getName()))) {
      logger.debug("createScenario {} in {}", s.getName(), root.getName());
      root.getChildren().add(new TestTreeNode(s.getName(), s.getDescription()));
    }
  }

  /**
   * Get the list of endpoints defined to the testing manager.
   * @return list of endpoints or empty list if the manager is not configured.
   */
  public List<RemoteTestingEndpointDefinition> getEndpoints() {
    if (endpoints != null) {
      return endpoints.stream()
          .filter(RemoteTestingEndpointDefinition::isEnabled)
          .collect(Collectors.toList());
    }
    else {
      return new ArrayList<>();
    }
  }

  /**
   * Code shared by getScenarios and getTestSuites.
   */
  private List<VtpNameDescriptionPair> returnNameDescriptionPairFromUrl(String url) {
    ParameterizedTypeReference<List<VtpNameDescriptionPair>> t = new ParameterizedTypeReference<List<VtpNameDescriptionPair>>() {};
    List<VtpNameDescriptionPair> rv = proxyGetRequestToExternalTestingSite(url, t);
    if (rv == null) {
      rv = new ArrayList<>();
    }
    return rv;
  }

  /**
   * Get the list of scenarios at a given endpoint.
   */
  public List<VtpNameDescriptionPair> getScenarios(final String endpoint) {
    String url = buildEndpointUrl(VTP_SCENARIOS_URI, endpoint, ArrayUtils.EMPTY_STRING_ARRAY);
    return returnNameDescriptionPairFromUrl(url);
  }

  /**
   * Get the list of test suites for an endpoint for the given scenario.
   */
  public List<VtpNameDescriptionPair> getTestSuites(final String endpoint, final String scenario) {
    String url = buildEndpointUrl(VTP_TESTSUITE_URI, endpoint, new String[] {scenario});
    return returnNameDescriptionPairFromUrl(url);
  }

  /**
   * Get the list of test cases under a scenario.  This is the VTP API.  It would
   * seem better to get the list of cases under a test suite but that is not supported.
   */
  @Override
  public List<VtpTestCase> getTestCases(String endpoint, String scenario) {
    String url = buildEndpointUrl(VTP_TESTCASES_URI, endpoint, new String[] {scenario});
    ParameterizedTypeReference<List<VtpTestCase>> t = new ParameterizedTypeReference<List<VtpTestCase>>() {};
    List<VtpTestCase> rv = proxyGetRequestToExternalTestingSite(url, t);
    if (rv == null) {
      rv = new ArrayList<>();
    }
    return rv;
  }

  /**
   * Get a test case definition.
   */
  @Override
  public VtpTestCase getTestCase(String endpoint, String scenario, String testSuite, String testCaseName) {
    String url = buildEndpointUrl(VTP_TESTCASE_URI, endpoint, new String[] {scenario, testSuite, testCaseName});
    ParameterizedTypeReference<VtpTestCase> t = new ParameterizedTypeReference<VtpTestCase>() {};
    return proxyGetRequestToExternalTestingSite(url, t);
  }

  /**
   * Return the results of a previous test execution.
   * @param endpoint endpoint to query
   * @param executionId execution to query.
   * @return execution response from testing endpoint.
   */
  @Override
  public VtpTestExecutionResponse getExecution(String endpoint,String executionId) {
    String url = buildEndpointUrl(VTP_EXECUTION_URI, endpoint, new String[] {executionId});
    ParameterizedTypeReference<VtpTestExecutionResponse> t = new ParameterizedTypeReference<VtpTestExecutionResponse>() {};
    return proxyGetRequestToExternalTestingSite(url, t);
  }

  /**
   * Execute a set of tests at a given endpoint.
   * @param endpointName name of the endpoint
   * @param testsToRun set of tests to run
   * @return list of execution responses.
   */
  private List<VtpTestExecutionResponse> execute(final String endpointName, final List<VtpTestExecutionRequest> testsToRun, String requestId) {
    if (endpoints == null) {
      throw new ExternalTestingException(INVALIDATE_STATE_ERROR_CODE, 500, NO_ACCESS_CONFIGURATION_DEFINED);
    }

    RemoteTestingEndpointDefinition endpoint = endpoints.stream()
        .filter(e -> StringUtils.equals(endpointName, e.getId()))
        .findFirst()
        .orElseThrow(() -> new ExternalTestingException(NO_SUCH_ENDPOINT_ERROR_CODE, 400, "No endpoint named " + endpointName + " is defined"));

    // if the endpoint requires an API key, specify it in the headers.
    HttpHeaders headers = new HttpHeaders();
    if (endpoint.getApiKey() != null) {
      headers.add("X-API-Key", endpoint.getApiKey());
    }
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // build the body.
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    for(VtpTestExecutionRequest test: testsToRun) {
      if ((test.getParameters() != null) &&
          (test.getParameters().containsKey(SDC_CSAR) || test.getParameters().containsKey(SDC_HEAT))) {
        attachArchiveContent(test, body);
      }
    }

    try {
      // remove the endpoint from the test request since that is a FE/BE attribute
      testsToRun.forEach(t -> t.setEndpoint(null));

      body.add("executions", new ObjectMapper().writeValueAsString(testsToRun));
    }
    catch (IOException ex) {
      logger.error("exception converting tests to string", ex);
      VtpTestExecutionResponse err = new VtpTestExecutionResponse();
      err.setHttpStatus(500);
      err.setCode(TESTING_HTTP_ERROR_CODE);
      err.setMessage("Execution failed due to " + ex.getMessage());
      return Collections.singletonList(err);
    }

    // form and send request.
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    String url = buildEndpointUrl(VTP_EXECUTIONS_URI, endpointName, ArrayUtils.EMPTY_STRING_ARRAY);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    if (requestId != null) {
      builder = builder.queryParam("requestId", requestId);
    }
    ParameterizedTypeReference<List<VtpTestExecutionResponse>> t = new ParameterizedTypeReference<List<VtpTestExecutionResponse>>() {};
    try {
      return proxyRequestToExternalTestingSite(builder.toUriString(), requestEntity, t);
    }
    catch (ExternalTestingException ex) {
      logger.error("exception caught invoking endpoint {}", endpointName, ex);
      VtpTestExecutionResponse err = new VtpTestExecutionResponse();
      err.setHttpStatus(ex.getHttpStatus());
      err.setCode(TESTING_HTTP_ERROR_CODE);
      err.setMessage(ex.getMessageCode() + ": " + ex.getDetail());
      return Collections.singletonList(err);
    }
  }


  /**
   * Execute tests splitting them across endpoints and collecting the results.
   * @param testsToRun list of tests to be executed.
   * @return collection of result objects.
   */
  @Override
  public List<VtpTestExecutionResponse> execute(final List<VtpTestExecutionRequest> testsToRun, String requestId) {
    if (endpoints == null) {
      throw new ExternalTestingException(INVALIDATE_STATE_ERROR_CODE, 500, NO_ACCESS_CONFIGURATION_DEFINED);
    }

    // partition the requests by endpoint.
    Map<String, List<VtpTestExecutionRequest>> partitions =
        testsToRun.stream().collect(Collectors.groupingBy(VtpTestExecutionRequest::getEndpoint));

    // process each group and collect the results.
    return partitions.entrySet().stream()
        .flatMap(e -> execute(e.getKey(), e.getValue(), requestId).stream())
        .collect(Collectors.toList());
  }

  /**
   * Return URL with endpoint url as prefix.
   * @param format format string.
   * @param endpointName endpoint to address
   * @param args args for format.
   * @return qualified url.
   */
  private String buildEndpointUrl(String format, String endpointName, String[] args) {
    if (endpoints != null) {
      RemoteTestingEndpointDefinition ep = endpoints.stream()
          .filter(e -> e.isEnabled() && e.getId().equals(endpointName))
          .findFirst()
          .orElseThrow(() -> new ExternalTestingException(NO_SUCH_ENDPOINT_ERROR_CODE, 500, "No endpoint named " + endpointName + " is defined")
          );

      Object[] newArgs = ArrayUtils.add(args, 0, ep.getUrl());
      return String.format(format, newArgs);
    }
    throw new ExternalTestingException(INVALIDATE_STATE_ERROR_CODE, 500, NO_ACCESS_CONFIGURATION_DEFINED);
  }

  /**
   * Proxy a get request to a testing endpoint.
   * @param url URL to invoke.
   * @param responseType type of response expected.
   * @param <T> type of response expected
   * @return instance of <T> parsed from the JSON response from endpoint.
   */
  private <T> T proxyGetRequestToExternalTestingSite(String url, ParameterizedTypeReference<T> responseType) {
    return proxyRequestToExternalTestingSite(url, null, responseType);
  }

  /**
   * Make the actual HTTP post (using Spring RestTemplate) to an endpoint.
   * @param url URL to the endpoint
   * @param request optional request body to send
   * @param responseType expected type
   * @param <T> extended type
   * @return instance of expected type
   */
  private <R,T> T proxyRequestToExternalTestingSite(String url, HttpEntity<R> request, ParameterizedTypeReference<T> responseType) {
    if (request != null) {
      logger.debug("POST request to {} with {} for {}", url, request, responseType.getType().getTypeName());
    }
    else {
      logger.debug("GET request to {} for {}", url, responseType.getType().getTypeName());
    }
    SimpleClientHttpRequestFactory rf =
        (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    if (rf != null) {
      rf.setReadTimeout(10000);
      rf.setConnectTimeout(10000);
    }
    ResponseEntity<T> re;
    try {
      if (request != null) {
        re = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
      } else {
        re = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
      }
    }
    catch (HttpStatusCodeException ex) {
      // make my own exception out of this.
      logger.warn("Unexpected HTTP Status from endpoint {}", ex.getRawStatusCode());
      if ((ex.getResponseHeaders().getContentType() != null) &&
          ((ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) ||
              (ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.parseMediaType("application/problem+json"))))) {
        String s = ex.getResponseBodyAsString();
        logger.warn("endpoint body content is {}", s);
        try {
          JsonObject o = new GsonBuilder().create().fromJson(s, JsonObject.class);
          throw buildTestingException(ex.getRawStatusCode(), o);
        }
        catch (JsonParseException e) {
          logger.warn("unexpected JSON response", e);
          throw new ExternalTestingException(ENDPOINT_ERROR_CODE, ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
        }
      }
      else {
        throw new ExternalTestingException(ENDPOINT_ERROR_CODE, ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
      }
    }
    catch (ResourceAccessException ex) {
      throw new ExternalTestingException(ENDPOINT_ERROR_CODE, 500, ex.getMessage(), ex);
    }
    catch (Exception ex) {
      throw new ExternalTestingException(ENDPOINT_ERROR_CODE, 500, "Generic Exception " + ex.getMessage(), ex);
    }
    if (re != null) {
      logger.debug("http status of {} from external testing entity {}", re.getStatusCodeValue(), url);
      return re.getBody();
    }
    else {
      logger.error("null response from endpoint");
      return null;
    }
  }

  /**
   * Errors from the endpoint could conform to the expected ETSI body or not.
   * Here we try to handle various response body elements.
   * @param statusCode http status code in response.
   * @param o JSON object parsed from the http response body
   * @return Testing error body that should be returned to the caller
   */
  private ExternalTestingException buildTestingException(int statusCode, JsonObject o) {
    String code = null;
    String message = null;

    if (o.has(CODE)) {
      code = o.get(CODE).getAsString();
    }
    else if (o.has(ERROR)) {
      code = o.get(ERROR).getAsString();
    }
    else {
      if (o.has(HTTP_STATUS)) {
        code = o.get(HTTP_STATUS).getAsJsonPrimitive().getAsString();
      }
    }
    if (o.has(MESSAGE)) {
      if (!o.get(MESSAGE).isJsonNull()) {
        message = o.get(MESSAGE).getAsString();
      }
    }
    else if (o.has(DETAIL)) {
      message = o.get(DETAIL).getAsString();
    }
    if (o.has(PATH)) {
      if (message == null) {
        message = o.get(PATH).getAsString();
      }
      else {
        message = message + " " + o.get(PATH).getAsString();
      }
    }
    return new ExternalTestingException(code, statusCode, message);
  }

  void attachArchiveContent(VtpTestExecutionRequest test, MultiValueMap<String, Object> body) {
    Map<String, String> params = test.getParameters();
    String vspId = params.get(VSP_ID);
    String version = params.get(VSP_VERSION);

    try {
      extractMetadata(test, body, vspId, version);
    } catch (IOException ex) {
      logger.error("metadata extraction failed", ex);
    }
  }

  /**
   * Extract the metadata from the VSP CSAR file.
   *
   * @param requestItem item to add metadata to for processing
   * @param vspId       VSP identifier
   * @param version     VSP version
   */
  private void extractMetadata(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body, String vspId, String version) throws IOException {

    Version ver = new Version(version);
    logger.debug("attempt to retrieve archive for VSP {} version {}", vspId, ver.getId());

    Optional<Pair<String, byte[]>> ozip = candidateManager.get(vspId, ver);
    if (!ozip.isPresent()) {
      ozip = vendorSoftwareProductManager.get(vspId, ver);
    }

    if (!ozip.isPresent()) {
      List<Version> versions = versioningManager.list(vspId);
      String knownVersions = versions
          .stream()
          .map(v -> String.format("%d.%d: %s (%s)", v.getMajor(), v.getMinor(), v.getStatus(), v.getId()))
          .collect(Collectors.joining("\n"));

      String detail = String.format("Archive processing failed.  Unable to find archive for VSP ID %s and Version %s.  Known versions are:\n%s",
          vspId, version, knownVersions);

      throw new ExternalTestingException(SDC_RESOLVER_ERR, 500, detail);
    }

    // safe here to do get.
    Pair<String, byte[]> zip = ozip.get();
    processArchive(requestItem, body, zip.getRight());
  }

  private void processArchive(final VtpTestExecutionRequest test, final MultiValueMap<String, Object> body, final byte[] zip) {

    // We need to make one pass through the zip input stream.  Pull out files that match our expectations into a temporary
    // map that we can process over.   These are not huge files so we shouldn't need to worry about memory.

    List<String> extensions = Arrays.asList(".yaml", ".meta", ".yml", ".json", ".env");
    final Map<String, byte[]> contentWeCareAbout = extractRelevantContent(zip, extensions);

    // VTP does not support concurrent executions of the same test with the same associated file name.
    // It writes files to /tmp and if we were to send two requests with the same file, the results are unpredictable.
    String key = UUID.randomUUID().toString();
    key = key.substring(0, key.indexOf('-'));

    // if there's a MANIFEST.json file, we're dealing with a heat archive.
    // otherwise, we will treat it as a CSAR.
    if (contentWeCareAbout.containsKey(MANIFEST_JSON)) {
      byte[] data = processHeatArchive(contentWeCareAbout);
      if (data != null) {
        body.add("file", new NamedByteArrayResource(data, key + ".heat.zip"));
        test.getParameters().put(SDC_HEAT, FILE_URL_PREFIX + key + ".heat.zip");
      }
    }
    else {
      byte[] data = processCsarArchive(contentWeCareAbout);
      if ((data != null) && (data.length != 0)) {
        body.add("file", new NamedByteArrayResource(data, key + ".csar.zip"));
        test.getParameters().put(SDC_CSAR, FILE_URL_PREFIX + key + ".csar.zip");
      }
    }
  }

  /**
   * Process the archive as a heat archive.  Load the MANIFEST.json file and pull out the referenced
   * heat and environment files.
   * @param content relevant content from the heat archive.
   * @return byte array of client to send to endpoint.
   */
  private byte[] processHeatArchive(Map<String,byte[]> content) {
    byte[] manifestBytes = content.get(MANIFEST_JSON);
    ManifestContent manifest = JsonUtil.json2Object(new String(manifestBytes), ManifestContent.class);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try(ZipOutputStream zipOutput = new ZipOutputStream(baos)) {
      for(FileData item : manifest.getData()) {
        processManifestItem(item, zipOutput, content);
      }

      return baos.toByteArray();
    } catch (IOException ex) {
      logger.error("IO Exception parsing zip", ex);
      throw new ExternalTestingException(SDC_RESOLVER_ERR, 500, ex.getMessage());
    }
  }

  private void processManifestItem(FileData item, ZipOutputStream zipOutput, Map<String,byte[]> contentMap) throws IOException {
    if ((item.getType() == FileData.Type.HEAT) || (item.getType() == FileData.Type.HEAT_ENV)) {
      byte[] content = contentMap.get(item.getFile());
      if (content == null) {
        logger.warn("manifest included {} but not in content extracted", item.getFile());
      }
      else {
        ZipEntry zi = new ZipEntry(item.getFile());
        zipOutput.putNextEntry(zi);
        zipOutput.write(content);
        zipOutput.closeEntry();
      }

      // recurse
      if (item.getData() != null) {
        for(FileData subitem: item.getData()) {
          processManifestItem(subitem, zipOutput, contentMap);
        }
      }
    }
  }

  /**
   * Process the archive as a CSAR file.
   * @param content relevant extracted content.
   * @return byte array of client to send to endpoint.
   */
  private byte[] processCsarArchive(Map<String,byte[]> content) {
    // look for the entry point file.
    String fileToGet = null;
    if (content.containsKey(TOSCA_META)) {
      fileToGet = getEntryDefinitionPointer(content.get(TOSCA_META)).orElse(null);
    }
    if (fileToGet == null) {
      // fall back to the SDC standard location.  not required to be here though...
      fileToGet = MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME;
    }

    if (!content.containsKey(fileToGet)) {
      // user story says to let the call to the VTP go through without the attachment.
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try(ZipOutputStream zipOutput = new ZipOutputStream(baos)) {
      processCsarArchiveEntry(fileToGet, zipOutput, content);
      return baos.toByteArray();
    } catch (IOException ex) {
      logger.error("IO Exception parsing zip", ex);
      throw new ExternalTestingException(SDC_RESOLVER_ERR, 500, ex.getMessage());
    }
  }

  /**
   * Add the named file (if it exists) from the contentMap into the output zip to send to VTP.
   * If the file is a yaml file, peek inside and also add any imported files.
   * @param filename name to apply to the zip entry being created
   * @param zipOutput zip output stream to append new entry to
   * @param contentMap map of content we are processing
   * @throws IOException thrown in the event of processing errors.
   */
  private void processCsarArchiveEntry(String filename, ZipOutputStream zipOutput, Map<String, byte[]> contentMap) throws IOException {
    byte[] content = contentMap.get(filename);
    if (content == null) {
      // no such content, just return.
      return;
    }

    ZipEntry zi = new ZipEntry(filename);
    zipOutput.putNextEntry(zi);
    zipOutput.write(content);
    zipOutput.closeEntry();

    // if this is a yaml file, we should peek inside for includes.
    if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
      Yaml yaml = new Yaml();
      @SuppressWarnings("unchecked")
      Map<String, Object> yamlContent = (Map<String, Object>) yaml.load(new ByteArrayInputStream(content));
      if (!yamlContent.containsKey("imports")) {
        return;
      }

      Object imports = yamlContent.get("imports");
      if (imports instanceof ArrayList) {
        @SuppressWarnings("unchecked") ArrayList<String> lst = (ArrayList<String>) imports;
        for (String imp : lst) {
          File f = new File(filename);
          File impFile = new File(f.getParent(), imp);
          logger.debug("look for import {} with {}", imp, impFile.getPath());
          processCsarArchiveEntry(impFile.getPath(), zipOutput, contentMap);
        }
      }
      else {
        logger.warn("archive {} contains imports but it is not an array.  Unexpected, this is.", filename);
      }
    }
  }

  private Optional<String> getEntryDefinitionPointer(byte[] toscaMetadataFile) {
    try {
      Properties p = new Properties();
      p.load(new ByteArrayInputStream(toscaMetadataFile));
      return Optional.ofNullable(p.getProperty(TOSCA_META_ENTRY_DEFINITIONS));
    }
    catch (IOException ex) {
      logger.error("failed to process tosca metadata file {}", TOSCA_META, ex);
    }

    return Optional.empty();
  }

  /**
   * We don't want to send the entire CSAR file to VTP.  Here we take a pass through the
   * archive (heat/csar) file and pull out the files we care about.
   * @param zip csar/heat zip to iterate over
   * @return relevant content from the archive file as a map.
   */
  private Map<String, byte[]> extractRelevantContent(final byte[] zip, final List<String> extensions) {
    final Map<String, byte[]> rv = new HashMap<>();  // FYI, rv = return value.
    try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zip))) {
      ZipEntry entry;
      while ((entry = zipStream.getNextEntry()) != null) {
        final String entryName = entry.getName();

        // NOTE: leaving this debugging in for dublin...
        logger.debug("archive contains entry {}", entryName);

        int idx = entryName.lastIndexOf('.');
        if ((idx >= 0) && (extensions.contains(entryName.substring(idx)))) {
          byte[] content = IOUtils.toByteArray(zipStream);
          rv.put(entryName, content);
        }
      }
    }
    catch (IOException ex) {
      logger.error("error encountered processing archive", ex);
      throw new ExternalTestingException(SDC_RESOLVER_ERR, 500, ex.getMessage());
    }
    return rv;
  }

  /**
   * We need to name the byte array we add to the multipart request sent to the VTP.
   */
  @EqualsAndHashCode(callSuper = false)
  protected class NamedByteArrayResource extends ByteArrayResource {
    private String filename;
    NamedByteArrayResource(byte[] bytes, String filename) {
      super(bytes, filename);
      this.filename = filename;
    }
    @Override
    public String getFilename() {
      return this.filename;
    }
  }


}