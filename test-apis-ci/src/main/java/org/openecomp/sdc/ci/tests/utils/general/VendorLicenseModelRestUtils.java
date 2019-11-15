/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.utils.general;

import com.aventstack.extentreports.Status;
import org.json.JSONObject;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

import java.util.Arrays;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class VendorLicenseModelRestUtils {

    public static void updateVendorLicense(VendorLicenseModel vendorLicenseModel, User user, Boolean isVlmUpdated) throws Exception {

//		create major method
        RestResponse creationMethodVendorLicense = creationMethodVendorLicense(vendorLicenseModel, user);
        assertEquals("did not succeed to create method for vendor license", 200, creationMethodVendorLicense.getErrorCode().intValue());
        vendorLicenseModel
            .setVersion(ResponseParser.getValueFromJsonResponse(creationMethodVendorLicense.getResponse(), "id"));

        if(isVlmUpdated) {
//		TODO update vlm do nothing
//		commit
            RestResponse commitVendorLicense = commitVendorLicense(vendorLicenseModel, user);
            assertEquals("did not succeed to commit vendor license", 200, commitVendorLicense.getErrorCode().intValue());
        }

//		submit
        RestResponse submitVendorLicense = submitVendorLicense(vendorLicenseModel, user);
        assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

        if(ComponentBaseTest.getExtendTest() != null){
            ComponentBaseTest.getExtendTest().log(Status.INFO, "Succeeded in updating the vendor license");
        }
    }

     private static RestResponse getVLMComponentByVersion(String vlmId, String vlmVersion, User user) throws Exception{
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_VLM_COMPONENT_BY_VERSION, config.getOnboardingBeHost(),config.getOnboardingBePort(), vlmId,vlmVersion);
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendGet(url, headersMap);
        return response;
    }

     public static boolean validateVlmExist(String vlmId, String vlmVersion, User user) throws Exception {
        RestResponse restResponse = getVLMComponentByVersion(vlmId, vlmVersion, user);
        assertEquals(String.format("VLM version not updated, reponse code message: %s", restResponse.getResponse()),restResponse.getErrorCode().intValue(),200);
        return (restResponse.getErrorCode()==200);
    }

    public static VendorLicenseModel createVendorLicense(User user) throws Exception {

        VendorLicenseModel vendorLicenseModel;
//		ComponentBaseTest.getExtendTest().log(Status.INFO, "Starting to create the vendor license");
        String vendorLicenseName = "ciLicense" + OnboardingUtils.getShortUUID();
        RestResponse vendorLicenseResponse = createVendorLicenseModels_1(vendorLicenseName, user);
        assertEquals("did not succeed to create vendor license model", 200, vendorLicenseResponse.getErrorCode().intValue());
        String vendorId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "itemId");
        String versionId = ResponseParser.getValueFromJsonResponse(vendorLicenseResponse.getResponse(), "version:id");

        RestResponse vendorKeyGroupsResponse = createVendorKeyGroups_2(vendorId, versionId, user);
        assertEquals("did not succeed to create vendor key groups", 200, vendorKeyGroupsResponse.getErrorCode().intValue());
        String keyGroupId = ResponseParser.getValueFromJsonResponse(vendorKeyGroupsResponse.getResponse(), "value");

        RestResponse vendorEntitlementPool = createVendorEntitlementPool_3(vendorId, versionId, user);
        assertEquals("did not succeed to create vendor entitlement pool", 200, vendorEntitlementPool.getErrorCode().intValue());
        String entitlementPoolId = ResponseParser.getValueFromJsonResponse(vendorEntitlementPool.getResponse(), "value");

        RestResponse vendorLicenseFeatureGroups = createVendorLicenseFeatureGroups_4(vendorId, versionId, keyGroupId, entitlementPoolId, user);
        assertEquals("did not succeed to create vendor license feature groups", 200, vendorLicenseFeatureGroups.getErrorCode().intValue());
        String featureGroupId = ResponseParser.getValueFromJsonResponse(vendorLicenseFeatureGroups.getResponse(), "value");

        RestResponse vendorLicenseAgreement = createVendorLicenseAgreement_5(vendorId, versionId, featureGroupId, user);
        assertEquals("did not succeed to create vendor license agreement", 200, vendorLicenseAgreement.getErrorCode().intValue());
        String vendorLicenseAgreementId = ResponseParser.getValueFromJsonResponse(vendorLicenseAgreement.getResponse(), "value");

//		RestResponse checkinVendorLicense = OnboardingUtils.checkinVendorLicense(vendorId, user, versionId);
//		assertEquals("did not succeed to checkin vendor license", 200, checkinVendorLicense.getErrorCode().intValue());

        vendorLicenseModel = new VendorLicenseModel(vendorId, vendorLicenseName, vendorLicenseAgreementId, featureGroupId);
        vendorLicenseModel.setVersion(versionId); // Once object created and submitted, his initial version is 1.0

        RestResponse submitVendorLicense = submitVendorLicense(vendorLicenseModel, user);
        assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

