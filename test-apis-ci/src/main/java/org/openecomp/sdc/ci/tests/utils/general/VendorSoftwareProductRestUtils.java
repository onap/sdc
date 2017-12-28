package org.openecomp.sdc.ci.tests.utils.general;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.datatypes.enums.CvfcTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class VendorSoftwareProductRestUtils {

    public static Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct(ResourceReqDetails resourceReqDetails, String heatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers, Map<CvfcTypeEnum, String> cvfcArtifacts)
            throws Exception {

        Pair<String, VendorSoftwareProductObject> pair = createVSP(resourceReqDetails, heatFileName, filepath, user, amdocsLicenseMembers);
        if(cvfcArtifacts != null && ! cvfcArtifacts.isEmpty()){
            addCvfcArtifacts(cvfcArtifacts, user, pair.right);
        }
        prepareVspForUse(user, pair.right, true);
        return pair;
    }

    public static Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct(ResourceReqDetails resourceReqDetails, String heatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers)
            throws Exception {

        Map<CvfcTypeEnum, String> cvfcArtifacts = new HashMap<>();
        return createVendorSoftwareProduct(resourceReqDetails, heatFileName, filepath, user, amdocsLicenseMembers, cvfcArtifacts);
    }

    /**
     * @param user user
     * @param vendorSoftwareProductObject vendorSoftwareProductObject
     * @param isVspUpdated - in case isVspUpdated = false the commit API should not be issued
     * the method do commit, submit and create package
     * @throws Exception
     */
    public static void prepareVspForUse(User user, VendorSoftwareProductObject vendorSoftwareProductObject, Boolean isVspUpdated) throws Exception {

        if(isVspUpdated) {
            RestResponse commit = commitVendorSoftwareProduct(vendorSoftwareProductObject, user);
            assertEquals("did not succeed to commit new VSP", 200, commit.getErrorCode().intValue());
        }
        RestResponse submit = submitVendorSoftwareProduct(vendorSoftwareProductObject.getVspId(), user, vendorSoftwareProductObject.getComponentId());
        assertEquals("did not succeed to submit new VSP", 200, submit.getErrorCode().intValue());

        RestResponse createPackage = createPackageOfVendorSoftwareProduct(vendorSoftwareProductObject.getVspId(), user, vendorSoftwareProductObject.getComponentId());
        assertEquals("did not succeed to create package of new VSP ", 200, createPackage.getErrorCode().intValue());

    }

    public static VendorSoftwareProductObject createAndFillVendorSoftwareProduct(ResourceReqDetails resourceReqDetails, String heatFileName, String filePath, User user, AmdocsLicenseMembers amdocsLicenseMembers, Map<CvfcTypeEnum, String> cvfcArtifacts)
            throws Exception {

        Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = createVendorSoftwareProduct(resourceReqDetails, heatFileName, filePath, user, amdocsLicenseMembers, cvfcArtifacts);
        VendorSoftwareProductObject vendorSoftwareProductObject = fillVendorSoftwareProductObjectWithMetaData(heatFileName, createVendorSoftwareProduct);
        return vendorSoftwareProductObject;

    }


    public static Pair<String, VendorSoftwareProductObject> createVSP(ResourceReqDetails resourceReqDetails, String heatFileName, String filepath, User user, AmdocsLicenseMembers amdocsLicenseMembers) throws Exception {
        String vspName = handleFilename(heatFileName);

        if(ComponentBaseTest.getExtendTest() != null){
            ComponentBaseTest.getExtendTest().log(Status.INFO, "Starting to create the vendor software product");
        }

        Pair<RestResponse, VendorSoftwareProductObject> createNewVspPair = createNewVendorSoftwareProduct(resourceReqDetails, vspName, amdocsLicenseMembers, user);
        assertEquals("did not succeed to create new VSP", 200,createNewVspPair.left.getErrorCode().intValue());

        RestResponse uploadHeatPackage = uploadHeatPackage(filepath, heatFileName,  createNewVspPair.right, user);
        assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());

        RestResponse validateUpload = validateUpload(createNewVspPair.right, user);
        assertEquals("did not succeed to validate upload process, reason: " + validateUpload.getResponse(), 200, validateUpload.getErrorCode().intValue());

        Pair<String, VendorSoftwareProductObject> pair = new Pair<>(vspName, createNewVspPair.right);
        return pair;
    }

    public static void updateVspWithVfcArtifacts(String filepath, String updatedSnmpPoll, String updatedSnmpTrap, String componentInstanceId, User user, VendorSoftwareProductObject vendorSoftwareProductObject) throws Exception{
        RestResponse checkout = checkoutVendorSoftwareProduct(vendorSoftwareProductObject, user);
        assertEquals("did not succeed to checkout new VSP", 200, checkout.getErrorCode().intValue());
//		ExtentTestActions.log(Status.INFO, "Deleting SNMP POLL");
        deleteArtifactByType(componentInstanceId, vendorSoftwareProductObject, user, CvfcTypeEnum.SNMP_POLL);
//		ExtentTestActions.log(Status.INFO, "Deleting SNMP TRAP");
        deleteArtifactByType(componentInstanceId, vendorSoftwareProductObject, user, CvfcTypeEnum.SNMP_TRAP);
        addVFCArtifacts(filepath, updatedSnmpPoll, updatedSnmpTrap, vendorSoftwareProductObject, user, componentInstanceId);
        prepareVspForUse(user, vendorSoftwareProductObject, true);
    }

    private static RestResponse deleteArtifactByType(String componentInstanceId, VendorSoftwareProductObject vendorSoftwareProductObject, User user, CvfcTypeEnum snmpType) throws Exception
    {
        Config config = Utils.getConfig();
        String url = String.format(Urls.DELETE_AMDOCS_ARTIFACT_BY_TYPE, config.getCatalogBeHost(), config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId(), componentInstanceId, snmpType.getValue());
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendDelete(url, headersMap);
        return response;
    }

    public static void updateVendorSoftwareProductToNextVersion(VendorSoftwareProductObject vendorSoftwareProductObject, User user, Boolean isVspUpdated) throws Throwable {

        RestResponse createMethod = creationMethodVendorSoftwareProduct(vendorSoftwareProductObject, user);
        assertEquals("did not succeed to createMethod for new VSP", 200, createMethod.getErrorCode().intValue());
        prepareVspForUse(user,vendorSoftwareProductObject, isVspUpdated);

    }

    public static String handleFilename(String heatFileName) {
        final String namePrefix = String.format("%sVF%s", ElementFactory.getResourcePrefix(), "Onboarded-");
        final String nameSuffix = "-" + OnboardingUtils.getShortUUID();

        String subHeatFileName = heatFileName.substring(0, heatFileName.lastIndexOf("."));

        if ((namePrefix + subHeatFileName + nameSuffix).length() >= 50) {
            subHeatFileName = subHeatFileName.substring(0, 50 - namePrefix.length() - nameSuffix.length());
        }

        if (subHeatFileName.contains("(") || subHeatFileName.contains(")")) {
            subHeatFileName = subHeatFileName.replace("(", "-");
            subHeatFileName = subHeatFileName.replace(")", "-");
        }

        String vnfName = namePrefix + subHeatFileName + nameSuffix;
        return vnfName;
    }

    public static String addVFCArtifacts(String filepath, String snmpPoll, String snmpTrap, VendorSoftwareProductObject vendorSoftwareProductObject, User user, String componentInstanceId) throws Exception{
        componentInstanceId = (componentInstanceId == null) ? getVspComponentId(vendorSoftwareProductObject, user) : componentInstanceId;
        if (componentInstanceId != null){
            if (snmpPoll != null){
//				ExtentTestActions.log(Status.INFO, "Adding VFC artifact of type SNMP POLL with the file " + snmpPoll);
                RestResponse uploadSnmpPollArtifact = uploadSnmpPollArtifact(filepath, snmpPoll, vendorSoftwareProductObject, user, componentInstanceId);
                assertEquals("Did not succeed to add SNMP POLL", 200, uploadSnmpPollArtifact.getErrorCode().intValue());
            }
            if (snmpTrap != null){
//				ExtentTestActions.log(Status.INFO, "Adding VFC artifact of type SNMP TRAP with the file " + snmpTrap);
                RestResponse uploadSnmpTrapArtifact = uploadSnmpTrapArtifact(filepath, snmpTrap, vendorSoftwareProductObject, user, componentInstanceId);
                assertEquals("Did not succeed to add SNMP TRAP", 200, uploadSnmpTrapArtifact.getErrorCode().intValue());
            }
        }

        return componentInstanceId;
    }

    public static String addCvfcArtifacts(Map<CvfcTypeEnum, String> componentVfcArtifacts, User user, VendorSoftwareProductObject vendorSoftwareProductObject) throws Exception{
        String componentInstanceId = getVspComponentId(vendorSoftwareProductObject, user);
        if (componentInstanceId != null){
            for(Map.Entry<CvfcTypeEnum, String> entry : componentVfcArtifacts.entrySet()){
//				ExtentTestActions.log(Status.INFO, "Adding VFC artifact of type " + entry.getKey().getValue() + " with the file " + entry.getValue());
                RestResponse uploadSnmpPollArtifact = uploadCvfcArtifact(entry.getValue(), entry.getKey().getValue(), user, vendorSoftwareProductObject, componentInstanceId);
                assertEquals("Did not succeed to add " + entry.getKey().getValue(), BaseRestUtils.STATUS_CODE_SUCCESS, uploadSnmpPollArtifact.getErrorCode().intValue());
            }
        }
        return componentInstanceId;
    }

    public static String addVFCArtifacts(String filepath, String snmpPoll, String snmpTrap, VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception{
        return addVFCArtifacts(filepath, snmpPoll, snmpTrap, vendorSoftwareProductObject, user, null);
    }

    public static RestResponse uploadCvfcArtifact(String filepath, String cvfcType, User user, VendorSoftwareProductObject vendorSoftwareProductObject, String componentInstanceId) throws IOException {
        Config config = Utils.getConfig();
        String snmpPollUrl = String.format(Urls.UPLOAD_AMDOCS_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId(), componentInstanceId, cvfcType);
        return uploadFile(filepath, null, snmpPollUrl, user);
    }


    private static RestResponse uploadSnmpPollArtifact(String filepath, String zipArtifact, VendorSoftwareProductObject vendorSoftwareProductObject, User user, String componentInstanceId) throws IOException {
        Config config = Utils.getConfig();
        String snmpPollUrl = String.format(Urls.UPLOAD_SNMP_POLL_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId(), componentInstanceId);
        return uploadFile(filepath, zipArtifact, snmpPollUrl, user);
    }

    private static RestResponse uploadSnmpTrapArtifact(String filepath, String zipArtifact, VendorSoftwareProductObject vendorSoftwareProductObject, User user, String vspComponentId) throws IOException {
        Config config = Utils.getConfig();
        String snmpTrapUrl = String.format(Urls.UPLOAD_SNMP_POLL_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId(), vspComponentId);
        return uploadFile(filepath, zipArtifact, snmpTrapUrl, user);
    }

    private static RestResponse deleteSnmpArtifact(String componentId, String vspId, User user, SnmpTypeEnum snmpType) throws Exception
    {
        Config config = Utils.getConfig();
        String url = String.format(Urls.DELETE_AMDOCS_ARTIFACT_BY_TYPE, config.getCatalogBeHost(),config.getCatalogBePort(), vspId, componentId, snmpType.getValue());
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendDelete(url, headersMap);
        return response;
    }


    /**
     * @param vendorSoftwareProductObject VendorSoftwareProductObject
     * @param user user object
     * @return return first found component instance Id from list
     * @throws Exception Exception
     */
    private static String getVspComponentId(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        RestResponse componentList = getVSPComponents(vendorSoftwareProductObject, user);
        String response = componentList.getResponse();
        Map<String, Object> responseMap = (Map<String, Object>) JSONValue.parse(response);
        JSONArray results = (JSONArray)responseMap.get("results");
        for (Object res : results){
            Map<String, Object> componentMap = (Map<String, Object>) JSONValue.parse(res.toString());
            String componentInstanceId = componentMap.get("id").toString();
            return componentInstanceId;
        }
        return null;
    }

    private static RestResponse getVSPComponents(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception{
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_VSP_COMPONENTS, config.getCatalogBeHost(),config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(user.getUserId());

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendGet(url, headersMap);
        return response;
    }



    public static boolean validateVspExist(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        RestResponse restResponse = getVSPComponentByVersion(vendorSoftwareProductObject, user);
        assertEquals(String.format("Vsp version not updated, reponse message: %s", restResponse.getResponse()),restResponse.getErrorCode().intValue(),200);
        return (restResponse.getErrorCode()==200);
    }


    private static RestResponse getVSPComponentByVersion(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception{
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_VSP_COMPONENT_BY_VERSION, config.getCatalogBeHost(),config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendGet(url, headersMap);
        return response;
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

    public static Pair<RestResponse, VendorSoftwareProductObject> createNewVendorSoftwareProduct(ResourceReqDetails resourceReqDetails, String vspName, AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {

        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_VENDOR_SOFTWARE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort());
        String userId = user.getUserId();
        VendorSoftwareProductObject vendorSoftwareProductObject = new VendorSoftwareProductObject();
        LicensingData licensingData = new LicensingData(amdocsLicenseMembers.getVendorLicenseAgreementId(), Arrays.asList(amdocsLicenseMembers.getFeatureGroupId()));
        ResourceCategoryEnum resourceCategoryEnum = ResourceCategoryEnum.findEnumNameByValues(resourceReqDetails.getCategories().get(0).getName(), resourceReqDetails.getCategories().get(0).getSubcategories().get(0).getName());

        vendorSoftwareProductObject.setName(vspName);
        vendorSoftwareProductObject.setDescription(resourceReqDetails.getDescription());
        vendorSoftwareProductObject.setCategory(resourceCategoryEnum.getCategoryUniqeId());
        vendorSoftwareProductObject.setSubCategory(resourceCategoryEnum.getSubCategoryUniqeId());
        vendorSoftwareProductObject.setOnboardingMethod("NetworkPackage");
        vendorSoftwareProductObject.setVendorName(amdocsLicenseMembers.getVendorLicenseName());
        vendorSoftwareProductObject.setVendorId(amdocsLicenseMembers.getVendorId());
        vendorSoftwareProductObject.setIcon("icon");
        vendorSoftwareProductObject.setLicensingData(licensingData);
        vendorSoftwareProductObject.setLicensingVersion(amdocsLicenseMembers.getVersion());

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        Gson gson = new Gson();
        String body = gson.toJson(vendorSoftwareProductObject);

        RestResponse response = http.httpSendPost(url, body, headersMap);

        vendorSoftwareProductObject.setVspId(ResponseParser.getValueFromJsonResponse(response.getResponse(), "itemId"));
        vendorSoftwareProductObject.setComponentId(ResponseParser.getValueFromJsonResponse(response.getResponse(), "version:id"));
//		vendorSoftwareProductObject.setVersion(ResponseParser.getValueFromJsonResponse(response.getResponse(), "version:name"));
        vendorSoftwareProductObject.setAttContact(user.getUserId());

        return new Pair<>(response, vendorSoftwareProductObject);
    }

    public static RestResponse validateUpload(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.VALIDATE_UPLOAD, config.getCatalogBeHost(), config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, null, headersMap);

        return response;
    }

    public static RestResponse uploadHeatPackage(String filepath, String filename, VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.UPLOAD_HEAT_PACKAGE, config.getCatalogBeHost(), config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        return uploadFile(filepath, filename, url, user);
    }

    private static RestResponse uploadFile(String filepath, String filename, String url, User user) throws IOException{
        CloseableHttpResponse response = null;

        MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();
        mpBuilder.addPart("upload", new FileBody(getTestZipFile(filepath, filename)));

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(user.getUserId());
        headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "multipart/form-data");

        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            RestResponse restResponse = new RestResponse();

            Iterator<String> iterator = headersMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = headersMap.get(key);
                httpPost.addHeader(key, value);
            }
            httpPost.setEntity(mpBuilder.build());
            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = null;
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(instream, writer);
                    responseBody = writer.toString();
                } finally {
                    instream.close();
                }
            }

            restResponse.setErrorCode(response.getStatusLine().getStatusCode());
            restResponse.setResponse(responseBody);

            return restResponse;

        } finally {
            closeResponse(response);
            closeHttpClient(client);

        }
    }

    private static void closeResponse(CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            System.out.println(String.format("failed to close client or response: %s", e.getMessage()));
        }
    }

    private static void closeHttpClient(CloseableHttpClient client) {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            System.out.println(String.format("failed to close client or response: %s", e.getMessage()));
        }
    }

    private static File getTestZipFile(String filepath, String filename) throws IOException {
        Config config = Utils.getConfig();
        String sourceDir = config.getImportResourceTestsConfigDir();
        java.nio.file.Path filePath;
        if(filename == null){
            filePath = FileSystems.getDefault().getPath(filepath);
        }else{
            filePath = FileSystems.getDefault().getPath(filepath + File.separator + filename);
        }
        return filePath.toFile();
    }

    public static RestResponse checkinVendorSoftwareProduct(User user, VendorSoftwareProductObject vendorSoftwareProductObject) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.UPDATE_VSP, config.getCatalogBeHost(), config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());

        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
