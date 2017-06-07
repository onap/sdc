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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64.Decoder;

import javax.json.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.cassandra.cli.CliParser.operator_return;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.ActionEnum;
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
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.DistributionNotificationStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.SearchCriteriaEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.DistributionValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ApplicationErrorCodesEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.UploadArtifactInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.openecomp.sdc.utils.Pair;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.relevantcodes.extentreports.LogStatus;

import fj.P;
import fj.data.Either;
import it.unimi.dsi.fastutil.bytes.ByteSortedSets.SynchronizedSortedSet;

import static java.util.Arrays.asList;

public class SearchFilterCategoryExternalAPI extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(CRUDExternalAPI.class.getName());

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";

	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();

	@BeforeMethod
	public void init() throws Exception{
		AtomicOperationUtils.createDefaultConsumer(true);
	}
	
	;
	@Rule 
	public static TestName name = new TestName();

	public SearchFilterCategoryExternalAPI() {
		super(name, SearchFilterCategoryExternalAPI.class.getName());

	}
	
	// Search for invalid resourceType
	@Test
	public void searchWithInvalidFilter() throws Exception {
		RestResponse restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "invalid", ResourceTypeEnum.VFC.toString());
		
		Integer expectedResponseCode = 400;
		Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
		
		List<String> variables = Arrays.asList("resourceTypeinvalid", "[resourceType, subCategory, category]");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FILTER_KEY.name(), variables, restResponse.getResponse());

		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_FILTERED_ASSET_LIST, "?" + SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "invalid=" + ResourceTypeEnum.VFC.toString());
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_FILTER_KEY.name());
		expectedExternalAudit.setDESC(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		expectedExternalAudit.setSTATUS("400");
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedExternalAudit.getRESOURCE_URL());
		AuditValidationUtils.validateAuditExternalSearchAPI(expectedExternalAudit, AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), body);
	}
	
	@DataProvider(name="searchForResourceTypeNegativeTest") 
	public static Object[][] dataProviderSearchForResourceTypeNegativeTest() {
		return new Object[][] {
			{"invalidResourceType"},
			{""}
			};
	}
	
	// Search for invalid resourceType
	@Test(dataProvider="searchForResourceTypeNegativeTest")
	public void searchForResourceTypeNegativeTest(String resourceType) throws Exception {
		RestResponse restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), resourceType);
		
		Integer expectedResponseCode = 400;
		Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
		
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables, restResponse.getResponse());

		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_FILTERED_ASSET_LIST, "?" + SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "=" + resourceType);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());
		expectedExternalAudit.setDESC(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		expectedExternalAudit.setSTATUS("400");
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedExternalAudit.getRESOURCE_URL());
		AuditValidationUtils.validateAuditExternalSearchAPI(expectedExternalAudit, AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), body);
	}
	
	// Searching for resource filter incorrect resource type using external API
	@Test
	public void searchingForResouceFilterIncorrectResouceTypeUsingExternalAPI() throws Exception {
		Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();
		List<String> createdResoucesName = new ArrayList<String>();
		createdResoucesName.add(resource.getName());
		
		for(ResourceTypeEnum resourceTypeEnum: ResourceTypeEnum.values()) {
			// Create resource for each type so it will not return 404
			AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(resourceTypeEnum, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();

			RestResponse restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), resourceTypeEnum.toString());
			
			Integer expectedResponseCode = 200;
			Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
			if(resourceTypeEnum == ResourceTypeEnum.VF) {
				validateJsonContainResource(restResponse.getResponse(), createdResoucesName, true);
			} else {
				validateJsonContainResource(restResponse.getResponse(), createdResoucesName, false);
			}
			
			
			ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_FILTERED_ASSET_LIST, "?" + SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "=" + resourceTypeEnum.toString());
			Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedExternalAudit.getRESOURCE_URL());
			AuditValidationUtils.validateAuditExternalSearchAPI(expectedExternalAudit, AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), body);
		}
	}
	
	// Searching for several resource types using external API
	@Test
	public void searchingForSeveralResouceTypesUsingExternalAPI() throws Exception {
		for(ResourceTypeEnum resourceTypeEnum: ResourceTypeEnum.values()) {
			List<String> createdResoucesName = new ArrayList<String>();
			Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(resourceTypeEnum, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();
			createdResoucesName.add(resource.getName());
			
			RestResponse restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), resourceTypeEnum.toString());
			
			Integer expectedResponseCode = 200;
			Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
			validateJsonContainResource(restResponse.getResponse(), createdResoucesName, true);
			
			ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_FILTERED_ASSET_LIST, "?" + SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "=" + resourceTypeEnum.toString());
			Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
			body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedExternalAudit.getRESOURCE_URL());
			AuditValidationUtils.validateAuditExternalSearchAPI(expectedExternalAudit, AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), body);
		}
	}
	
	// Searching for several resources of type VFCMT using external API
	@Test
	public void searchingForSeveralResourcesOfTypeVFCMTUsingExternalAPI() throws Exception {
		Random random = new Random();
 		int numberOfResouceToCreate = random.nextInt(5) + 1;	
		List<String> createdResoucesName = new ArrayList<String>();
		
		for(int i=0; i<numberOfResouceToCreate; i++) {
			
			Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFCMT, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();
			createdResoucesName.add(resource.getName());
		}
		
		RestResponse restResponse = ResourceRestUtils.getResourceListFilterByCriteria(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), SearchCriteriaEnum.RESOURCE_TYPE.getValue(), ResourceTypeEnum.VFCMT.toString());
		
		Integer expectedResponseCode = 200;
		Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
		validateJsonContainResource(restResponse.getResponse(), createdResoucesName, true);
		
		ExpectedExternalAudit expectedExternalAudit = ElementFactory.getDefaultExternalAuditObject(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_FILTERED_ASSET_LIST, "?" + SearchCriteriaEnum.RESOURCE_TYPE.getValue() + "=" + ResourceTypeEnum.VFCMT.toString());
		AuditValidationUtils.validateAuditExternalSearchAPI(expectedExternalAudit, AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), null);
	}
	
	
	
	@DataProvider(name="normativeResourceCategory") 
	public static Object[][] dataProviderNormativeResourceCategory() {
		return new Object[][] {
			{ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE},
			{ResourceCategoryEnum.TEMPLATE_MONITORING_TEMPLATE},
			};
	}
	
	// Verify exist of normative resource category from data provider
	@Test(dataProvider="normativeResourceCategory")
	public void normativeResourceCategory(ResourceCategoryEnum resourceCategoryEnum) throws Exception {
		RestResponse restResponse = CategoryRestUtils.getAllCategories(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "resources");
		validateJsonContainResourceCategory(restResponse.getResponse(), resourceCategoryEnum);
		
		Resource resource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, resourceCategoryEnum, UserRoleEnum.DESIGNER, true).left().value();
		List<String> createdResoucesName = new ArrayList<String>();
		createdResoucesName.add(resource.getName());
		
		restResponse = ResourceRestUtils.getResourceListFilterByCategory(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), AssetTypeEnum.RESOURCES.getValue(), resourceCategoryEnum.getCategory());
		
		Integer expectedResponseCode = 200;
		Assert.assertEquals(restResponse.getErrorCode(), expectedResponseCode);
		validateJsonContainResource(restResponse.getResponse(), createdResoucesName, true);
	}
	
	protected void validateJsonContainResource(String json, List<String> resourceNameList, Boolean willBeFound) {
		int lenResourceNameList = resourceNameList.size();
		Gson gson = new Gson();
		JsonElement jsonElement = new JsonParser().parse(json);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		for(JsonElement jElement: jsonArray) {
			ResourceReqDetails jResource = gson.fromJson(jElement, ResourceReqDetails.class);
			
			if(resourceNameList.contains(jResource.getName())) {
				resourceNameList.remove(jResource.getName());
			}
		}
		
		if(resourceNameList.size() != 0 && willBeFound) {			
			Assert.assertTrue(false, "Created resource not found on search filtered by category.");
		} else if (lenResourceNameList != resourceNameList.size() & !willBeFound) {
			Assert.assertTrue(false, "Some of the resources found when expect that no resource will be found.");
		}
	}
	
	
	
	protected void validateJsonContainResourceCategory(String json, ResourceCategoryEnum resourceCategoryEnum) {
		Gson gson = new Gson();
		JsonElement jelement = new JsonParser().parse(json);
		JsonArray jsonArray = jelement.getAsJsonArray();
		for(JsonElement jsonElement : jsonArray){
			CategoryDefinition categoryDefinition = gson.fromJson(jsonElement, CategoryDefinition.class);
			
			if(categoryDefinition.getName().equals(resourceCategoryEnum.getCategory())) { 
				for(SubCategoryDefinition subcategory: categoryDefinition.getSubcategories()) {
					if(subcategory.getName().equals(resourceCategoryEnum.getSubCategory())) {
						return;
					}
				}
			}
			
		}
		
		Assert.assertTrue(false, "Category and subcategory not found in categories list.");
	}

	
}
