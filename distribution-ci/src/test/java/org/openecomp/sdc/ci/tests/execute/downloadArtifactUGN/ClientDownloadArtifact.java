package org.openecomp.sdc.ci.tests.execute.downloadArtifactUGN;

import org.junit.Test;

/**
 * 
 * @author al714h
 * US510007 - Story : ASDC Distr Client - Download Artifact
 * following test set partially cover the US451327 - Story : API to download the specific artifact, cover the audit message.
 */

//public class ClientDownloadArtifact extends AttSdcTest{
	public class ClientDownloadArtifact {
	
//		Logger logger = null;
//		
//	DistributionUtils distributionUtils = new DistributionUtils();
//	DownloadArtifactDetails downloadArtifactDetails = new DownloadArtifactDetails();
//	Utils utils = new Utils();
//	DbUtils dbUtils = new DbUtils();
//	UserUtils userUtils = new UserUtils();
//	ResourceUtils resourceUtils = new ResourceUtils();
//	ServiceUtils serviceUtils = new ServiceUtils();
//	ArtifactUtils artifactUtils = new ArtifactUtils();
//	private JSONParser jsonParser = new JSONParser();
//	
	String serviceBaseVersion = "0.1";
//	
//	@Before
//	public void before() throws Exception {
//		
//		distributionUtils.resetInit();
//		dbUtils.cleanAllAudits();
//	}
//	@Rule 
//    public static TestName name = new TestName();
//	
//	}
//	
//	@Rule 
//    public static TestName name = new TestName();
//	
////	public ClientDownloadArtifact() {
////		super(name, ClientDownloadArtifact.class.getName());
////	}
//
//		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
////		get default artifact info to upload
//		ArtifactDefinition uploadedResourceArtifactInfo = artifactUtils.constructDefaultArtifactInfo();
////		set artifact info for download
//		downloadArtifactDetails.setArtifactName(uploadedResourceArtifactInfo.getArtifactName());
//		String artifactName = downloadArtifactDetails.getArtifactName();
////	Andrey TODO	String resourceArtifactUrl = String.format(Urls.DOWNLOAD_RESOURCE_ARTIFACT,);
//		
//		int numOfResourcArtifacts = 1;
//		RestResponse createCertifiedResourceWithArtifacts = resourceUtils.createCertifiedResourceWithArtifacts(resourceDetails, uploadedResourceArtifactInfo, numOfResourcArtifacts);
//		
//		
//		
//		try{
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//			RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//			distributionUtils.verifyDownloadedArtifact(downloadArtifactRestResponse, uploadedResourceArtifactInfo);
//			
//	//		validate audit message server side
//			ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//			String action = "ArtifactDownload";
//			ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//			distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		}finally{
////			delete created resource
//			resourceUtils.deleteResource_allVersions(resourceDetails, UserUtils.getAdminDetails());
//		}
		
//	}
	
//	send all mandatory headers only
	@Test
	public void downloadServiceArtifactSuccess() throws Exception{
		
////		ServiceReqDetails serviceDetails = serviceUtils.createDefaultDetailsService();
//		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
//		Service service = new Service();
//		try{
////		get default artifact info to upload
//		ArtifactDefinition uploadedResourceArtifactInfo = artifactUtils.constructDefaultArtifactInfo();
//		uploadedResourceArtifactInfo.setArtifactType(ArtifactTypeEnum.HEAT.getType());
//		ArtifactDefinition uploadedServiceArtifactInfo = artifactUtils.constructDefaultArtifactInfo();
//		uploadedServiceArtifactInfo.setArtifactType(ArtifactTypeEnum.MURANO_PKG.getType());
//		
////		set artifact info for download
//		downloadArtifactDetails.setArtifactName(uploadedServiceArtifactInfo.getArtifactName());
//		downloadArtifactDetails.setArtifactType(ArtifactTypeEnum.MURANO_PKG.getType());
//		String artifactName = downloadArtifactDetails.getArtifactName();
//		
////		create default resource details
//		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
//		
//		int numOfResourcArtifacts = 1;
//		service = serviceUtils.createCertifiedServiceWithResourceInstanceAndArtifacts(serviceDetails, resourceDetails, uploadedResourceArtifactInfo, uploadedServiceArtifactInfo , numOfResourcArtifacts);
//		logger.debug("service version = " + service.getVersion());
//		logger.debug("service distribution status = " + service.getDistributionStatus());
//		RestResponse changeDistributionStateToApprove = serviceUtils.changeDistributionStateToApprove(service, UserUtils.getGovernorDetails1());
//		
//		int status = changeDistributionStateToApprove.getErrorCode();
//		assertTrue("response code is not 200, returned :" + status, status == 200);
//		
//		serviceDetails.setVersion(service.getVersion());
//		serviceDetails.setUniqueId(service.getUniqueId());
//
//		String responseString = changeDistributionStateToApprove.getResponse();
//		Gson gson = new Gson();
//		service = gson.fromJson(responseString, Service.class);
//		ServiceMetadataDataDefinition serviceMetadataDataDefinition = gson.fromJson(responseString, ServiceMetadataDataDefinition.class);
//		service.setDistributionStatus(DistributionStatusEnum.findState(serviceMetadataDataDefinition.getDistributionStatus()));
//		DistributionStatusEnum distributionStatus = service.getDistributionStatus();
//		assertNotNull("distribution state is null",distributionStatus.name());
//		assertTrue("the default distribution state is invalid", DistributionStatusEnum.DISTRIBUTION_APPROVED.equals(distributionStatus));
//		
//		String serviceArtifactUrl = String.format(Urls.DISTRIBUTION_DOWNLOAD_ARTIFACT,service.getName(), service.getVersion(), artifactName);
//		logger.debug("download service artifact url: "+ serviceArtifactUrl+ " serviceName = " + service.getName() + " service version = " + service.getVersion() + "service artifact name = " + artifactName);
//		
//		
////			Log.debug("artifact detailes: "+ downloadArtifactDetails.toString());
////			RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
////			distributionUtils.verifyDownloadedArtifact(downloadArtifactRestResponse, uploadedArtifactInfo);
////			
////	//		validate audit message server side
////			ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
////			String action = "ArtifactDownload";
////			ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
////			distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		}finally{
////			delete created service
//			serviceUtils.deleteService_allVersions(serviceDetails, UserUtils.getAdminDetails());
//		}
		
//	@Test
//	public void downloadServiceArtifactSuccess() throws Exception{
//		
////		ServiceReqDetails serviceDetails = serviceUtils.createDefaultDetailsService();
//		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
//		Service service = new Service();
//		try{
////		get default artifact info to upload
//		ArtifactDefinition uploadedResourceArtifactInfo = artifactUtils.constructDefaultArtifactInfo();
//		uploadedResourceArtifactInfo.setArtifactType(ArtifactTypeEnum.HEAT.getType());
//		ArtifactDefinition uploadedServiceArtifactInfo = artifactUtils.constructDefaultArtifactInfo();
//		uploadedServiceArtifactInfo.setArtifactType(ArtifactTypeEnum.MURANO_PKG.getType());
//		
////		set artifact info for download
//		downloadArtifactDetails.setArtifactName(uploadedServiceArtifactInfo.getArtifactName());
//		downloadArtifactDetails.setArtifactType(ArtifactTypeEnum.MURANO_PKG.getType());
//		String artifactName = downloadArtifactDetails.getArtifactName();
//		
////		create default resource details
//		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
//		
//		int numOfResourcArtifacts = 1;
//		service = serviceUtils.createCertifiedServiceWithResourceInstanceAndArtifacts(serviceDetails, resourceDetails, uploadedResourceArtifactInfo, uploadedServiceArtifactInfo , numOfResourcArtifacts);
//		logger.debug("service version = " + service.getVersion());
//		logger.debug("service distribution status = " + service.getDistributionStatus());
//		RestResponse changeDistributionStateToApprove = serviceUtils.changeDistributionStateToApprove(service, UserUtils.getGovernorDetails1());
//		
//		int status = changeDistributionStateToApprove.getErrorCode();
//		assertTrue("response code is not 200, returned :" + status, status == 200);
//		
//		serviceDetails.setVersion(service.getVersion());
//		serviceDetails.setUniqueId(service.getUniqueId());
//
//		String responseString = changeDistributionStateToApprove.getResponse();
//		Gson gson = new Gson();
//		service = gson.fromJson(responseString, Service.class);
//		ServiceMetadataDataDefinition serviceMetadataDataDefinition = gson.fromJson(responseString, ServiceMetadataDataDefinition.class);
//		service.setDistributionStatus(DistributionStatusEnum.findState(serviceMetadataDataDefinition.getDistributionStatus()));
//		DistributionStatusEnum distributionStatus = service.getDistributionStatus();
//		assertNotNull("distribution state is null",distributionStatus.name());
//		assertTrue("the default distribution state is invalid", DistributionStatusEnum.DISTRIBUTION_APPROVED.equals(distributionStatus));
//		
//		String serviceArtifactUrl = String.format(Urls.DISTRIBUTION_DOWNLOAD_ARTIFACT,service.getName(), service.getVersion(), artifactName);
//		logger.debug("download service artifact url: "+ serviceArtifactUrl+ " serviceName = " + service.getName() + " service version = " + service.getVersion() + "service artifact name = " + artifactName);
//		
//		
////			Log.debug("artifact detailes: "+ downloadArtifactDetails.toString());
////			RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
////			distributionUtils.verifyDownloadedArtifact(downloadArtifactRestResponse, uploadedArtifactInfo);
////			
////	//		validate audit message server side
////			ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
////			String action = "ArtifactDownload";
////			ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
////			distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		}finally{
////			delete created service
//			serviceUtils.deleteService_allVersions(serviceDetails, UserUtils.getAdminDetails());
//		}
//		
//	}
//	
////	Andrey not relevant, dev ci should cover, qa can't influence on sending headers 
//////	send all headers mandatory and not mandatory
////	@Test
////	public void downloadArtifactSuccessMandatoryAndNotMandatoryHeaders() throws Exception{
////		
////		Log.debug("artifact detailes: "+ downloadArtifactDetails.toString());
////		
////		Map<String, String> headersMap = new HashMap<String,String>();
////		headersMap.put(HttpHeaderEnum.X_ECOMP_InstanceID.getValue(), "instar_name"); // mandatory
////		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), "usernamePassword"); // mandatory	
////		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/octet-stream"); // not mandatory
////		headersMap.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(),downloadArtifactDetails.getResourceUUID());// not mandatory
////		Log.debug("headers detailes: "+ headersMap.toString());
////		
////		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, headersMap);
////		distributionUtils.verifyDownloadedArtifact(downloadArtifactRestResponse);
////		
//////		validate audit message server side
////		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
////		String action = "ArtifactDownload";
////		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
////		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
////		
////	}
//
//	
////---------------------------------Failure scenario--------------------------------------------------------------------------------
//
////	Andrey not relevant, dev ci should cover, qa can't influence on sending headers 
//////	missing InstanceID mandatory header
////	@Test
////	public void downloadArtifactMissingInstanceIdHeader() throws Exception, JSONException{
////		
////		Log.debug("artifact detailes: "+ downloadArtifactDetails.toString());
////		
////		Map<String, String> headersMap = new HashMap<String,String>();
////		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), "usernamePassword"); // mandatory	
////		Log.debug("headers detailes: "+ headersMap.toString());
////		
////		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, headersMap);
////		
////		ErrorInfo errorInfo = utils.parseYaml(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID.name());
////		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
////		
////		List<String> variables = Arrays.asList();
////		utils.checkBodyResponseOnError(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID.name(), variables, downloadArtifactRestResponse.getResponse()));
////		
////	}	
//	
////	Andrey not relevant, dev ci should cover, qa can't influence on sending headers 
//////	missing Authorization mandatory header
////	@Test
////	public void downloadArtifactMissingAuthorizationHeader() throws Exception, JSONException{
////		
////		Log.debug("artifact detailes: "+ downloadArtifactDetails.toString());
////		
////		Map<String, String> headersMap = new HashMap<String,String>();
////		headersMap.put(HttpHeaderEnum.X_ECOMP_InstanceID.getValue(), "usernamePassword"); // mandatory	
////		Log.debug("headers detailes: "+ headersMap.toString());
////		
////		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, headersMap);
////		
////		ErrorInfo errorInfo = utils.parseYaml(ActionStatus.ECOMP_RESEND_WITH_BASIC_AUTHENTICATION_CREDENTIALS.name());
////		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
////		
////		List<String> variables = Arrays.asList();
////		utils.checkBodyResponseOnError(ActionStatus.ECOMP_RESEND_WITH_BASIC_AUTHENTICATION_CREDENTIALS.name(), variables, downloadArtifactRestResponse.getResponse()));
////		
////	}
//	
//
////	artifact not found
//	@Test
//	public void downloadArtifactArtifactNotFound() throws Exception, JSONException{
//
//		downloadArtifactDetails.setArtifactName("artifactNotExist");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		
//	}
//	
////	service not found
//	@Test
//	public void downloadArtifactServiceNameNotFound() throws Exception, JSONException{
//
//		downloadArtifactDetails.setArtifactURL("/asdc/v1/services/serviceNotExist/0.1/artifacts/aaa.hh");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
////		TODO Andrey, change desc message
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		
//	}
//	
//	
////	service version not found
//	@Test
//	public void downloadArtifactServiceVersionNotFound() throws Exception, JSONException{
//
//		downloadArtifactDetails.setArtifactURL("/asdc/v1/services/serviceName/0.888/artifacts/aaa.hh");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.COMPONENT_VERSION_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.COMPONENT_VERSION_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
////		TODO Andrey, change desc message
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//			
//	}
//	
////	invalid HTTP method, PUT HTTP method
//	@Test
//	public void downloadArtifactByPutMethod() throws Exception, JSONException{
//
//		String method = "PUT";
//		
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendDownloadedArtifactByMethod(downloadArtifactDetails, null, method);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name(), variables, downloadArtifactRestResponse.getResponse());
//		
//	}
//
////	invalid HTTP method, DELETE HTTP method
//	@Test
//	public void downloadArtifactByDeleteMethod() throws Exception, JSONException{
//
//		String method = "DELETE";
//		downloadArtifactDetails.setArtifactName("artifactNotExist");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.DISTRIBUTION_ARTIFACT_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		
//		downloadArtifactDetails.setArtifactURL("/asdc/v1/services/serviceNotExist/0.1/artifacts/aaa.hh");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
////		TODO Andrey, change desc message
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		downloadArtifactDetails.setArtifactURL("/asdc/v1/services/serviceName/0.888/artifacts/aaa.hh");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.COMPONENT_VERSION_NOT_FOUND.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.COMPONENT_VERSION_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse());
//
////		validate audit message server side
//		ArtifactDownloadAuditMessageInfo expectedArtifactDownloadAuditMessageInfo = distributionUtils.constructDefaultArtifactDownloadAuditMessageInfo();
//		expectedArtifactDownloadAuditMessageInfo.setStatus(errorInfo.getCode().toString());
////		TODO Andrey, change desc message
//		String desc = "Error: Artifact " + downloadArtifactDetails.getArtifactName() + " was not found";
//		expectedArtifactDownloadAuditMessageInfo.setDesc(desc);
//		
//		String action = "ArtifactDownload";
//		ArtifactDownloadAuditMessageInfo actualArtifactDownloadAuditMessageInfo = userUtils.parseDestributionAuditRespByAction(action);
//		distributionUtils.validateArtifactDownloadAuditMessage(actualArtifactDownloadAuditMessageInfo, expectedArtifactDownloadAuditMessageInfo);
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendDownloadedArtifactByMethod(downloadArtifactDetails, null, method);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name(), variables, downloadArtifactRestResponse.getResponse());
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendDownloadedArtifactByMethod(downloadArtifactDetails, null, method);
//		
//		ErrorInfo errorInfo = utils.parseErrorConfigYaml(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name());
//		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
//		
//		List<String> variables = Arrays.asList();
//		utils.checkBodyResponseOnError(ActionStatus.METHOD_NOT_ALLOWED_TO_DOWNLOAD_ARTIFACT.name(), variables, downloadArtifactRestResponse.getResponse());
//		downloadArtifactDetails.setArtifactChecksum("invalidChecksum");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
////		TODO
//		
////		ErrorInfo errorInfo = utils.parseYaml(ActionStatus.SPECIFIED_SERVICE_RESOURCE_VERSION_NOT_FOUND.name());
////		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
////		
////		List<String> variables = Arrays.asList();
////		utils.checkBodyResponseOnError(ActionStatus.SPECIFIED_SERVICE_RESOURCE_VERSION_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse()));
//
//		downloadArtifactDetails.setArtifactChecksum("invalidChecksum");
//		logger.debug("artifact detailes: "+ downloadArtifactDetails.toString());
//		
//		RestResponse downloadArtifactRestResponse = distributionUtils.sendGetDownloadedArtifact(downloadArtifactDetails, null);
////		TODO
//		
////		ErrorInfo errorInfo = utils.parseYaml(ActionStatus.SPECIFIED_SERVICE_RESOURCE_VERSION_NOT_FOUND.name());
////		assertEquals("Check response code after artifact download request", errorInfo.getCode(), downloadArtifactRestResponse.getErrorCode());
////		
////		List<String> variables = Arrays.asList();
////		utils.checkBodyResponseOnError(ActionStatus.SPECIFIED_SERVICE_RESOURCE_VERSION_NOT_FOUND.name(), variables, downloadArtifactRestResponse.getResponse()));
//		
//	}
//	
//
//	
//	
	
	}
	
}