//		unset vspId, componentId, attContact, onboardingMethod
        String vspId = vendorSoftwareProductObject.getVspId();
        String componentId = vendorSoftwareProductObject.getComponentId();
        String attContact = vendorSoftwareProductObject.getAttContact();
        String onboardingMethod = vendorSoftwareProductObject.getOnboardingMethod();
        vendorSoftwareProductObject.setVspId(null);
        vendorSoftwareProductObject.setComponentId(null);
        vendorSoftwareProductObject.setAttContact(null);
        vendorSoftwareProductObject.setOnboardingMethod(null);
        Gson gson = new Gson();
        String body = gson.toJson(vendorSoftwareProductObject);
        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPut(url, body, headersMap);
//		set back vspId, componentId, attContact, onboardingMethod
        vendorSoftwareProductObject.setVspId(vspId);
        vendorSoftwareProductObject.setComponentId(componentId);
        vendorSoftwareProductObject.setAttContact(attContact);
        vendorSoftwareProductObject.setOnboardingMethod(onboardingMethod);

        return response;
    }

    public static RestResponse checkoutVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        return actionOnComponent(vendorSoftwareProductObject.getVspId(), "{\"action\":\"Checkout\"}", "vendor-software-products", user, vendorSoftwareProductObject.getComponentId());
    }

    public static RestResponse commitVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        String messageBody = "{\"action\":\"Commit\",\"commitRequest\":{\"message\":\"commit\"}}";
        return actionOnComponent(vendorSoftwareProductObject.getVspId(), messageBody, "items", user, vendorSoftwareProductObject.getComponentId());
    }

    public static RestResponse submitVendorSoftwareProduct(String vspid, User user, String componentId) throws Exception {
        return actionOnComponent(vspid, "{\"action\":\"Submit\"}", "vendor-software-products", user, componentId);
    }

    public static RestResponse createPackageOfVendorSoftwareProduct(String vspid, User user, String componentId) throws Exception {
        return actionOnComponent(vspid, "{\"action\":\"Create_Package\"}", "vendor-software-products", user, componentId);
    }

    public static RestResponse creationMethodVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, User user) throws Exception {
        String messageBody = "{\"description\":\"2.0\",\"creationMethod\":\"major\"}";
        return createMethodVendorSoftwareProduct(vendorSoftwareProductObject, messageBody, "items", user);
    }

    private static RestResponse createMethodVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, String body, String onboardComponent, User user) throws Exception {
        Config config = Utils.getConfig();
        String url = String.format(Urls.CREATE_METHOD, config.getCatalogBeHost(), config.getCatalogBePort(), onboardComponent, vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        String userId = user.getUserId();
        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);

        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendPost(url, body, headersMap);
        if(response.getErrorCode().intValue() == 200) {
            vendorSoftwareProductObject.setComponentId(ResponseParser.getValueFromJsonResponse(response.getResponse(), "id"));
        }
        return response;
    }

    public static VendorSoftwareProductObject updateVSPWithNewVLMParameters(VendorSoftwareProductObject vendorSoftwareProductObject, AmdocsLicenseMembers amdocsLicenseMembers, User user) throws Exception {

        RestResponse createMethod = creationMethodVendorSoftwareProduct(vendorSoftwareProductObject, user);
        assertEquals("did not succeed to checkout new VSP", 200, createMethod.getErrorCode().intValue());
//        vendorSoftwareProductObject.setComponentId(ResponseParser.getValueFromJsonResponse(createMethod.getResponse(), "id"));

        String licensingVersion = amdocsLicenseMembers.getVersion();
        LicensingData licensingData = new LicensingData(amdocsLicenseMembers.getVendorLicenseAgreementId(), Arrays.asList(amdocsLicenseMembers.getFeatureGroupId()));
        vendorSoftwareProductObject.setVendorId(amdocsLicenseMembers.getVendorId());
        vendorSoftwareProductObject.setVendorName(amdocsLicenseMembers.getVendorLicenseName());
        vendorSoftwareProductObject.setLicensingVersion(licensingVersion);
        vendorSoftwareProductObject.setLicensingData(licensingData);

        VendorSoftwareProductObjectReqDetails vendorSoftwareProductObjectReqDetails = new VendorSoftwareProductObjectReqDetails(
                vendorSoftwareProductObject.getName(), vendorSoftwareProductObject.getDescription(), vendorSoftwareProductObject.getCategory(),
                vendorSoftwareProductObject.getSubCategory(), vendorSoftwareProductObject.getVendorId(), vendorSoftwareProductObject.getVendorName(),
                vendorSoftwareProductObject.getLicensingVersion(), vendorSoftwareProductObject.getLicensingData(),
                null, null, null, vendorSoftwareProductObject.getIcon()	);

        Gson gson = new Gson();
        String body = gson.toJson(vendorSoftwareProductObjectReqDetails);
        RestResponse updateResponse = updateVendorSoftwareProduct(vendorSoftwareProductObject, body, user);
        assertEquals("did not succeed to update VSP", 200, updateResponse.getErrorCode().intValue());

        prepareVspForUse(user, vendorSoftwareProductObject, true);

        return vendorSoftwareProductObject;
    }

    public static RestResponse updateVendorSoftwareProduct(VendorSoftwareProductObject vendorSoftwareProductObject, String body, User user) throws Exception {

        Config config = Utils.getConfig();
        String url = String.format(Urls.UPDATE_VSP, config.getCatalogBeHost(), config.getCatalogBePort(), vendorSoftwareProductObject.getVspId(), vendorSoftwareProductObject.getComponentId());
        String userId = user.getUserId();

        Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
        HttpRequest http = new HttpRequest();

        RestResponse response = http.httpSendPut(url, body, headersMap);
        return response;
    }

