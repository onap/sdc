/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.execute.devCI;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.DistributionNotificationStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.DistributionValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.relevantcodes.extentreports.LogStatus;

import fj.data.Either;

public class CRUDExternalAPI extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(CRUDExternalAPI.class.getName());
	protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
	protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";



	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();


	protected String serviceVersion;
	protected ResourceReqDetails resourceDetails;
	protected User sdncUserDetails;
	protected ServiceReqDetails serviceDetails;
	

	@BeforeMethod
	public void init() throws Exception{
		AtomicOperationUtils.createDefaultConsumer(true);
	}
	
	
	@Rule 
	public static TestName name = new TestName();

	public CRUDExternalAPI() {
		super(name, CRUDExternalAPI.class.getName());

	}
	
	
	
	@DataProvider(name="uploadArtifactOnVfcVlCpViaExternalAPI") 
	public static Object[][] dataProviderUploadArtifactOnVfcVlCpViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			};
	}
		

	
	// External API
	// Upload artifact on VFC, VL, CP via external API - happy flow
	@Test(dataProvider="uploadArtifactOnVfcVlCpViaExternalAPI")
	public void uploadArtifactOnVfcVlCpViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s, resourceTypeEnum: %s", chosenLifeCycleState, artifactType, resourceTypeEnum));
		uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, chosenLifeCycleState, artifactType, resourceTypeEnum);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@DataProvider(name="uploadArtifactOnVFViaExternalAPI") 
	public static Object[][] dataProviderUploadArtifactOnVFViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKOUT, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKIN, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_JSON"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_DOC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"}
			};
	}
		

	
	// External API
	// Upload artifact on VF via external API - happy flow
	@Test(dataProvider="uploadArtifactOnVFViaExternalAPI")
	public void uploadArtifactOnVFViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, chosenLifeCycleState, artifactType, null);
	}
	
	
	
	
	
	@DataProvider(name="uploadArtifactOnServiceViaExternalAPI") 
	public static Object[][] dataProviderUploadArtifactOnServiceViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"}
			};
	}
	
	
	
	
	
	@Test(dataProvider="uploadArtifactOnServiceViaExternalAPI")
	public void uploadArtifactOnServiceViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, chosenLifeCycleState, artifactType, null);
	}
	
	
	@DataProvider(name="uploadArtifactOnServiceViaExternalAPIIncludingDistribution") 
	public static Object[][] dataProviderUploadArtifactOnServiceViaExternalAPIIncludingDistribution() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			};
	}
	
	@Test(dataProvider="uploadArtifactOnServiceViaExternalAPIIncludingDistribution")
	public void uploadArtifactOnServiceViaExternalAPIIncludingDistribution(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, chosenLifeCycleState, artifactType, null);
		
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		
		if(config.getIsDistributionClientRunning()){
			List<String> distributionStatusList = Arrays.asList(DistributionNotificationStatusEnum.DOWNLOAD_OK.toString(), DistributionNotificationStatusEnum.DEPLOY_OK.toString(), DistributionNotificationStatusEnum.NOTIFIED.toString());
			DistributionValidationUtils.validateDistributedArtifactsByAudit((Service)component, distributionStatusList);
		}
	}
	
	
	// Happy flow - get chosen life cycle state, artifact type and asset type
	// Create asset, upload artifact via external API + check audit & response code
	// Download artifact via external API + check audit & response code
	protected Component uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		Component component = null;
		RestResponse restResponse;
		int numberOfArtifact = 0;
		
		// get artifact data
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, false);
		
		// create component/s & upload artifact via external api
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			
			component = getComponentWithResourceInstanceInTargetLifeCycleState(chosenLifeCycleState, resourceTypeEnum);
			restResponse = uploadArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
			
 			if((LifeCycleStatesEnum.CERTIFICATIONREQUEST == chosenLifeCycleState) && (!component.getComponentType().toString().equals(ComponentTypeEnum.RESOURCE.toString()))) {
				numberOfArtifact = (component.getComponentInstances().get(0).getDeploymentArtifacts() == null ? 0 : component.getComponentInstances().get(0).getDeploymentArtifacts().size()) + 1;
			} else {
				numberOfArtifact = (component.getComponentInstances().get(0).getDeploymentArtifacts() == null ? 0 : component.getComponentInstances().get(0).getDeploymentArtifacts().size()) + 1;
			}
		} else {
			component = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, chosenLifeCycleState, resourceTypeEnum);
			
			restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
			numberOfArtifact = component.getDeploymentArtifacts().size() + 1;
		}
		
		
		
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		component = getNewerVersionOfComponent(component, chosenLifeCycleState);
		
		// Get list of deployment artifact + download them via external API
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will be increase by one.");
		
		// Download the uploaded artifact via external API
		downloadResourceDeploymentArtifactExternalAPI(component, deploymentArtifacts.get(responseArtifact.getArtifactLabel()), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentTypeEnum);
	
		return component;
	}
	
	// Upload artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse uploadArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, ComponentInstance componentInstance, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, resourceDetails.getComponentInstances().get(0));
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPLOAD_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, componentInstance.getNormalizedName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		expectedExternalAudit.setRESOURCE_NAME(componentInstance.getNormalizedName());
		expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + componentInstance.getNormalizedName() + "/artifacts");
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	
	
	protected Component getComponentWithResourceInstanceInTargetLifeCycleState(LifeCycleStatesEnum lifeCycleStatesEnum, ResourceTypeEnum resourceTypeEnum) throws Exception {
		Component component;
		if(resourceTypeEnum == ResourceTypeEnum.VF) {
			component = getComponentInTargetLifeCycleState(ComponentTypeEnum.SERVICE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
			
			Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, null);
			AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
			
			// Add artifact to service if asked for certifcationrequest - must be at least one artifact for the flow
			if((LifeCycleStatesEnum.CERTIFICATIONREQUEST == lifeCycleStatesEnum) || (LifeCycleStatesEnum.STARTCERTIFICATION == lifeCycleStatesEnum)) {
			}
			AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, component, UserRoleEnum.DESIGNER, true, true).left().value();
			component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, lifeCycleStatesEnum, true).getLeft();
		} else {
			component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
			Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, resourceTypeEnum);
			AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
			component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, lifeCycleStatesEnum, true).getLeft();
		}
		
		
		return component;
	}
	
	
	
	
	// Upload artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(resourceDetails, sdncModifierDetails, artifactReqDetails);
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPLOAD_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	
	// Check Component version (increase by one if not in checkout)
	// Input: component, componentLifeCycleState
	// for any LifeCycleState != checkout
	// find component of version +0.1
	// check that this version different for input version
	// check that this component uniqueID different from input uniqueID
	// Return: that version
	protected Component getNewerVersionOfComponent(Component component, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		Component resourceDetails = null;
		
		if((!lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKOUT)) && (!lifeCycleStatesEnum.equals(LifeCycleStatesEnum.STARTCERTIFICATION))) {
			
			
			String resourceVersion = component.getVersion();
			String resourceUniqueID = component.getUniqueId();
			
			if(component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
				resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), String.format("%.1f", Double.parseDouble(component.getVersion()) + 0.1));
			} else {
				resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), String.format("%.1f", Double.parseDouble(component.getVersion()) + 0.1));
			}
			
			String resourceNewVersion = resourceDetails.getVersion();
			String resourceNewUniqueID = resourceDetails.getUniqueId();
			
			System.out.println(resourceNewVersion);
			System.out.println("Service UUID: " + resourceDetails.getUUID());
			System.out.println("Service UniqueID: " + resourceDetails.getUniqueId());
			
			// Checking that new version exist + different from old one by unique id
			Assert.assertNotEquals(resourceVersion, resourceNewVersion, "Expected for diffrent resource version.");
			Assert.assertNotEquals(resourceUniqueID, resourceNewUniqueID, "Expected that resource will have new unique ID.");
		} else {
			if(component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
				resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
			} else {
				resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
			}
		}
		return resourceDetails;
	}
	
	
	
	
	
	// download deployment via external api + check response code for success (200) + get artifactReqDetails and verify payload + verify audit
	protected RestResponse downloadResourceDeploymentArtifactExternalAPI(Component resourceDetails, ArtifactDefinition artifactDefinition, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, ComponentTypeEnum componentTypeEnum) throws Exception {
		RestResponse restResponse;
		
		if(componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
			restResponse = ArtifactRestUtils.getComponentInstanceDeploymentArtifactExternalAPI(resourceDetails.getUUID(), resourceDetails.getComponentInstances().get(0).getNormalizedName(), artifactDefinition.getArtifactUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getComponentType().toString());
		} else {
			restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceDetails.getUUID(), artifactDefinition.getArtifactUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getComponentType().toString());
		}
		
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 200;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
		
		
		// For known artifact/payload - verify payload of downloaded artfaict
		if (artifactReqDetails != null) {
			String response = restResponse.getResponse();
			String payloadData = artifactReqDetails.getPayload();
			String decodedPaypload = org.openecomp.sdc.ci.tests.utils.Decoder.decode(payloadData);
			
			Assert.assertEquals(response, decodedPaypload, "Response artifact payload not correct.");
		}
		
		//TODO - including body - resourceDetails.getName()
