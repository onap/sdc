package org.openecomp.sdc.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockserver.model.HttpCallback.callback;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnResult;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
//import org.openecomp.sdc.dmaap.DmaapPublisher;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IConfiguration;
import org.openecomp.sdc.api.consumer.IFinalDistrStatusMessage;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.cucumber.spring.ImportTableConfig;
import org.openecomp.sdc.http.HttpAsdcClient;
import org.openecomp.sdc.http.HttpAsdcResponse;
import org.openecomp.sdc.http.IHttpAsdcClient;
import org.openecomp.sdc.impl.DistributionClientFactory;
import org.openecomp.sdc.utils.ArtifactTypeEnum;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fj.data.Either;

public class StepsTenantIsolationCI {

	private Map<String, OperationalEnvironmentEntry> recordMap = new HashMap<>();
	private OperationalEnvironmentDao operationalEnvironmentDao;
	private ClientAndServer aaiMockServer;
	private ClientAndServer msoMockServer;
	private List<IDistributionClient> distributionClients = new ArrayList<>();

	private List<String> wdFinalStatusSent = new ArrayList<>();

	private HttpRequest msoHttpRequest;
	private List<String> uuidServicesList;
	private boolean cleanFlag = true;
	private int maxStepWaitTimeMs;

	private List<String> envIds = new ArrayList<>();
	private List<OperationalEnvironmentEntry> envList = new ArrayList<>();
	private String asdcAddress;
	private volatile int numberOfFinalDistributionsSentByASDC;
	private int numOfArtifactsToDownload;
	private volatile int totalNumOfArtifactsToDownload;
	private List<String> envNames = new ArrayList<>(Arrays.asList("Apple", "Orange", "Grape", "Pear", "Watermelon", "Bannana", "Cherry", "Coconut", "Fig", "Mango", "Peach", "Pineapple", "Plum", "Strawberries", "Apricot"));

	private static final String AAI_RESPONSE_BODY_FMT = "{\"operational-environment-id\":\"UUID of Operational Environment\","
			+ "\"operational-environment-name\":\"Op Env Name\"," + "\"operational-environment-type\":\"ECOMP\","
			+ "\"operational-environment-status\":\"Activate\"," + "\"tenant-context\":\"%s\","
			+ "\"workload-context\":\"%s\"," + "\"resource-version\":\"1505228226913\"," + "\"relationship-list\":{}}";

	private static final String AAI_PATH_REGEX = "/aai/v12/cloud-infrastructure/operational-environments/.*";

	private static final String MSO_PATH_REGEX = "/onap/mso/infra/modelDistributions/v1/distributions/.*";

	@Before
	public void beforeScenario() {
		Collections.shuffle(envNames);
		aaiMockServer = ClientAndServer.startClientAndServer(1111);
		msoMockServer = ClientAndServer.startClientAndServer(1112);

		this.operationalEnvironmentDao = createDaoObj();
	}

	@After
	public void afterScenario() {
		System.out.println("Cleaning Up After Scenario...");
		aaiMockServer.stop();
		msoMockServer.stop();
		if (cleanFlag) {
			envIds.stream().forEach(operationalEnvironmentDao::delete);
		}
		distributionClients.stream().forEach(IDistributionClient::stop);
		System.out.println("Cleaning Up After Scenario Done");
	}

	// ############################# Given - Start #############################

	@Given("^clean db after test is (.*)$")
	public void clean_db_after_test_is(boolean cleanFlag) {
		this.cleanFlag = cleanFlag;
	}

	@Given("^AAI returns (.*) and aai_body contains (.*) and (.*)$")
	public void aai_returns(int retCode, String tenant, String workload) throws Throwable {
		String aaiResponseBody = String.format(AAI_RESPONSE_BODY_FMT, tenant, workload);

		setAaiMockServer(aaiResponseBody);

		System.out.println(aaiMockServer.getClass());
	}

	@Given("^MSO-WD Simulators Started with topic name (.*)$")
	public void notification_listner_simulators_started(String topicName) throws Throwable {
		envList.forEach(env -> {
			final IDistributionClient distClientSim = simulateDistributionClientWD(topicName, env);
			distributionClients.add(distClientSim);
		});

	}

	@Given("^MSO Final Distribution Simulator is UP$")
	public void mso_Final_Distribution_Simulator_is_UP() throws Throwable {
		msoHttpRequest = request().withPath(MSO_PATH_REGEX);
		msoMockServer.when(msoHttpRequest).callback(
				callback().withCallbackClass("org.openecomp.sdc.cucumber.steps.PrecannedTestExpectationCallback"));
	}