//	private static void importUpdateVSP(Pair<String, Map<String, String>> vsp, boolean isUpdate) throws Exception{
//		String vspName = vsp.left;
//		Map<String, String> vspMetadata = vsp.right;
//		boolean vspFound = HomePage.searchForVSP(vspName);
//
//		if (vspFound){
//
//			List<WebElement> elemenetsFromTable = HomePage.getElemenetsFromTable();
////			WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
////			WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elemenetsFromTable.get(1)));
////			findElement.click();
//			elemenetsFromTable.get(1).click();
//			GeneralUIUtils.waitForLoader();
//
//			if (isUpdate){
//				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.UPDATE_VSP.getValue());
//
//			}
//			else{
//				GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.IMPORT_VSP.getValue());
//			}
//
//			String lifeCycleState = ResourceGeneralPage.getLifeCycleState();
//			boolean needCheckout = lifeCycleState.equals(LifeCycleStateEnum.CHECKIN.getValue()) || lifeCycleState.equals(LifeCycleStateEnum.CERTIFIED.getValue());
//			if (needCheckout)
//			{
//				try {
//					ResourceGeneralPage.clickCheckoutButton();
//					Assert.assertTrue(ResourceGeneralPage.getLifeCycleState().equals(LifeCycleStateEnum.CHECKOUT.getValue()), "Did not succeed to checkout");
//
//				} catch (Exception e) {
//					ExtentTestActions.log(Status.ERROR, "Did not succeed to checkout");
//					e.printStackTrace();
//				}
//				GeneralUIUtils.waitForLoader();
//			}
//
//			//Metadata verification
//			VfVerificator.verifyOnboardedVnfMetadata(vspName, vspMetadata);
//
//			ExtentTestActions.log(Status.INFO, "Clicking create/update VNF");
//			String duration = GeneralUIUtils.getActionDuration(() -> waitUntilVnfCreated());
//		    ExtentTestActions.log(Status.INFO, "Succeeded in importing/updating " + vspName, duration);
//		}
//		else{
//			Assert.fail("Did not find VSP named " + vspName);
//		}
//	}