//		// Verify audit
//		String auditAction = "DownloadArtifact";
//		
//		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
//		body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
//		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
//		
//		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
//		expectedResourceAuditJavaObject.setAction(auditAction);
//		expectedResourceAuditJavaObject.setResourceType(resourceDetails.getComponentType().getValue());
//		expectedResourceAuditJavaObject.setStatus("200");
//		expectedResourceAuditJavaObject.setDesc("OK");
//		expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
//		
//		if(componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
//			expectedResourceAuditJavaObject.setResourceName(resourceDetails.getComponentInstances().get(0).getName());
//			String resource_url = String.format("/sdc/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s", resourceDetails.getUUID(), resourceDetails.getComponentInstances().get(0).getNormalizedName(), artifactDefinition.getArtifactUUID());
//			expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
//				
//			AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
//		} else {
//			expectedResourceAuditJavaObject.setResourceName(resourceDetails.getName());
//			String resource_url = String.format("/sdc/v1/catalog/services/%s/artifacts/%s", resourceDetails.getUUID(), artifactDefinition.getArtifactUUID());
//			expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
//		}
//		
//		AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
		
		return restResponse;
		
	}
	
	// download deployment via external api + check response code for success (200) + verify audit
	protected void downloadResourceDeploymentArtifactExternalAPI(Component resourceDetails, ArtifactDefinition artifactDefinition, User sdncModifierDetails) throws Exception {
		downloadResourceDeploymentArtifactExternalAPI(resourceDetails, artifactDefinition, sdncModifierDetails, null, resourceDetails.getComponentType());
	}
	
	
	
	
	
	
	
	
	
	@DataProvider(name="uploadArtifactOnRIViaExternalAPI") 
	public static Object[][] dataProviderUploadArtifactOnRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT"},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL"},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP"},
			
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT"},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL"},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP"},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP"}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="uploadArtifactOnRIViaExternalAPI")
	public void uploadArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType, null);
	}
	
	
	
	@DataProvider(name="uploadArtifactOnVfcVlCpRIViaExternalAPI") 
	public static Object[][] dataProviderUploadArtifactOnVfcVlCpRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="uploadArtifactOnVfcVlCpRIViaExternalAPI")
	public void uploadArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType, resourceTypeEnum);
	}
	
	
	
	
	@DataProvider(name="uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI") 
	public static Object[][] dataProviderUploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI() {
		return new Object[][] {
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithSameLabel"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
			};
	}
	
	
	
	
	// InvalidArtifact + check audit & response code function
	@Test(dataProvider="uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI")
	public void uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState,
			ComponentTypeEnum componentTypeEnum, String uploadArtifactTestType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, componentTypeEnum: %s, uploadArtifactTestType: %s", chosenLifeCycleState, componentTypeEnum, uploadArtifactTestType));
		Component resourceDetails;
		ComponentInstance componentResourceInstanceDetails = null;
		ArtifactReqDetails artifactReqDetails;
		
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum)	 {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);
			
			resourceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.SERVICE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
			resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(chosenLifeCycleState, null);
			componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
		} else {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);
			
			resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, chosenLifeCycleState, null);
		}
		
		
		switch (uploadArtifactTestType) {
		case "uploadArtifactWithInvalidTypeToLong":
			uploadArtifactWithInvalidTypeToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidTypeEmpty":
			uploadArtifactWithInvalidTypeEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidCheckSum":
			uploadArtifactWithInvalidCheckSum(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidNameToLong":
			uploadArtifactWithInvalidNameToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidNameEmpty":
			uploadArtifactWithInvalidNameEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidLabelToLong":
			uploadArtifactWithInvalidLabelToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidLabelEmpty":
			uploadArtifactWithInvalidLabelEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidDescriptionToLong":
			uploadArtifactWithInvalidDescriptionToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithInvalidDescriptionEmpty":
			uploadArtifactWithInvalidDescriptionEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		case "uploadArtifactWithSameLabel":
		default:
			uploadArtifactWithSameLabel(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
			break;
		}	
	}
	
	// Upload artifact with invalid type via external API - to long type
	protected void uploadArtifactWithInvalidTypeToLong(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		artifactReqDetails.setArtifactType("dsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfds");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
		List<String> variables = asList(artifactReqDetails.getArtifactType());
		
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	// Upload artifact with invalid type via external API - empty type
	protected void uploadArtifactWithInvalidTypeEmpty(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		artifactReqDetails.setArtifactType("");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
		List<String> variables = asList(artifactReqDetails.getArtifactType());
		
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	// Upload artifact with invalid checksum via external API
	protected void uploadArtifactWithInvalidCheckSum(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_INVALID_MD5.name());
		List<String> variables = asList();
		uploadArtifactWithInvalidCheckSumOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
						artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables);
	}
	
	
	// Upload artifact with valid type & invalid name via external API - name to long
	protected void uploadArtifactWithInvalidNameToLong(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
		List<String> variables = asList("artifact name", "255");
		artifactReqDetails.setArtifactName("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
						artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	
	// Upload artifact with valid type & invalid name via external API - name is empty
	protected void uploadArtifactWithInvalidNameEmpty(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_ARTIFACT_NAME.name());
		List<String> variables = asList();
		
		artifactReqDetails.setArtifactName("");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	
	// Upload artifact with valid type & invalid label via external API - label to long
	protected void uploadArtifactWithInvalidLabelToLong(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
		List<String> variables = asList("artifact label", "25");
		artifactReqDetails.setArtifactLabel("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
		
		
	// Upload artifact with valid type & invalid label via external API - label is empty
	protected void uploadArtifactWithInvalidLabelEmpty(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		List<String> variables = asList("artifact label");
		artifactReqDetails.setArtifactLabel("");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	
	// Upload artifact with invalid description via external API - to long description
	protected void uploadArtifactWithInvalidDescriptionToLong(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
			
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
		List<String> variables = asList("artifact description", "256");
		artifactReqDetails.setDescription("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
			
			
	// Upload artifact with invalid description via external API - empty description
	protected void uploadArtifactWithInvalidDescriptionEmpty(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
			
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		List<String> variables = asList("artifact description");
		artifactReqDetails.setDescription("");
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	

	
	
	// Upload artifact with same label via external API
	protected void uploadArtifactWithSameLabel(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			ComponentInstance componentResourceInstanceDetails) throws Exception {
		
		RestResponse restResponse = null;
		if(componentResourceInstanceDetails != null) {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
		} else {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails);

		}
		
		ArtifactDefinition artifactDefinition = getArtifactDataFromJson(restResponse.getResponse());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_EXIST.name());
		
		List<String> variables = asList(artifactDefinition.getArtifactDisplayName());
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables, null);
	}
	
	
	
	
	
	
	
	
	protected RestResponse uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			Integer expectedResponseCode, ComponentInstance componentResourceInstanceDetails, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		RestResponse restResponse;
		
		if(componentResourceInstanceDetails != null) {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(resourceDetails, sdncModifierDetails, artifactReqDetails, componentResourceInstanceDetails);
		} else {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(resourceDetails, sdncModifierDetails, artifactReqDetails);

		}
		
		// validate response code
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
				
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPLOAD_BY_API;
				
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
//		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		
		responseArtifact.setUpdaterFullName("");
		responseArtifact.setUserIdLastUpdater(sdncModifierDetails.getUserId());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditFailure(assetTypeEnum, action, responseArtifact, resourceDetails.getUUID(), errorInfo, variables);
		expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
		expectedExternalAudit.setRESOURCE_TYPE(resourceDetails.getComponentType().getValue());
		expectedExternalAudit.setARTIFACT_DATA(null);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
		if(componentResourceInstanceDetails != null) {
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getComponentInstances().get(0).getNormalizedName());
			expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + resourceDetails.getComponentInstances().get(0).getNormalizedName() + "/artifacts");
			expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getComponentInstances().get(0).getNormalizedName());
		} else {
				expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
				body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		}
		
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API.getName(), body);
		
		return restResponse;
	
	}

	
	
	
	
	
	protected RestResponse uploadArtifactWithInvalidCheckSumOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
			Integer expectedResponseCode, ComponentInstance componentResourceInstanceDetails, ErrorInfo errorInfo, List<String> variables) throws Exception {
		RestResponse restResponse;
		
		if(componentResourceInstanceDetails != null) {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactWithInvalidCheckSumOfComponentInstanceOnAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
		} else {
			restResponse = ArtifactRestUtils.externalAPIUploadArtifactWithInvalidCheckSumOfTheAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails);

		}
		
		// validate response code
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		// Check auditing for upload operation
//		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.DEPLOYMENT_ARTIFACT_NAME_ALREADY_EXISTS.name());
//		 = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
//		List<String> variables = asList("artifact name", "255");
		
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
				
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPLOAD_BY_API;
				
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
//		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		
		responseArtifact.setUpdaterFullName("");
		responseArtifact.setUserIdLastUpdater(sdncModifierDetails.getUserId());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditFailure(assetTypeEnum, action, responseArtifact, resourceDetails.getUUID(), errorInfo, variables);
		expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
		expectedExternalAudit.setRESOURCE_TYPE(resourceDetails.getComponentType().getValue());
		expectedExternalAudit.setARTIFACT_DATA(null);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
		if(componentResourceInstanceDetails != null) {
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getComponentInstances().get(0).getNormalizedName());
			expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + resourceDetails.getComponentInstances().get(0).getNormalizedName() + "/artifacts");
			expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getComponentInstances().get(0).getNormalizedName());
		} else {
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		}
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API.getName(), body);
		
		return restResponse;
	
	}
	
	
	
	
	
	
	
	
	@DataProvider(name="uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset") 
	public static Object[][] dataProviderUploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset() {
		return new Object[][] {
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT},
			};
	}
		
	
	// External API
	// Upload artifact by diffrent user then creator of asset - Fail
	@Test(dataProvider="uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset")
	public void uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, userRoleEnum: %s, lifeCycleStatesEnum: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum));
		Component resourceDetails;
		ComponentInstance componentResourceInstanceDetails = null;
		ArtifactReqDetails artifactReqDetails;
		
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum)	 {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);
			
			resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(lifeCycleStatesEnum, null);
			componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
		} else {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);
			
			resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, lifeCycleStatesEnum, null);
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList();
		
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(userRoleEnum),
				artifactReqDetails, 409, componentResourceInstanceDetails, errorInfo, variables, lifeCycleStatesEnum);
		
		if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKIN)) {
			performeClean();
		}
	}
	
	
	
	
	
	@DataProvider(name="uploadArtifactOnAssetWhichNotExist") 
	public static Object[][] dataProviderUploadArtifactOnAssetWhichNotExist() {
		return new Object[][] {
			{ComponentTypeEnum.SERVICE},
			{ComponentTypeEnum.RESOURCE},
			{ComponentTypeEnum.RESOURCE_INSTANCE},
			};
	}
		

	// External API
	// Upload artifact on VF via external API - happy flow
	@Test(dataProvider="uploadArtifactOnAssetWhichNotExist")
	public void uploadArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s", componentTypeEnum));
		Component resourceDetails;
		ComponentInstance componentResourceInstanceDetails = null;
		ArtifactReqDetails artifactReqDetails;
		
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum)	 {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);
			
			resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(LifeCycleStatesEnum.CHECKIN, null);
			componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
			
			resourceDetails.setUUID("12345");
			componentResourceInstanceDetails.setNormalizedName("12345");
		} else {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", "OTHER", true, false);
			
			resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, null);
			
			resourceDetails.setUUID("12345");
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
		List<String> variables = asList("null");
		
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 404, componentResourceInstanceDetails, errorInfo, variables, LifeCycleStatesEnum.CHECKIN);
		
		performeClean();
		
	}
	
	
	@DataProvider(name="uploadArtifactOnAssetWhichInInvalidStateForUploading") 
	public static Object[][] dataProviderUploadArtifactOnAssetWhichInInvalidStateForUploading() {
		return new Object[][] {
			{ComponentTypeEnum.SERVICE},
			{ComponentTypeEnum.RESOURCE},
			{ComponentTypeEnum.RESOURCE_INSTANCE},
			};
	}
	
	
	@Test(dataProvider="uploadArtifactOnAssetWhichInInvalidStateForUploading")
	public void uploadArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s", componentTypeEnum));
		Component resourceDetails;
		ComponentInstance componentResourceInstanceDetails = null;
		ArtifactReqDetails artifactReqDetails;
		
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum)	 {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);
			
			resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(LifeCycleStatesEnum.STARTCERTIFICATION, null);
			componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
		} else {
			artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);

			resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, null);
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
		List<String> variables = asList(resourceDetails.getName(), resourceDetails.getComponentType().toString().toLowerCase(), resourceDetails.getLastUpdaterFullName().split(" ")[0],
				resourceDetails.getLastUpdaterFullName().split(" ")[1], resourceDetails.getLastUpdaterUserId());
		
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				artifactReqDetails, 403, componentResourceInstanceDetails, errorInfo, variables, LifeCycleStatesEnum.STARTCERTIFICATION);
		
		performeClean();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	//					Update External API											  //
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	@DataProvider(name="updateArtifactForServiceViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactForServiceViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"},
			{LifeCycleStatesEnum.CERTIFY, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFY, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFY, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFY, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFY, "OTHER"}
			};
	}
	
	
	
	
	// Update artifact for Service - Success
	@Test(dataProvider="updateArtifactForServiceViaExternalAPI")
	public void updateArtifactForServiceViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.SERVICE, lifeCycleStatesEnum, artifactType);
		
		// for certify version check that previous version exist, and that it artifact can be download + checksum
		if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
			// Download the uploaded artifact via external API
			downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.SERVICE);
		}
	}
	
	@DataProvider(name="updateArtifactForVFViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactForVFViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKOUT, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKIN, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_JSON"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_DOC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"}
			};
	}
	
	
	// Update artifact for VF - Success
	@Test(dataProvider="updateArtifactForVFViaExternalAPI")
	public void updateArtifactForVFViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum, artifactType);
		
		// for certify version check that previous version exist, and that it artifact can be download + checksum
		if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
			// Download the uploaded artifact via external API
			downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE);
		}
	}
	
	@DataProvider(name="updateArtifactForVfcVlCpViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactForVfcVlCpViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			};
	}
	
	
	// Update artifact for VFC/VL/CP - Success
	@Test(dataProvider="updateArtifactForVfcVlCpViaExternalAPI")
	public void updateArtifactForVfcVlCpViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s, resourceTypeEnum: %s", lifeCycleStatesEnum, artifactType, resourceTypeEnum));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum, artifactType);
		
		// for certify version check that previous version exist, and that it artifact can be download + checksum
		if(lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
			// Download the uploaded artifact via external API
			downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE);
		}
	}
	
	@DataProvider(name="updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI() {
		return new Object[][] {
			{ResourceTypeEnum.VFC},
			{ResourceTypeEnum.VL},
			{ResourceTypeEnum.CP}
			};
	}
	
	
	// Verify that it cannot update VFC/VL/CP artifact on VFCi/VLi/CPi - Failure flow
	@Test(dataProvider="updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI")
	public void updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI(ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("resourceTypeEnum: %s", resourceTypeEnum));
		
		Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, resourceTypeEnum);
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", "SNMP_TRAP", true, false);
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceInstanceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
		resourceInstanceDetails = AtomicOperationUtils.changeComponentState(resourceInstanceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Component component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
		component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
		Map<String, ArtifactDefinition> deploymentArtifacts;
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, ComponentTypeEnum.RESOURCE_INSTANCE);
		String artifactUUID = null;
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		List<String> variables = asList(artifactUUID);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", errorInfo, variables, UserRoleEnum.DESIGNER, 404);

	}
	
	
	
	
	
	@DataProvider(name="updateArtifactOnRIViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactOnRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", null},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", null},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VF}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="updateArtifactOnRIViaExternalAPI")
	public void updateArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType);
		
		// for certify version check that previous version exist, and that it artifact can be download + checksum
		if(chosenLifeCycleState.equals(LifeCycleStatesEnum.CERTIFY)) {
			// Download the uploaded artifact via external API
			downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE_INSTANCE);
		}
	}
	
	
	
	
	
	
	
	@DataProvider(name="updateArtifactOnVfcVlCpRIViaExternalAPI") 
	public static Object[][] dataProviderUpdateArtifactOnVfcVlCpRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="updateArtifactOnVfcVlCpRIViaExternalAPI")
	public void updateArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType);
		
		
		// for certify version check that previous version exist, and that it artifact can be download + checksum
		if(chosenLifeCycleState.equals(LifeCycleStatesEnum.CERTIFY)) {
			// Download the uploaded artifact via external API
			downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE_INSTANCE);
		}
	}
	
	
	
	
	
	
	
	
	
	
	@DataProvider(name="updateArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset") 
	public static Object[][] dataProviderUpdateArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset() {
		return new Object[][] {
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			};
	}
		


	// External API
	// Update artifact by diffrent user then creator of asset - Fail
	@Test(dataProvider="updateArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset")
	public void updateArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, userRoleEnum: %s, lifeCycleStatesEnum: %s, artifactType: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList();
		updateArtifactOnAssetViaExternalAPI(component, componentTypeEnum, lifeCycleStatesEnum, artifactType, errorInfo, variables, userRoleEnum, 409);
	}
	
	
	@DataProvider(name="updateArtifactOnAssetWhichNotExist") 
	public static Object[][] dataProviderUpdateArtifactOnAssetWhichNotExist() {
		return new Object[][] {
			{ComponentTypeEnum.SERVICE, "OTHER", null},
			{ComponentTypeEnum.RESOURCE, "OTHER", null},
			{ComponentTypeEnum.RESOURCE_INSTANCE, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VF},
			};
	}
		


	// External API
	// Upload artifact on VF via external API - happy flow
	@Test(dataProvider="updateArtifactOnAssetWhichNotExist")
	public void updateArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, resourceTypeEnum);
		
		// get updated artifact data
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		ArtifactReqDetails artifactReqDetails = getUpdatedArtifact(deploymentArtifacts, artifactType);
		String artifactUUID = deploymentArtifacts.get(artifactReqDetails.getArtifactLabel()).getArtifactUUID();
					
		// Invalid artifactUUID
		String invalidArtifactUUID = "12341234-1234-1234-1234-123412341234";
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
		List<String> variables = asList(invalidArtifactUUID);
		
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, component.getComponentInstances().get(0), artifactReqDetails, invalidArtifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, null, artifactReqDetails, invalidArtifactUUID, errorInfo, variables, null);

		}
		
		// Invalid componentUUID