	@Given("^ASDC Address is (.*)$")
	public void asdc_Address_is(String asdcAddress) throws Throwable {
		this.asdcAddress = asdcAddress;

	}

	@Given("^ASDC Contains the following services (.*)$")
	public void asdc_Contains_the_following_services(String listOfServicesUUID) throws Throwable {
		uuidServicesList = Arrays.asList(listOfServicesUUID.split(",")).stream().map(String::trim)
				.collect(Collectors.toList());

		int maxMinWait = uuidServicesList.size() * envIds.size();
		this.maxStepWaitTimeMs = 60000 * maxMinWait;
		System.out.println(String.format("Set Max Step Wait Time To: %s Minutes", maxMinWait));
	}

	@Given("^The number of complete environments is (.*)$")
	public void the_number_of_complete_environments_is(int envNum) throws Throwable {

		int counter = 1;
		while( envNum > envNames.size()){
			envNames.add(String.valueOf(counter));
			counter++;
		}
		
		
		for (int i = 0; i < envNum; i++) {
			OperationalEnvironmentEntry preSaveEntry = new OperationalEnvironmentEntry();

			preSaveEntry.setStatus(EnvironmentStatusEnum.COMPLETED);
			preSaveEntry.setLastModified(new Date(System.currentTimeMillis()));

			Set<String> uebAdresses = new HashSet<>();
			uebAdresses.add("uebsb92sfdc.it.att.com");
			preSaveEntry.setDmaapUebAddress(uebAdresses);
			preSaveEntry.setIsProduction(false);
			preSaveEntry.setUebApikey("sSJc5qiBnKy2qrlc");
			preSaveEntry.setUebSecretKey("4ZRPzNJfEUK0sSNBvccd2m7X");
			preSaveEntry.setTenant("TEST");
			preSaveEntry.setEcompWorkloadContext("ECOMP_E2E-IST");

			// String envId = UUID.randomUUID().toString();
			String envId = envNames.get(i);
			preSaveEntry.setEnvironmentId(envId);
			envIds.add(envId);
			envList.add(preSaveEntry);
			operationalEnvironmentDao.save(preSaveEntry);
		}
	}

	@Given("^The number of artifacts each Simulator downloads from a service is (.*)$")
	public void number_of_artifacts_each_simulator_downloads(int numOfArtifactsToDownload) throws Throwable {
		this.numOfArtifactsToDownload = numOfArtifactsToDownload;
	}

	// ############################# Given - End #############################

	// ############################# When - Start #############################
	@When("^Distribution Requests are Sent By MSO$")
	public void distribution_Requests_are_Sent_By_MSO() throws Throwable {
		envList.stream().forEach(this::distributeServiceInEnv);

	}

	@When("^The Number Of Operational Envrinoments that created is (.*) and Records are added with data (.*)$")
	public void operational_envrinoments_records_are_added_with_data(int numOfRecords, String recordData)
			throws Throwable {
		for (int i = 0; i < numOfRecords; i++) {
			OperationalEnvironmentEntry preSaveEntry = new OperationalEnvironmentEntry();
			JsonElement root = new JsonParser().parse(recordData);

			String originalStatus = root.getAsJsonObject().get("status").getAsString();
			int delta = root.getAsJsonObject().get("last_modified_delta").getAsInt();
			preSaveEntry.setStatus(EnvironmentStatusEnum.getByName(originalStatus));
			long last_modified = System.currentTimeMillis() + delta * 1000;
			preSaveEntry.setLastModified(new Date(last_modified));
			String envId = UUID.randomUUID().toString();
			preSaveEntry.setEnvironmentId(envId);
			envIds.add(envId);
			// envrionmentIds.add(envId);
			operationalEnvironmentDao.save(preSaveEntry);

		}

	}

	// ############################# When - End #############################

	// ############################# Then - Start #############################
	@Then("^Operational Environment record contains tenant field (.*$)")
	public void operational_environment_record_contains_tenant(boolean tenantExist) throws Throwable {
		envIds.forEach(envId -> {
			validateStringFieldPresent(tenantExist, OperationalEnvironmentEntry::getTenant, envId,
					"Tenant is not as expected");
		});
	}

	@Then("^Operational Environment record contains workload field (.*$)")
	public void operational_environment_record_contains_workload(boolean workloadExist) throws Throwable {
		envIds.forEach(envId -> {
			validateStringFieldPresent(workloadExist, OperationalEnvironmentEntry::getEcompWorkloadContext, envId,
					"Workload is not as expected");
		});
	}