//	private static void waitUntilVnfCreated() {
//		GeneralUIUtils.clickOnElementByTestIdWithoutWait(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
//		GeneralUIUtils.waitForLoader(60*10);
//		GeneralUIUtils.waitForAngular();
//		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
//	}
//
//	public static void updateVSP(Pair<String, Map<String, String>> vsp) throws Exception{
//		ExtentTestActions.log(Status.INFO, "Updating VSP " + vsp.left);
//		importUpdateVSP(vsp, true);
//	}
//
//	public static void importVSP(Pair<String, Map<String, String>> vsp) throws Exception{
//		ExtentTestActions.log(Status.INFO, "Importing VSP " + vsp.left);
//		importUpdateVSP(vsp, false);
//	}
//
//	public static void updateVnfAndValidate(String filepath, Pair<String, Map<String, String>> vsp, String updatedVnfFile, User user) throws Exception, Throwable {
//		ExtentTestActions.log(Status.INFO, String.format("Going to update the VNF with %s......", updatedVnfFile));
//		System.out.println(String.format("Going to update the VNF with %s......", updatedVnfFile));
//
//		Map<String, String> vspMap = vsp.right;
//		String vspId = vspMap.get("vspId");
//
//		updateVendorSoftwareProduct(vspId, updatedVnfFile, filepath, user);
//		HomePage.showVspRepository();
//		updateVSP(vsp);
//		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
//		DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, updatedVnfFile);
//	}
//
//	public static Pair<String, Map<String, String>> onboardAndValidate(String filepath, String vnfFile, User user) throws Exception {
//		ExtentTestActions.log(Status.INFO, String.format("Going to onboard the VNF %s", vnfFile));
//		System.out.println(String.format("Going to onboard the VNF %s", vnfFile));
//
//		AmdocsLicenseMembers amdocsLicenseMembers = createVendorLicense(user);
//		Pair<String, Map<String, String>> createVendorSoftwareProduct = createVendorSoftwareProduct(vnfFile, filepath, user, amdocsLicenseMembers);
//		String vspName = createVendorSoftwareProduct.left;
//
//		DownloadManager.downloadCsarByNameFromVSPRepository(vspName, createVendorSoftwareProduct.right.get("vspId"));
//		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
//
//		ExtentTestActions.log(Status.INFO, String.format("Searching for onboarded %s", vnfFile));
//		HomePage.showVspRepository();
//		ExtentTestActions.log(Status.INFO,String.format("Going to import %s", vnfFile.substring(0, vnfFile.indexOf("."))));
//		importVSP(createVendorSoftwareProduct);
//
//		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
//
//		// Verify deployment artifacts
//		Map<String, Object> combinedMap = ArtifactFromCsar.combineHeatArtifacstWithFolderArtifacsToMap(latestFilefromDir.getAbsolutePath());
//
//		LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts = ((LinkedList<HeatMetaFirstLevelDefinition>) combinedMap.get("Deployment"));
//		ArtifactsCorrelationManager.addVNFartifactDetails(vspName, deploymentArtifacts);
//
//		List<String> heatEnvFilesFromCSAR = deploymentArtifacts.stream().filter(e -> e.getType().equals("HEAT_ENV")).
//																		 map(e -> e.getFileName()).
//																		 collect(Collectors.toList());
//
//		validateDeploymentArtifactsVersion(deploymentArtifacts, heatEnvFilesFromCSAR);
//
//		DeploymentArtifactPage.verifyArtifactsExistInTable(filepath, vnfFile);
//		return createVendorSoftwareProduct;
//	}
//
//	public static void validateDeploymentArtifactsVersion(LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts,
//			List<String> heatEnvFilesFromCSAR) {
//		String artifactVersion;
//		String artifactName;
//
//		for(HeatMetaFirstLevelDefinition deploymentArtifact: deploymentArtifacts) {
//			artifactVersion = "1";
//
//			if(deploymentArtifact.getType().equals("HEAT_ENV")) {
//				continue;
//			} else if(deploymentArtifact.getFileName().contains(".")) {
//				artifactName = deploymentArtifact.getFileName().trim().substring(0, deploymentArtifact.getFileName().lastIndexOf("."));
//			} else {
//				artifactName = deploymentArtifact.getFileName().trim();
//			}
//
//			if (heatEnvFilesFromCSAR.contains(artifactName + ".env")){
//				artifactVersion = "2";
//			}
//			ArtifactUIUtils.validateArtifactNameVersionType(artifactName, artifactVersion, deploymentArtifact.getType());
//		}
//	}


    public static VendorSoftwareProductObject fillVendorSoftwareProductObjectWithMetaData(String vnfFile, Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct) {
        VendorSoftwareProductObject vendorSoftwareProductObject = new VendorSoftwareProductObject();
        VendorSoftwareProductObject map = createVendorSoftwareProduct.right;
        vendorSoftwareProductObject.setAttContact(map.getAttContact());
        vendorSoftwareProductObject.setCategory(map.getCategory());
        vendorSoftwareProductObject.setComponentId(map.getComponentId());
        vendorSoftwareProductObject.setDescription(map.getDescription());
        vendorSoftwareProductObject.setSubCategory(map.getSubCategory());
        vendorSoftwareProductObject.setVendorName(map.getVendorName());
        vendorSoftwareProductObject.setVspId(map.getVspId());
        vendorSoftwareProductObject.setName(createVendorSoftwareProduct.left);
        String[] arrFileNameAndExtension = vnfFile.split("\\.");
        vendorSoftwareProductObject.setOnboardingMethod("NetworkPackage");
        vendorSoftwareProductObject.setNetworkPackageName(arrFileNameAndExtension[0]);
        vendorSoftwareProductObject.setOnboardingOrigin(arrFileNameAndExtension[1]);

        return vendorSoftwareProductObject;
    }

    public static void updateVendorSoftwareProductToNextVersion(VendorSoftwareProductObject vendorSoftwareProductObject, User user, String filepath, String heatFileName) throws Throwable {

        RestResponse createMethod = creationMethodVendorSoftwareProduct(vendorSoftwareProductObject, user);
        assertEquals("did not succeed to createMethod for new VSP", 200, createMethod.getErrorCode().intValue());

        RestResponse uploadHeatPackage = uploadHeatPackage(filepath, heatFileName, vendorSoftwareProductObject, user);
        assertEquals("did not succeed to upload HEAT package", 200, uploadHeatPackage.getErrorCode().intValue());

        RestResponse validateUpload = validateUpload(vendorSoftwareProductObject, user);
        assertEquals("did not succeed to validate upload process, reason: " + validateUpload.getResponse(), 200, validateUpload.getErrorCode().intValue());

        prepareVspForUse(user,vendorSoftwareProductObject, true);
    }


}
