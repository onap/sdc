package org.openecomp.sdc.ci.tests.utils.general;

import com.aventstack.extentreports.Status;
import org.json.JSONObject;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

import java.util.Arrays;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class VendorLicenseModelRestUtils {

    public static void updateVendorLicense(AmdocsLicenseMembers amdocsLicenseMembers, User user, Boolean isVlmUpdated) throws Exception {

//		create major method
        RestResponse creationMethodVendorLicense = creationMethodVendorLicense(amdocsLicenseMembers, user);
        assertEquals("did not succeed to create method for vendor license", 200, creationMethodVendorLicense.getErrorCode().intValue());
        amdocsLicenseMembers.setVersion(ResponseParser.getValueFromJsonResponse(creationMethodVendorLicense.getResponse(), "id"));

        if(isVlmUpdated) {
//		TODO update vlm do nothing
//		commit
            RestResponse commitVendorLicense = commitVendorLicense(amdocsLicenseMembers, user);
            assertEquals("did not succeed to commit vendor license", 200, commitVendorLicense.getErrorCode().intValue());
        }

//		submit
        RestResponse submitVendorLicense = submitVendorLicense(amdocsLicenseMembers, user);
        assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

        if(ComponentBaseTest.getExtendTest() != null){
            ComponentBaseTest.getExtendTest().log(Status.INFO, "Succeeded in updating the vendor license");
        }
    }

     private static RestResponse getVLMComponentByVersion(String vlmId, String vlmVersion, User user) throws Exception{
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_VLM_COMPONENT_BY_VERSION, config.getCatalogBeHost(),config.getCatalogBePort(), vlmId,vlmVersion);
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

    public static AmdocsLicenseMembers createVendorLicense(User user) throws Exception {

        AmdocsLicenseMembers amdocsLicenseMembers;
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

        amdocsLicenseMembers = new AmdocsLicenseMembers(vendorId, vendorLicenseName, vendorLicenseAgreementId, featureGroupId);
        amdocsLicenseMembers.setVersion(versionId); // Once object created and submitted, his initial version is 1.0

        RestResponse submitVendorLicense = submitVendorLicense(amdocsLicenseMembers, user);
        assertEquals("did not succeed to submit vendor license", 200, submitVendorLicense.getErrorCode().intValue());

//		ComponentBaseTest.getExtendTest().log(Status.INFO, "Succeeded in creating the vendor license");

        return amdocsLicenseMembers;
    }

    private static RestResponse actionOnComponent(String vspid, String body, String onboardComponent, User user, String componentVersion) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.ACTION_ON_COMPONENT, config.getCatalogBeHost(), config.getCatalogBePort(), onboardComponent, vspid, componentVersion);
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, body, headersMap);
        return response;
    }

    private static RestResponse createMethodVendorLicense(String vendorId, String body, String onboardComponent, User user, String componentVersion) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_METHOD, config.getCatalogBeHost(), config.getCatalogBePort(), onboardComponent, vendorId, componentVersion);
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, body, headersMap);
        return response;
    }

    public static RestResponse submitVendorLicense(AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {
        return actionOnComponent(amdocsLicenseMembers.getVendorId(), "{\"action\":\"Submit\"}", "vendor-license-models", user, amdocsLicenseMembers.getVersion());
    }

    /**
     * @param amdocsLicenseMembers
     * @param user
     * @return
     * checkOut exist VLM method
     * @throws Exception
     */
    public static RestResponse creationMethodVendorLicense(AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {
        String messageBody = "{\"description\":\"2.0\",\"creationMethod\":\"major\"}";
        return createMethodVendorLicense(amdocsLicenseMembers.getVendorId(), messageBody, "items", user, amdocsLicenseMembers.getVersion());
    }

    public static RestResponse commitVendorLicense(AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {
        String messageBody = "{\"action\":\"Commit\",\"commitRequest\":{\"message\":\"commit\"}}";
        return actionOnComponent(amdocsLicenseMembers.getVendorId(), messageBody, "items", user, amdocsLicenseMembers.getVersion());
    }

    public static RestResponse createVendorLicenseModels_1(String name, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_MODELS, config.getCatalogBeHost(), config.getCatalogBePort());
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
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_AGREEMENT, config.getCatalogBeHost(), config.getCatalogBePort(), vspid, versionId);
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
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_FEATURE_GROUPS, config.getCatalogBeHost(), config.getCatalogBePort(), vspid, versionId);
        String userId = user.getUserId();

        JSONObject jObject = new JSONObject();
        jObject.put("name", "xyz");
        jObject.put("description", "new vendor license feature groups");
        jObject.put("partNumber", "123abc456");
        jObject.put("manufacturerReferenceNumber", "5");
        jObject.put("addedLicenseKeyGroupsIds", Arrays.asList(licenseKeyGroupId).toArray());
        jObject.put("addedEntitlementPoolsIds", Arrays.asList(entitlementPoolId).toArray());

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, jObject.toString(), headersMap);
        return response;

    }

    public static RestResponse createVendorEntitlementPool_3(String vspid, String versionId, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_ENTITLEMENT_POOL, config.getCatalogBeHost(), config.getCatalogBePort(), vspid, versionId);
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
        String url = String.format(Urls.CREATE_VENDOR_LISENCE_KEY_GROUPS, config.getCatalogBeHost(), config.getCatalogBePort(), vspid, versionId);
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
        String url = String.format(Urls.VALIDATE_UPLOAD, config.getCatalogBeHost(), config.getCatalogBePort(), vspid,vspVersion);
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, body, headersMap);

        return response;
    }

}