	@Then("^Operational Environment record contains UEB Address field (.*$)")
	public void operational_environment_record_contains_ueb_address(boolean uebAddresExist) throws Throwable {
		envIds.forEach(envId -> {
			validateStringFieldPresent(uebAddresExist, this::convertUebAddressToList, envId,
					"UEB Address is not as expected");
		});
	}

	@Then("^The Number Of Environment is (.*) with status (.*)$")
	public void the_Number_Of_Environment_Created_is(int numberOfEnvsCreated, String status) throws Throwable {
		// Write code here that turns the phrase above into concrete actions

		retryMethodOnResult(() -> getCurrentEnvironmets(status), envList -> envList.size() == numberOfEnvsCreated,
				40000, 500);

		List<OperationalEnvironmentEntry> environmentsFound = getCurrentEnvironmets(status);

		assertThat(environmentsFound.size(), is(numberOfEnvsCreated));

		environmentsFound.forEach(env -> recordMap.put(env.getEnvironmentId(), env));
		envList.addAll(environmentsFound);
	}

	@Then("^MSO Final Distribution Recieved Correct Number Of Request$")
	public void mso_final_distribution_recieved_request() throws Throwable {
		int expectedNumberOfRequestsSentByASDC = calculateExcpectedNumberOfDistributionRequets();
		Function<Integer, Boolean> resultVerifier = actualStatusList -> actualStatusList >= expectedNumberOfRequestsSentByASDC;
		retryMethodOnResult(() -> numberOfFinalDistributionsSentByASDC(expectedNumberOfRequestsSentByASDC),
				resultVerifier, maxStepWaitTimeMs, 500);
		int actualNumberOfRequestsSentByASDC = numberOfFinalDistributionsSentByASDC(expectedNumberOfRequestsSentByASDC);
		assertThat(actualNumberOfRequestsSentByASDC, is(expectedNumberOfRequestsSentByASDC));
	}

	@Then("^All MSO-WD Simulators Sent The Distribution Complete Notifications$")
	public void all_mso_wd_simulators_sent_the_distribution_complete_notifications() {
		// Wait Watch Dogs To Send Final Distribution
		int excpectedNumberOfDistributionCompleteNotifications = calculateExcpectedNumberOfDistributionRequets();
		final Function<List<String>, Boolean> resultVerifier = actualStatusList -> actualStatusList
				.size() == excpectedNumberOfDistributionCompleteNotifications;
		retryMethodOnResult(() -> wdFinalStatusSent, resultVerifier, maxStepWaitTimeMs, 500);
		assertThat(wdFinalStatusSent.size(), is(excpectedNumberOfDistributionCompleteNotifications));
	}
	@Then("^All Artifacts were downloaded by Simulators$")
	public void all_artifacts_downloaded() {
		// Wait Watch Dogs To Send Final Distribution
		int excpectedNumberOfArtifactsToDownload = numOfArtifactsToDownload * envIds.size() * uuidServicesList.size();
		assertThat(totalNumOfArtifactsToDownload, is(excpectedNumberOfArtifactsToDownload));
	}
	// ############################# Then - End #############################

	private List<OperationalEnvironmentEntry> getCurrentEnvironmets(String status) {
		Either<List<OperationalEnvironmentEntry>, CassandraOperationStatus> eitherStatus = operationalEnvironmentDao
				.getByEnvironmentsStatus(EnvironmentStatusEnum.getByName(status));
		assertThat(eitherStatus.isLeft(), is(true));

		List<OperationalEnvironmentEntry> environmentsRetrieved = eitherStatus.left().value();

		List<OperationalEnvironmentEntry> environmentsFound = environmentsRetrieved.stream()
				.filter(env -> envIds.contains(env.getEnvironmentId())).collect(Collectors.toList());
		return environmentsFound;
	}

	private int numberOfFinalDistributionsSentByASDC(int expectedNumberOfRequestsSentByASDC) {
		final int newVal = msoMockServer.retrieveRecordedRequests(msoHttpRequest).length;
		if (newVal != numberOfFinalDistributionsSentByASDC) {
			System.out.println(String.format(
					"MSO Server Simulator Recieved %s/%s Final Distribution Complete Rest Reports From ASDC", newVal,
					expectedNumberOfRequestsSentByASDC));
			numberOfFinalDistributionsSentByASDC = newVal;
		}

		return newVal;
	}

	private int calculateExcpectedNumberOfDistributionRequets() {
		int numberOfDistributionRequests = envList.size() * uuidServicesList.size();
		return numberOfDistributionRequests;
	}