//		errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
//		variables = asList("null");
		
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			component.getComponentInstances().get(0).setNormalizedName("invalidNormalizedName");
			
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER.name());
			
			variables = asList("invalidNormalizedName", ComponentTypeEnum.RESOURCE_INSTANCE.getValue().toLowerCase(), ComponentTypeEnum.SERVICE.getValue(), component.getName());
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN);
		} else {
			component.setUUID("invalidComponentUUID");
			
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
			variables = asList("null");
			
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, null, artifactReqDetails, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN);
		}
	}
	
	
	@DataProvider(name="updateArtifactOnAssetWhichInInvalidStateForUploading") 
	public static Object[][] dataProviderUpdateProviderDeleteArtifactOnAssetWhichInInvalidStateForUploading() {
		return new Object[][] {
//			{ComponentTypeEnum.SERVICE, "OTHER"},
//			{ComponentTypeEnum.RESOURCE, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, "DCAE_INVENTORY_TOSCA"},
			};
	}
	
	
	@Test(dataProvider="updateArtifactOnAssetWhichInInvalidStateForUploading")
	public void updateArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
		List<String> variables = asList(component.getName(), component.getComponentType().toString().toLowerCase(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getFirstName(),
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getLastName(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId());
		updateArtifactOnAssetViaExternalAPI(component, componentTypeEnum, LifeCycleStatesEnum.STARTCERTIFICATION, artifactType, errorInfo, variables, UserRoleEnum.DESIGNER, 403);
		
	}
	
	
	
	
	
	@DataProvider(name="updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI") 
	public static Object[][] dataProviderUpdateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI() {
		return new Object[][] {
//			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
//			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},

			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},
			
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
			};
	}
	
	
	
	
	// InvalidArtifact + check audit & response code function
	@Test(dataProvider="updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI")
	public void updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState,
			ComponentTypeEnum componentTypeEnum, String uploadArtifactTestType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, componentTypeEnum: %s, uploadArtifactTestType: %s", chosenLifeCycleState, componentTypeEnum, uploadArtifactTestType));
		Component component;
		ComponentInstance componentInstance = null;
		String artifactType;
		
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum)	 {
			artifactType = ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString();
			component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
			componentInstance = component.getComponentInstances().get(0);
		} else {
			artifactType = ArtifactTypeEnum.OTHER.toString();
			component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
		}
		
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
		
		switch (uploadArtifactTestType) {
		case "updateArtifactWithInvalidCheckSum":
			updateArtifactWithInvalidCheckSum(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidNameToLong":
			updateArtifactWithInvalidNameToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidNameEmpty":
			updateArtifactWithInvalidNameEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidLabelToLong":
			updateArtifactWithInvalidLabelToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidLabelEmpty":
			updateArtifactWithInvalidLabelEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidDescriptionToLong":
			updateArtifactWithInvalidDescriptionToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		case "updateArtifactWithInvalidDescriptionEmpty":
		default:
			updateArtifactWithInvalidDescriptionEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
			break;
		}
		
///////////////////////////////////////////////////////////////////////////////		
//		// TODO: there is defect when checking invalid type
////		// Upload artifact with invalid type via external API
////		// invalid type
////		String artifactType = artifactReqDetails.getArtifactType();
////		artifactReqDetails.setArtifactType("invalidType");
////		restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 400, componentResourceInstanceDetails);
////		// empty type
////		artifactReqDetails.setArtifactType("");
////		restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 400, componentResourceInstanceDetails);
////		artifactReqDetails.setArtifactType(artifactType);
///////////////////////////////////////////////////////////////////////////////			
	}
	
	// TODO
	// Update artifact with invalid checksum via external API
	protected void updateArtifactWithInvalidCheckSum(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_INVALID_MD5.name());
		List<String> variables = asList();
//		uploadArtifactWithInvalidCheckSumOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
//						artifactReqDetails, 400, componentResourceInstanceDetails, errorInfo, variables);
	}
	
	
	// Update artifact with valid type & invalid name via external API - name to long
	protected void updateArtifactWithInvalidNameToLong(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
		List<String> variables = asList("artifact name", "255");
		artifactReqDetails.setArtifactName("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
		
		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}
	
	
	// Update artifact with valid type & invalid name via external API - name is empty
	protected void updateArtifactWithInvalidNameEmpty(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_ARTIFACT_NAME.name());
		List<String> variables = asList();
		artifactReqDetails.setArtifactName("");
		
		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}
	
	
	// Update artifact with valid type & invalid label via external API - label to long
	protected void updateArtifactWithInvalidLabelToLong(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED.name());
		List<String> variables = asList();
		artifactReqDetails.setArtifactLabel("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");

		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}
		
		
	// Update artifact with valid type & invalid label via external API - label is empty
	protected void updateArtifactWithInvalidLabelEmpty(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		List<String> variables = asList("artifact label");
		artifactReqDetails.setArtifactLabel("");
		
		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}
	
	
	// Update artifact with invalid description via external API - to long description
	protected void updateArtifactWithInvalidDescriptionToLong(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
			
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED.name());
		List<String> variables = asList();
		artifactReqDetails.setDescription("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
		
		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}
			
			
	// Update artifact with invalid description via external API - empty description
	protected void updateArtifactWithInvalidDescriptionEmpty(Component component, User sdncModifierDetails, String artifactType,
			ComponentInstance componentInstance) throws Exception {
		
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		if(componentInstance != null) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
					
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_LOGICAL_NAME_CANNOT_BE_CHANGED.name());
		List<String> variables = asList("artifact description");
		artifactReqDetails.setDescription("");
		
		if(componentInstance != null) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					400, null, artifactReqDetails, artifactUUID, errorInfo, variables, null);

		}
	}

	// Unhappy flow - get chosen life cycle state, artifact type and asset type
	// update artifact via external API + check audit & response code
	// Download artifact via external API + check audit & response code
	// Check artifact version, uuid & checksusm
	protected Component updateArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ErrorInfo errorInfo, List<String> variables, UserRoleEnum userRoleEnum, Integer expectedResponseCode) throws Exception {
		String componentVersionBeforeUpdate = null;
				
		// get updated artifact data
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
		componentVersionBeforeUpdate = component.getVersion();
		
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		ArtifactReqDetails artifactReqDetails = getUpdatedArtifact(deploymentArtifacts, artifactType);
		
		String artifactName = artifactReqDetails.getArtifactLabel();
		String artifactUUID = deploymentArtifacts.get(artifactName).getArtifactUUID();
		String artifactVersionBeforeUpdate = deploymentArtifacts.get(artifactName).getArtifactVersion();
		int numberOfArtifact = deploymentArtifacts.size();		
				
		// create component/s & upload artifact via external api
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
					expectedResponseCode, component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, chosenLifeCycleState);
		} else {
			updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
					expectedResponseCode, null, artifactReqDetails, artifactUUID, errorInfo, variables, chosenLifeCycleState);
		}
		
		if(component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
			component = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
		} else {
			component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
		}
			
		// Get list of deployment artifact + download them via external API
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
		Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will be same as before.");
		Assert.assertEquals(String.valueOf((Integer.parseInt(artifactVersionBeforeUpdate))), deploymentArtifacts.get(artifactName).getArtifactVersion(), "Expected that aftifact will not change.");
		Assert.assertEquals(artifactUUID, deploymentArtifacts.get(artifactName).getArtifactUUID(), "Expected that aftifactUUID will not change.");
		Assert.assertEquals(componentVersionBeforeUpdate, component.getVersion(), "Expected that check-out component will not change version number.");

		return component;
	}
	
	protected RestResponse updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails,
			Integer expectedResponseCode, ComponentInstance componentInstance, ArtifactReqDetails artifactReqDetails, String artifactUUID, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		RestResponse restResponse;
		
		if(componentInstance != null) {
			restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfComponentInstanceOnAsset(resourceDetails, sdncModifierDetails, artifactReqDetails, componentInstance, artifactUUID);
		} else {
			restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfTheAsset(resourceDetails, sdncModifierDetails, artifactReqDetails, artifactUUID);

		}
		
		// validate response code
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		//TODO
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
				
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPDATE_BY_API;
				
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
//		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		
		responseArtifact.setUpdaterFullName("");
		responseArtifact.setUserIdLastUpdater(sdncModifierDetails.getUserId());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditFailure(assetTypeEnum, action, responseArtifact, resourceDetails.getUUID(), errorInfo, variables);
		expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
		expectedExternalAudit.setRESOURCE_TYPE(resourceDetails.getComponentType().getValue());
		expectedExternalAudit.setARTIFACT_DATA("");
		expectedExternalAudit.setCURR_ARTIFACT_UUID(artifactUUID);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
		if(componentInstance != null) {
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getComponentInstances().get(0).getNormalizedName());
			expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + resourceDetails.getComponentInstances().get(0).getNormalizedName() + "/artifacts/" + artifactUUID);
			expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getComponentInstances().get(0).getNormalizedName());
		} else {
			expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL() + "/" + artifactUUID);
			if((lifeCycleStatesEnum == LifeCycleStatesEnum.CHECKIN) || (lifeCycleStatesEnum == LifeCycleStatesEnum.STARTCERTIFICATION)) {
				expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
				body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
			} else {
				body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
			}
		}
			
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPDATE_BY_API.getName(), body);
		
		return restResponse;
	    
	}
	
	
	// This function get component, user & if updatedPayload or not
	// It will create default payload / updated payload of artifact
	// And download artifact of component which starts with ci
	protected RestResponse downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(Component component, String artifactType, User sdncModifierDetails, ComponentTypeEnum componentTypeEnum) throws IOException, Exception {
		// Download the uploaded artifact via external API
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("abcd", artifactType, true, false);
		String artifactName = null;
		for (String key : component.getDeploymentArtifacts().keySet()) {
			if (key.startsWith("ci")) {
				artifactName = key;
				break;
			}
		}
		return downloadResourceDeploymentArtifactExternalAPI(component, component.getDeploymentArtifacts().get(artifactName), sdncModifierDetails, artifactReqDetails, componentTypeEnum);
	}
	
	// Get deployment artifact of asset
	protected Map<String, ArtifactDefinition> getDeploymentArtifactsOfAsset(Component component, ComponentTypeEnum componentTypeEnum) {
		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			for(ComponentInstance componentInstance: component.getComponentInstances()) {
				if(componentInstance.getNormalizedName().startsWith("ci")) {
					deploymentArtifacts = componentInstance.getDeploymentArtifacts();
					break;
				}
			}
		} else {
			deploymentArtifacts = component.getDeploymentArtifacts();
		}
		return deploymentArtifacts;
	}
	
	// get deploymentArtifact of asset and artifactType -> generate new artifact that can be updated on the asset
	protected ArtifactReqDetails getUpdatedArtifact(Map<String, ArtifactDefinition> deploymentArtifacts, String artifactType) throws IOException, Exception {
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactReqDetails.setArtifactDisplayName(deploymentArtifacts.get(key).getArtifactDisplayName());
				artifactReqDetails.setArtifactName(deploymentArtifacts.get(key).getArtifactName());
				artifactReqDetails.setArtifactLabel(deploymentArtifacts.get(key).getArtifactLabel());
				break;
			}
		}
		
		return artifactReqDetails;
	}
	
	// Happy flow - get chosen life cycle state, artifact type and asset type
	// update artifact via external API + check audit & response code
	// Download artifact via external API + check audit & response code
	// Check artifact version, uuid & checksusm
	protected Component updateArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
		RestResponse restResponse = null;
		int numberOfArtifact = 0;
		String artifactVersionBeforeUpdate = null;
		String artifactName = null;
		String componentVersionBeforeUpdate = null;
			
		// get updated artifact data
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts;
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactName = key;
				artifactVersionBeforeUpdate = deploymentArtifacts.get(key).getArtifactVersion();
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				artifactReqDetails.setArtifactDisplayName(deploymentArtifacts.get(key).getArtifactDisplayName());
				artifactReqDetails.setArtifactName(deploymentArtifacts.get(key).getArtifactName());
				artifactReqDetails.setArtifactLabel(deploymentArtifacts.get(key).getArtifactLabel());
				break;
			}
		}
		
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
		componentVersionBeforeUpdate = component.getVersion();
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		numberOfArtifact = deploymentArtifacts.size();
		
		
		// create component/s & upload artifact via external api
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			if((chosenLifeCycleState == LifeCycleStatesEnum.CERTIFICATIONREQUEST) && (!component.getComponentType().toString().equals(ComponentTypeEnum.RESOURCE.toString()))) {
				numberOfArtifact = numberOfArtifact - 1;
			}
			restResponse = updateArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID, 200);
		} else {
			
			restResponse = updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID, 200);
		}
			
			
			
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		component = getNewerVersionOfComponent(component, chosenLifeCycleState);
			
		// Get list of deployment artifact + download them via external API
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will be same as before.");
		Assert.assertEquals(String.valueOf((Integer.parseInt(artifactVersionBeforeUpdate) + 1)), deploymentArtifacts.get(artifactName).getArtifactVersion(), "Expected that aftifact version will increase by one.");
		
		if(chosenLifeCycleState == LifeCycleStatesEnum.CHECKOUT) {
			Assert.assertEquals(componentVersionBeforeUpdate, component.getVersion(), "Expected that check-out component will not change version number.");
		} else {
			Assert.assertEquals(String.format("%.1f", (Double.parseDouble(componentVersionBeforeUpdate) + 0.1)), component.getVersion(), "Expected that non check-out component version will increase by 0.1.");
		}
		
		// Download the uploaded artifact via external API
		downloadResourceDeploymentArtifactExternalAPI(component, deploymentArtifacts.get(responseArtifact.getArtifactLabel()), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentTypeEnum);
		
		return component;
	}
	
	
	// Update artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse updateArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, ComponentInstance componentInstance, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfComponentInstanceOnAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, resourceDetails.getComponentInstances().get(0), artifactUUID);
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPDATE_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, componentInstance.getNormalizedName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
//		expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL()+ "/" + artifactUUID);
		expectedExternalAudit.setRESOURCE_NAME(componentInstance.getNormalizedName());
		expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + componentInstance.getNormalizedName() + "/artifacts/" + artifactUUID);
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPDATE_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	// Update artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfTheAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID);
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_UPDATE_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL()+ "/" + artifactUUID);
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_UPDATE_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	//					Delete External API											  //
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	@DataProvider(name="deleteArtifactForServiceViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactForServiceViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"},
			{LifeCycleStatesEnum.CERTIFY, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFY, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFY, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFY, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFY, "OTHER"}
			};
	}
	
	
	
	
	// Delete artifact for Service - Success
	@Test(dataProvider="deleteArtifactForServiceViaExternalAPI")
	public void deleteArtifactForServiceViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
		deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.SERVICE, lifeCycleStatesEnum);
	}
	
	@DataProvider(name="deleteArtifactForVFViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactForVFViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKOUT, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_JSON"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CHECKIN, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_DOC"},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML"},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CHECKIN, "OTHER"},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_JSON"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_POLICY"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_EVENT"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "APPC_CONFIG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_DOC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_TOSCA"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC"},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER"},
			};
	}
	
	
	// Delete artifact for VF - Success
	@Test(dataProvider="deleteArtifactForVFViaExternalAPI")
	public void deleteArtifactForVFViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
		deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum);
	}
	
	@DataProvider(name="deleteArtifactForVfcVlCpViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactForVfcVlCpViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKOUT, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CHECKIN, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "YANG_XML", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VNF_CATALOG", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VF_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "VENDOR_LICENSE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_INVENTORY_PROFILE", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "MODEL_QUERY_SPEC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "OTHER", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			};
	}
	
	
	// Delete artifact for VFC, VL, CP - Success
	@Test(dataProvider="deleteArtifactForVfcVlCpViaExternalAPI")
	public void deleteArtifactForVfcVlCpViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s, resourceTypeEnum: %s", lifeCycleStatesEnum, artifactType, resourceTypeEnum));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum);
	}
	
	@DataProvider(name="deleteArtifactOnRIViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactOnRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", null},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", null},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VF},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VF}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="deleteArtifactOnRIViaExternalAPI")
	public void deleteArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState);
	}
	
	
	@DataProvider(name="deleteArtifactOnVfcVlCpRIViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactOnVfcVlCpRIViaExternalAPI() {
		return new Object[][] {
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VFC, null},
			
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.VL, null},
			
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_POLL", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKOUT, "SNMP_TRAP", ResourceTypeEnum.CP, null},
			
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VFC, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VFC, null},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.VL, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.VL, null},
			
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_POLL", ResourceTypeEnum.CP, null},
			{LifeCycleStatesEnum.CHECKIN, "SNMP_TRAP", ResourceTypeEnum.CP, null},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VFC},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VFC},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.VL},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.VL},
			
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_JSON", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_POLICY", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_DOC", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_BLUEPRINT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "DCAE_INVENTORY_EVENT", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_POLL", ResourceTypeEnum.CP},
			{LifeCycleStatesEnum.CERTIFICATIONREQUEST, "SNMP_TRAP", ResourceTypeEnum.CP}
			
			};
	}
	
	
	
	
	
	@Test(dataProvider="deleteArtifactOnVfcVlCpRIViaExternalAPI")
	public void deleteArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
		deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState);
	}
	
	
	@DataProvider(name="deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset") 
	public static Object[][] dataProviderDeleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset() {
		return new Object[][] {
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.DESIGNER2, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
//			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_STRATEGIST1, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKIN, "DCAE_INVENTORY_TOSCA"},
			{ComponentTypeEnum.RESOURCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.SERVICE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.PRODUCT_MANAGER1, LifeCycleStatesEnum.CHECKOUT, "DCAE_INVENTORY_TOSCA"},
			};
	}
		

	// External API
	// Delete artifact by diffrent user then creator of asset - Fail
	@Test(dataProvider="deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset")
	public void deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, userRoleEnum %s, lifeCycleStatesEnum %s, artifactType: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, lifeCycleStatesEnum, artifactType, null);
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		
		String artifactUUID = null;
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		List<String> variables = asList();
		
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
					409, component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, lifeCycleStatesEnum);
		} else {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
					409, null, artifactUUID, errorInfo, variables, lifeCycleStatesEnum);
		}
			
		//TODO