//		ComponentBaseTest.getExtendTest().log(Status.INFO, "Succeeded in creating the vendor license");

        return vendorLicenseModel;
    }

    private static RestResponse actionOnComponent(String vspid, String body, String onboardComponent, User user, String componentVersion) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.ACTION_ON_COMPONENT, config.getOnboardingBeHost(), config.getOnboardingBePort(), onboardComponent, vspid, componentVersion);
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, body, headersMap);
        return response;
    }

    private static RestResponse createMethodVendorLicense(String vendorId, String body, String onboardComponent, User user, String componentVersion) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_METHOD, config.getOnboardingBeHost(), config.getOnboardingBePort(), onboardComponent, vendorId, componentVersion);
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, body, headersMap);
        return response;
    }

    public static RestResponse submitVendorLicense(VendorLicenseModel vendorLicenseModel, User user) throws Exception {
        return actionOnComponent(vendorLicenseModel.getVendorId(), "{\"action\":\"Submit\"}", "vendor-license-models", user, vendorLicenseModel
            .getVersion());
    }

    /**
     * @param vendorLicenseModel
     * @param user
     * @return
     * checkOut exist VLM method
     * @throws Exception
     */
    public static RestResponse creationMethodVendorLicense(VendorLicenseModel vendorLicenseModel, User user) throws Exception {
        String messageBody = "{\"description\":\"2.0\",\"creationMethod\":\"major\"}";
        return createMethodVendorLicense(vendorLicenseModel.getVendorId(), messageBody, "items", user, vendorLicenseModel
            .getVersion());
    }

    public static RestResponse commitVendorLicense(VendorLicenseModel vendorLicenseModel, User user) throws Exception {
        String messageBody = "{\"action\":\"Commit\",\"commitRequest\":{\"message\":\"commit\"}}";
        return actionOnComponent(vendorLicenseModel.getVendorId(), messageBody, "items", user, vendorLicenseModel.getVersion());
    }

    public static RestResponse createVendorLicenseModels_1(String name, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_MODELS, config.getOnboardingBeHost(), config.getOnboardingBePort());
        String userId = user.getUserId();

        JSONObject jObject = new JSONObject();
        jObject.put("vendorName", name);
        jObject.put("description", "new vendor license model");
        jObject.put("iconRef", "icon");

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
        return response;

    }

    public static RestResponse createVendorLicenseAgreement_5(String vspid, String versionId, String featureGroupId, User user)
            throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_AGREEMENT, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspid, versionId);
        String userId = user.getUserId();

        JSONObject licenseTermpObject = new JSONObject();
        licenseTermpObject.put("choice", "Fixed_Term");
        licenseTermpObject.put("other", "");

        JSONObject jObjectBody = new JSONObject();
        jObjectBody.put("name", "abc");
        jObjectBody.put("description", "new vendor license agreement");
        jObjectBody.put("requirementsAndConstrains", "abc");
        jObjectBody.put("licenseTerm", licenseTermpObject);
        jObjectBody.put("addedFeatureGroupsIds", Arrays.asList(featureGroupId).toArray());

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
        return response;
    }

    public static RestResponse createVendorLicenseFeatureGroups_4(String vspid, String versionId, String licenseKeyGroupId,
                                                                  String entitlementPoolId, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_FEATURE_GROUPS, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspid, versionId);
        String userId = user.getUserId();

        JSONObject jObject = new JSONObject();
        jObject.put("name", "xyz");
        jObject.put("description", "new vendor license feature groups");
        jObject.put("partNumber", "123abc456");
//      jObject.put("manufacturerReferenceNumber", "5");
        jObject.put("addedLicenseKeyGroupsIds", Arrays.asList(licenseKeyGroupId).toArray());
        jObject.put("addedEntitlementPoolsIds", Arrays.asList(entitlementPoolId).toArray());

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
        return response;

    }

    public static RestResponse createVendorEntitlementPool_3(String vspid, String versionId, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_ENTITLEMENT_POOL, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspid, versionId);
        String userId = user.getUserId();

        JSONObject jEntitlementMetricObject = new JSONObject();
        jEntitlementMetricObject.put("choice", "CPU");
        jEntitlementMetricObject.put("other", "");

        JSONObject jAggregationFunctionObject = new JSONObject();
        jAggregationFunctionObject.put("choice", "Peak");
        jAggregationFunctionObject.put("other", "");

        JSONObject jOperationalScope = new JSONObject();
        jOperationalScope.put("choices", Arrays.asList("Availability_Zone").toArray());
        jOperationalScope.put("other", "");

        JSONObject jTimeObject = new JSONObject();
        jTimeObject.put("choice", "Hour");
        jTimeObject.put("other", "");

        JSONObject jObjectBody = new JSONObject();
        jObjectBody.put("name", "def"+ OnboardingUtils.getShortUUID());
        jObjectBody.put("description", "new vendor license entitlement pool");
        jObjectBody.put("thresholdValue", "23");
        jObjectBody.put("thresholdUnits", "Absolute");
        jObjectBody.put("entitlementMetric", jEntitlementMetricObject);
        jObjectBody.put("increments", "abcd");
        jObjectBody.put("aggregationFunction", jAggregationFunctionObject);
        jObjectBody.put("operationalScope", jOperationalScope);
        jObjectBody.put("time", jTimeObject);
        jObjectBody.put("manufacturerReferenceNumber", "123aaa");

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
        return response;
    }

    public static RestResponse createVendorKeyGroups_2(String vspid, String versionId, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_KEY_GROUPS, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspid, versionId);
        String userId = user.getUserId();

        JSONObject jOperationalScope = new JSONObject();
        jOperationalScope.put("choices", Arrays.asList("Tenant").toArray());
        jOperationalScope.put("other", "");

        JSONObject jObjectBody = new JSONObject();
        jObjectBody.put("name", "keyGroup" + OnboardingUtils.getShortUUID());
        jObjectBody.put("description", "new vendor license key group");
        jObjectBody.put("operationalScope", jOperationalScope);
        jObjectBody.put("type", "Universal");

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObjectBody.toString(), headersMap);
        return response;
    }

    public static RestResponse validateUpload(String vspid, User user, String vspVersion) throws Exception {
        String body = null;
        Config config = Utils.getConfig();
        String url = String.format(Urls.VALIDATE_UPLOAD, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspid,vspVersion);
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, body, headersMap);

        return response;
    }

}