	private static OperationalEnvironmentDao createDaoObj() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ImportTableConfig.class);
		final OperationalEnvironmentDao openvDao = (OperationalEnvironmentDao) context
				.getBean("operational-environment-dao");
		return openvDao;
	}

	private OperationalEnvironmentEntry getRecord(String environmentId) {
		Either<OperationalEnvironmentEntry, CassandraOperationStatus> result = operationalEnvironmentDao
				.get(environmentId);
		return result.isLeft() ? result.left().value() : null;
	}

	private void validateStringFieldPresent(boolean fieldExist,
			Function<OperationalEnvironmentEntry, String> getFieldFunc, String envId, String msg) {
		OperationalEnvironmentEntry record = recordMap.computeIfAbsent(envId, this::getRecord);

		assertNotNull(record, "Expected DB record was not found");

		String actualValue = getFieldFunc.apply(record);

		assertEquals(fieldExist, !Strings.isNullOrEmpty(actualValue), msg);
	}

	private void setAaiMockServer(String aaiResponseBody) {
		HttpRequest httpRequest = request().withMethod("GET").withPath(AAI_PATH_REGEX)
				.withHeaders(new Header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()));

		aaiMockServer.when(httpRequest)
				.respond(response()
						.withHeaders(new Header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
						.withBody(aaiResponseBody));
	}

	private IDistributionClient simulateDistributionClientWD(String topicName, OperationalEnvironmentEntry opEnv) {

		IDistributionClient client = DistributionClientFactory.createDistributionClient();
		final IConfiguration buildDistributionClientConfiguration = buildDistributionClientConfiguration(topicName,
				opEnv);
		IDistributionClientResult initResult = client.init(buildDistributionClientConfiguration,
				new INotificationCallback() {

					@Override
					public void activateCallback(INotificationData data) {
						buildWdSimulatorCallback(opEnv, client, buildDistributionClientConfiguration, data);

					}

				});
		assertThat(initResult.getDistributionActionResult(), is(DistributionActionResultEnum.SUCCESS));
		IDistributionClientResult startResult = client.start();
		assertThat(startResult.getDistributionActionResult(), is(DistributionActionResultEnum.SUCCESS));
		System.out.println(String.format("WD Simulator On Environment:\"%s\" Started Successfully",
				buildDistributionClientConfiguration.getConsumerID()));
		return client;
	}

	private IFinalDistrStatusMessage buildFinalDistribution() {
		return new IFinalDistrStatusMessage() {

			@Override
			public long getTimestamp() {
				return System.currentTimeMillis();
			}

			@Override
			public DistributionStatusEnum getStatus() {
				return DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK;
			}

			@Override
			public String getDistributionID() {
				return "FakeDistributionId";
			}
		};
	}

	private IConfiguration buildDistributionClientConfiguration(String topicName, OperationalEnvironmentEntry opEnv) {
		return new IConfiguration() {

			public String getUser() {
				return "ci";
			}

			public int getPollingTimeout() {
				return 20;
			}

			public int getPollingInterval() {
				return 20;
			}

			public String getPassword() {
				return "123456";
			}

			public String getEnvironmentName() {
				return topicName;
			}

			public String getConsumerID() {
				return opEnv.getEnvironmentId();
			}

			public String getConsumerGroup() {
				return String.format("BenchMarkDistributionClientConsumerGroup%s", opEnv.getEnvironmentId());
			}

			public String getAsdcAddress() {
				return String.format("%s:8443", asdcAddress);
			}

			@Override
			public String getKeyStorePath() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getKeyStorePassword() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean activateServerTLSAuth() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public List<String> getRelevantArtifactTypes() {
				return Arrays.asList(ArtifactTypeEnum.values()).stream()
						.map(artifactTypeEnum -> artifactTypeEnum.name()).collect(Collectors.toList());
			}

			@Override
			public boolean isFilterInEmptyResources() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Boolean isUseHttpsWithDmaap() {
				return null;
			}

			@Override
			public List<String> getMsgBusAddress() {
				return opEnv.getDmaapUebAddress().stream().map(this::extractHost).collect(Collectors.toList());
			}

			private String extractHost(String url) {
				return url.split(":")[0];
			}
		};
	}

	private void distributeServiceInEnv(OperationalEnvironmentEntry env) {
		uuidServicesList.stream().forEach(serviceUUID -> distributeSingleService(env, serviceUUID));
	}

	private void distributeSingleService(OperationalEnvironmentEntry env, String serviceUUID) {
		IHttpAsdcClient client = new HttpAsdcClient(buildDistributionClientConfiguration(StringUtils.EMPTY, env));
		String pattern = "/sdc/v1/catalog/services/%s/distribution/%s/activate";
		String requestUrl = String.format(pattern, serviceUUID, env.getEnvironmentId());
		String requestBody = String.format("{\"workloadContext\":\"%s\"}", env.getEnvironmentId());
		StringEntity body = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("USER_ID", "cs0008");
		headersMap.put("X-ECOMP-InstanceID", "test");
		headersMap.put("Content-Type", "application/json");

		HttpAsdcResponse postRequest = FunctionalInterfaces.retryMethodOnResult(
				() -> distributeMethod(client, requestUrl, serviceUUID, body, headersMap, env.getEnvironmentId()),
				resp -> resp.getStatus() == HttpStatus.SC_ACCEPTED);
		assertThat(postRequest.getStatus(), is(HttpStatus.SC_ACCEPTED));

	}

	private HttpAsdcResponse distributeMethod(IHttpAsdcClient client, String requestUrl, String serviceUUID,
			StringEntity body, Map<String, String> headersMap, String envId) {

		final HttpAsdcResponse postRequest = client.postRequest(requestUrl, body, headersMap);
		final String message = String.format(
				"MSO Client Simulator Distributes Service:%s On Environment:\"%s\" - Recieved Response: %s", serviceUUID, envId,
				postRequest.getStatus());
		if (postRequest.getStatus() != HttpStatus.SC_ACCEPTED) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}

		return postRequest;
	}

	private String convertUebAddressToList(OperationalEnvironmentEntry op) {
		Set<String> dmaapUebAddress = op.getDmaapUebAddress();
		Wrapper<String> resultWrapper = new Wrapper<>(StringUtils.EMPTY);
		if (!CollectionUtils.isEmpty(dmaapUebAddress)) {
			dmaapUebAddress.stream()
					.forEach(uebAddress -> resultWrapper.setInnerElement(resultWrapper.getInnerElement() + uebAddress));
		}

		return resultWrapper.getInnerElement();
	}

	private void buildWdSimulatorCallback(OperationalEnvironmentEntry opEnv, IDistributionClient client,
			final IConfiguration buildDistributionClientConfiguration, INotificationData data) {
		if (StringUtils.equals(data.getWorkloadContext(), opEnv.getEnvironmentId())) {
			
			final String expectedArtifactType = org.openecomp.sdc.common.api.ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.name();
			Optional<IArtifactInfo> optional = data.getServiceArtifacts().stream().filter( artifact -> {
				return StringUtils.equals(artifact.getArtifactType(), expectedArtifactType);
			}).findAny();
			
			for (int i = 0; i < numOfArtifactsToDownload; i++) {
				optional.ifPresent( artifactInfo -> simulateDownload(client , artifactInfo, data) );
				optional.orElseThrow( () -> handleArtifactNotFound(expectedArtifactType, data));
				
			}
			if(  numOfArtifactsToDownload > 0 ){
				System.out.println(String.format(
						"ASDC Consumer Simulator On Environment:\"%s\" Downloaded %s Artifacts From ASDC Service with UUID:%s  Total Artifacts Downloaded from ASDC is: %s",
						data.getWorkloadContext(), numOfArtifactsToDownload,
						data.getServiceUUID(), totalNumOfArtifactsToDownload));
			}
			
			IDistributionClientResult finalDistrStatus = client.sendFinalDistrStatus(buildFinalDistribution());
			assertThat(finalDistrStatus.getDistributionActionResult(), is(DistributionActionResultEnum.SUCCESS));
			wdFinalStatusSent.add(data.getWorkloadContext());

			System.out.println(String.format(
					"WD Simulator On Environment:\"%s\" Recieved Notification From ASDC On WorkLoad: %s And Service UUID:%s And Sends Distribution Complete Notification",
					buildDistributionClientConfiguration.getConsumerID(), data.getWorkloadContext(),
					data.getServiceUUID()));
		}
	}
	
	private IllegalStateException handleArtifactNotFound(String expectedArtifactType, INotificationData data ){
		final String stringMessage = String.format("Did Not Find Artifact of type: %s to download from service with UUID:%s", expectedArtifactType, data.getServiceUUID());
		System.err.println(stringMessage); 
		return new IllegalStateException(stringMessage);
	 
	}
	
	private void simulateDownload(IDistributionClient client, IArtifactInfo info, INotificationData data){
		IDistributionClientDownloadResult downloadResult = client.download(info);
		if( downloadResult.getDistributionActionResult() != DistributionActionResultEnum.SUCCESS ){
			System.err.println(String.format("Client Simulator %s Failed to download artifact from service : %s", client.getConfiguration().getConsumerID(), data.getServiceUUID()));
			assertThat(downloadResult.getDistributionActionResult(), is(DistributionActionResultEnum.SUCCESS));
		}
		else{
			totalNumOfArtifactsToDownload++;
		
		}
	}
}