//		downloadResourceDeploymentArtifactExternalAPI(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, componentTypeEnum);
	}
	
	
	@DataProvider(name="deleteArtifactOnAssetWhichNotExist") 
	public static Object[][] dataProviderDeleteArtifactOnAssetWhichNotExist() {
		return new Object[][] {
			{ComponentTypeEnum.SERVICE, "OTHER", null},
			{ComponentTypeEnum.RESOURCE, "OTHER", null},
			{ComponentTypeEnum.RESOURCE_INSTANCE, "DCAE_INVENTORY_TOSCA", ResourceTypeEnum.VF},
			};
	}
		

	// External API
	// Upload artifact on VF via external API - happy flow
	@Test(dataProvider="deleteArtifactOnAssetWhichNotExist")
	public void deleteArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, resourceTypeEnum);
		
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		
		String artifactUUID = null;
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		// Invalid artifactUUID
		String invalidArtifactUUID = "12341234-1234-1234-1234-123412341234";
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
		List<String> variables = asList(invalidArtifactUUID);
		
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, component.getComponentInstances().get(0), invalidArtifactUUID, errorInfo, variables, null);
		} else {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, null, invalidArtifactUUID, errorInfo, variables, null);

		}
		
		
		// Invalid componentUUID	
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			component.getComponentInstances().get(0).setNormalizedName("invalidNormalizedName");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER.name());
			variables = asList("invalidNormalizedName", ComponentTypeEnum.RESOURCE_INSTANCE.getValue().toLowerCase(), ComponentTypeEnum.SERVICE.getValue(), component.getName());
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN);
		} else {
			component.setUUID("invalidComponentUUID");
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
			variables = asList("null");
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					404, null, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN);
		}
		
		

		
	}
	
	@DataProvider(name="deleteArtifactOnAssetWhichInInvalidStateForUploading") 
	public static Object[][] dataProviderDeleteArtifactOnAssetWhichInInvalidStateForUploading() {
		return new Object[][] {
			{ComponentTypeEnum.SERVICE, "OTHER"},
			{ComponentTypeEnum.RESOURCE, "OTHER"},
			{ComponentTypeEnum.RESOURCE_INSTANCE, "DCAE_INVENTORY_TOSCA"},
			};
	}
	
	
	@Test(dataProvider="deleteArtifactOnAssetWhichInInvalidStateForUploading")
	public void deleteArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum, String artifactType) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
		Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
		
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		String artifactUUID = null;
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		
		
		// Invalid artifactUUID
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
		List<String> variables = asList(component.getName(), component.getComponentType().toString().toLowerCase(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getFirstName(),
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getLastName(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId());
		
		if(componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					403, component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, null);
		} else {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
					403, null, artifactUUID, errorInfo, variables, null);

		}
		
	}
	
	
	@DataProvider(name="deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI") 
	public static Object[][] dataProviderDeleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI() {
		return new Object[][] {
			{ResourceTypeEnum.VFC},
			{ResourceTypeEnum.VL},
			{ResourceTypeEnum.CP}
			};
	}
	
	
	// Verify that it cannot delete VFC/VL/CP artifact on VFCi/VLi/CPi - Failure flow
	@Test(dataProvider="deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI")
	public void deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI(ResourceTypeEnum resourceTypeEnum) throws Exception {
		extendTest.log(LogStatus.INFO, String.format("resourceTypeEnum: %s", resourceTypeEnum));
		
		Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, resourceTypeEnum);
		ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", "SNMP_TRAP", true, false);
		uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceInstanceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
		resourceInstanceDetails = AtomicOperationUtils.changeComponentState(resourceInstanceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Component component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
		component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
		
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
		Map<String, ArtifactDefinition> deploymentArtifacts;
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, ComponentTypeEnum.RESOURCE_INSTANCE);
		String artifactUUID = null;
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci") && !key.endsWith("env")) {
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
		List<String> variables = asList(artifactUUID);
		deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				404, component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, null);
	}
	
	protected RestResponse deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails,
			Integer expectedResponseCode, ComponentInstance componentInstance, String artifactUUID, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
		RestResponse restResponse;
		
		if(componentInstance != null) {
			restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfComponentInstanceOnAsset(resourceDetails, sdncModifierDetails, componentInstance, artifactUUID);
		} else {
			restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfTheAsset(resourceDetails, sdncModifierDetails, artifactUUID);

		}
		
		// validate response code
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
				
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_DELETE_BY_API;
				
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
//		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		
		responseArtifact.setUpdaterFullName("");
		responseArtifact.setUserIdLastUpdater(sdncModifierDetails.getUserId());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditFailure(assetTypeEnum, action, responseArtifact, resourceDetails.getUUID(), errorInfo, variables);
		expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
		expectedExternalAudit.setRESOURCE_TYPE(resourceDetails.getComponentType().getValue());
		expectedExternalAudit.setARTIFACT_DATA(null);
		expectedExternalAudit.setCURR_ARTIFACT_UUID(artifactUUID);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
		if(componentInstance != null) {
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getComponentInstances().get(0).getNormalizedName());
			expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + resourceDetails.getComponentInstances().get(0).getNormalizedName() + "/artifacts/" + artifactUUID);
			expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getComponentInstances().get(0).getNormalizedName());
		} else {
			expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL() + "/" + artifactUUID);
			if((errorInfo.getMessageId().equals(ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name()).getMessageId())) || 
					errorInfo.getMessageId().equals(ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name()).getMessageId()) ||
					(lifeCycleStatesEnum == LifeCycleStatesEnum.STARTCERTIFICATION)) {
				expectedExternalAudit.setRESOURCE_NAME(resourceDetails.getName());
				body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
			} else {
				body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
			}
		}
				
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName(), body);
		
		return restResponse;
	
	}
	
	
	// Happy flow - get chosen life cycle state, artifact type and asset type
	// delete artifact via external API + check audit & response code
	protected Component deleteArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState) throws Exception {
		String artifactName = null;
		component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
			
		// get updated artifact data
		String artifactUUID = null;
		Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		
		for (String key : deploymentArtifacts.keySet()) {
			if (key.startsWith("ci")) {
				artifactName = key;
				artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
				break;
			}
		}
			
		
		String componentVersionBeforeDelete = component.getVersion();
		int numberOfArtifact = deploymentArtifacts.size();
			
				
		// create component/s & upload artifact via external api
		if(ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
			deleteArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, 200);
		} else {
			deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, 200);
		}	
				
		component = getNewerVersionOfComponent(component, chosenLifeCycleState);
			
		// Get list of deployment artifact + download them via external API
		deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
		if(deploymentArtifacts.get(artifactName) != null) {
			Assert.assertTrue(false, "Expected that deletecd artifact will not appear in deployment artifact list.");
		}
		if((LifeCycleStatesEnum.CERTIFICATIONREQUEST.equals(chosenLifeCycleState)) && (ComponentTypeEnum.RESOURCE_INSTANCE.equals(componentTypeEnum)) && (!component.getComponentType().toString().equals(ComponentTypeEnum.RESOURCE.toString()))) {
			Assert.assertEquals(numberOfArtifact - 2, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact (one deleted and one vfmodule) will decrease by two.");
		} else {
			Assert.assertEquals(numberOfArtifact - 1, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will decrease by one.");
		}
		

		if(chosenLifeCycleState == LifeCycleStatesEnum.CHECKOUT) {
			Assert.assertEquals(componentVersionBeforeDelete, component.getVersion(), "Expected that check-out component will not change version number.");
		} else {
			Assert.assertEquals(String.format("%.1f", (Double.parseDouble(componentVersionBeforeDelete) + 0.1)), component.getVersion(), "Expected that non check-out component version will increase by 0.1.");
		}
		
		downloadResourceDeploymentArtifactExternalAPI(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, componentTypeEnum);
		
		return component;
	}
	
	// Delete artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse deleteArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, ComponentInstance componentInstance, User sdncModifierDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfComponentInstanceOnAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getComponentInstances().get(0), artifactUUID);
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_DELETE_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, componentInstance.getNormalizedName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
//		expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL()+ "/" + artifactUUID);
		expectedExternalAudit.setRESOURCE_NAME(componentInstance.getNormalizedName());
		expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetTypeEnum.getValue() + "/" + resourceDetails.getUUID() + "/resourceInstances/" + componentInstance.getNormalizedName() + "/artifacts/" + artifactUUID);
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	// Delete artifact via external API + Check auditing for upload operation + Check response of external API
	protected RestResponse deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component resourceDetails, User sdncModifierDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
		RestResponse restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfTheAsset(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID);
		
		// Check response of external API
		Integer responseCode = restResponse.getErrorCode();
		Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
		
		
		// Check auditing for upload operation
		ArtifactDefinition responseArtifact = getArtifactDataFromJson(restResponse.getResponse());
		
		AuditingActionEnum action = AuditingActionEnum.ARTIFACT_DELETE_BY_API;
		
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
		
		AssetTypeEnum assetTypeEnum = AssetTypeEnum.valueOf((resourceDetails.getComponentType().getValue() + "s").toUpperCase());
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalArtifactAuditSuccess(assetTypeEnum, action, responseArtifact, resourceDetails);
		expectedExternalAudit.setRESOURCE_URL(expectedExternalAudit.getRESOURCE_URL()+ "/" + artifactUUID);
		AuditValidationUtils.validateExternalAudit(expectedExternalAudit, AuditingActionEnum.ARTIFACT_DELETE_BY_API.getName(), body);
		
		return restResponse;
	}
	
	
	
	// download deployment via external api + check response code for success (200) + get artifactReqDetails and verify payload + verify audit
	protected RestResponse downloadResourceDeploymentArtifactExternalAPI(Component resourceDetails, User sdncModifierDetails, String artifactUUID, ComponentTypeEnum componentTypeEnum) throws Exception {
		RestResponse restResponse;
		
		if(componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
			restResponse = ArtifactRestUtils.getComponentInstanceDeploymentArtifactExternalAPI(resourceDetails.getUUID(), resourceDetails.getComponentInstances().get(0).getNormalizedName(), artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getComponentType().toString());
		} else {
			restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceDetails.getUUID(), artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getComponentType().toString());
		}
		
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 404;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
		
		
		//TODO - including body - resourceDetails.getName()
//			// Verify audit
//			String auditAction = "DownloadArtifact";
//			
//			Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
//			body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, responseCode.toString());
//			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceDetails.getName());
//			
//			ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
//			expectedResourceAuditJavaObject.setAction(auditAction);
//			expectedResourceAuditJavaObject.setResourceType(resourceDetails.getComponentType().getValue());
//			expectedResourceAuditJavaObject.setStatus("200");
//			expectedResourceAuditJavaObject.setDesc("OK");
//			expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
//			
//			if(componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
//				expectedResourceAuditJavaObject.setResourceName(resourceDetails.getComponentInstances().get(0).getName());
//				String resource_url = String.format("/sdc/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s", resourceDetails.getUUID(), resourceDetails.getComponentInstances().get(0).getNormalizedName(), artifactDefinition.getArtifactUUID());
//				expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
//					
//				AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
//			} else {
//				expectedResourceAuditJavaObject.setResourceName(resourceDetails.getName());
//				String resource_url = String.format("/sdc/v1/catalog/services/%s/artifacts/%s", resourceDetails.getUUID(), artifactDefinition.getArtifactUUID());
//				expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
//			}
//			
//			AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
			
		return restResponse;
			
	}
	
	
	
	
	public Component getComponentInTargetLifeCycleState(String componentType, UserRoleEnum creatorUser, LifeCycleStatesEnum targetLifeCycleState, ResourceTypeEnum resourceTypeEnum) throws Exception {
		Component resourceDetails = null;
		
		if((componentType.toLowerCase().equals("vf")) || (componentType.toLowerCase().equals("resource"))){
			if(resourceTypeEnum==null) {
				resourceTypeEnum = ResourceTypeEnum.VF;
			}
			Either<Resource, RestResponse> createdResource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(resourceTypeEnum, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, creatorUser, true);
			resourceDetails = createdResource.left().value();
			resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, creatorUser, targetLifeCycleState, true).getLeft();
		} else {
			Either<Service, RestResponse> createdResource = AtomicOperationUtils.createDefaultService(creatorUser, true);
			resourceDetails = createdResource.left().value();
			// Add artifact to service if asked for certifcationrequest - must be at least one artifact for the flow
			if((LifeCycleStatesEnum.CERTIFICATIONREQUEST == targetLifeCycleState) || (LifeCycleStatesEnum.STARTCERTIFICATION == targetLifeCycleState)) {
						AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetails, UserRoleEnum.DESIGNER, true, true).left().value();
			}
			resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, creatorUser, targetLifeCycleState, true).getLeft();
		}
		
		return resourceDetails;
	}
	
	
	
	
	
	protected String createUploadArtifactBodyJson() {
		Map<String, Object> jsonBody = new HashMap<String, Object>();
		jsonBody.put("artifactName", UPLOAD_ARTIFACT_NAME);
		jsonBody.put("artifactDisplayName", "configure");
		jsonBody.put("artifactType", "SHELL");
		jsonBody.put("mandatory", "false");
		jsonBody.put("description", "ff");
		jsonBody.put("payloadData", UPLOAD_ARTIFACT_PAYLOAD);
		jsonBody.put("artifactLabel", "configure");
		return gson.toJson(jsonBody);
	}
	
	protected ArtifactDefinition getArtifactDataFromJson(String content) {

		JsonObject jsonElement = new JsonObject();
		ArtifactDefinition resourceInfo = null;
		
		try {
			Gson gson = new Gson();
			jsonElement = gson.fromJson(content, jsonElement.getClass());
			JsonElement artifactGroupValue = jsonElement.get(Constants.ARTIFACT_GROUP_TYPE_FIELD);
			if (artifactGroupValue != null && !artifactGroupValue.isJsonNull()) {
				String groupValueUpper = artifactGroupValue.getAsString().toUpperCase();
				if (!ArtifactGroupTypeEnum.getAllTypes().contains(groupValueUpper)) {
					StringBuilder sb = new StringBuilder();
					for (String value : ArtifactGroupTypeEnum.getAllTypes()) {
						sb.append(value).append(", ");
					}
					log.debug("artifactGroupType is {}. valid values are: {}", groupValueUpper, sb.toString());
					return null;
				} else {
					jsonElement.remove(Constants.ARTIFACT_GROUP_TYPE_FIELD);
					jsonElement.addProperty(Constants.ARTIFACT_GROUP_TYPE_FIELD, groupValueUpper);
				}
			}
			String payload = null;
			JsonElement artifactPayload = jsonElement.get(Constants.ARTIFACT_PAYLOAD_DATA);
			if (artifactPayload != null && !artifactPayload.isJsonNull()) {
				payload = artifactPayload.getAsString();
			}
			jsonElement.remove(Constants.ARTIFACT_PAYLOAD_DATA);
			String json = gson.toJson(jsonElement);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
			mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
			
			resourceInfo = mapper.readValue(json, ArtifactDefinition.class);
			resourceInfo.setPayloadData(payload);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeArtifactInformationInvalidError("Artifact Upload / Update");
			log.debug("Failed to convert the content {} to object.", content.substring(0, Math.min(50, content.length())), e);
		}

		return resourceInfo;
	}

	
	protected HttpGet createGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httpGet.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httpGet.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		return httpGet;
	}
	
	protected String getArtifactUid(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}
	
	protected String getArtifactEsId(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String esId = (String) responseMap.get("EsId");
		return esId;
	}
	
	protected ArtifactDefinition addArtifactDataFromResponse(HttpResponse response, ArtifactDefinition artifact) throws HttpResponseException, IOException, ParseException {
		//String responseString = new BasicResponseHandler().handleResponse(response);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity);				
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		artifact.setEsId((String)responseMap.get("esId"));
		artifact.setUniqueId((String) responseMap.get("uniqueId"));
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.findType((String) responseMap.get("artifactGroupType")));
		artifact.setTimeout(((Long) responseMap.get("timeout")).intValue());
		return artifact;
	}
	
	protected String getLifecycleArtifactUid(CloseableHttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		responseMap = (JSONObject) responseMap.get("implementation");
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}
	
	protected HttpDelete createDeleteArtifactRequest(String url) {
		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		httpDelete.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		return httpDelete;
	}
	
	protected HttpPost createPostAddArtifactRequeast(String jsonBody, String url, boolean addMd5Header) throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httppost.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httppost.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		if (addMd5Header) {
			httppost.addHeader(HttpHeaderEnum.Content_MD5.getValue(), GeneralUtility.calculateMD5Base64EncodedByString(jsonBody));
		}
		StringEntity input = new StringEntity(jsonBody);
		input.setContentType("application/json");
		httppost.setEntity(input);
		log.debug("Executing request {}" , httppost.getRequestLine());
		return httppost;
	}
	
	protected String createLoadArtifactBody() {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("artifactName", "install_apache2.sh");
		json.put("artifactType", "SHELL");
		json.put("description", "ddd");
		json.put("payloadData", "UEsDBAoAAAAIAAeLb0bDQz");
		json.put("artifactLabel", "name123");
		
		String jsonStr = gson.toJson(json);
		return jsonStr;
	}
	
	protected void checkDeleteResponse(RestResponse response) {
		BaseRestUtils.checkStatusCode(response, "delete request failed", false, 204, 404);
	}
	
	protected ArtifactUiDownloadData getArtifactUiDownloadData(String artifactUiDownloadDataStr) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			ArtifactUiDownloadData artifactUiDownloadData = mapper.readValue(artifactUiDownloadDataStr, ArtifactUiDownloadData.class);
			return artifactUiDownloadData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
}
