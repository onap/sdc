package org.openecomp.sdc.ci.tests.execute.resource;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by chaya on 6/15/2017.
 */
public class GetLeftPaletteTest extends ComponentBaseTest {

    private static Logger logger = LoggerFactory.getLogger(GetLeftPaletteTest.class.getName());
    protected User designerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
    protected ResourceReqDetails vlResourceDetails;
    protected ResourceReqDetails cpResourceDetails;
    protected ResourceReqDetails vfcResourceDetails;
    protected ResourceReqDetails vfcmtResourceDetails;
    protected ResourceReqDetails vfResourceDetails;


    public static TestName name = new TestName();

    public GetLeftPaletteTest() {
        super(name, GetLeftPaletteTest.class.getName());
    }

    @BeforeClass
    public void setUp() throws Exception {
        // create VL
        vlResourceDetails = createResource(vlResourceDetails, "vlRes", ResourceTypeEnum.VL);
        checkInResource(vlResourceDetails);

        // create CP
        cpResourceDetails = createResource(cpResourceDetails, "cpRes", ResourceTypeEnum.CP);
        checkInResource(cpResourceDetails);

        // create VFC
        vfcResourceDetails = createResource(vfcResourceDetails, "vfcRes", ResourceTypeEnum.VFC);
        checkInResource(vfcResourceDetails);

        // create VFCMT
        vfcmtResourceDetails = createResource(vfcmtResourceDetails, "vfcmtRes", ResourceTypeEnum.VFCMT);
        checkInResource(vfcmtResourceDetails);

        // create VF
        vfResourceDetails = createResource(vfResourceDetails, "vfRes", ResourceTypeEnum.VF);
        checkInResource(vfResourceDetails);
    }



    @Test
    public void testGetLeftPaletteForPNF() throws IOException {

        RestResponse getResourceLatestVersionResponse = ResourceRestUtils.getResourceLatestVersionListMetadata(designerDetails, "PNF");
        assertTrue("response code is not 200, returned :" + getResourceLatestVersionResponse.getErrorCode(),
                getResourceLatestVersionResponse.getErrorCode() == 200);

        String json = getResourceLatestVersionResponse.getResponse();
        JSONArray jsonResp = (JSONArray) JSONValue.parse(json);

        AssertJUnit.assertTrue("check vlResource is in response",
                isComponentInArray(vlResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertTrue("check cpResource is in response",
                isComponentInArray(cpResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfcResource is not in response",
                isComponentInArray(vfcResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfResource is not in response",
                isComponentInArray(vfResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfcmtResource is not in response",
                isComponentInArray(vfcmtResourceDetails.getUniqueId(), jsonResp));

    }

    @Test
    public void testGetLeftPaletteForVF() throws IOException {

        RestResponse getResourceLatestVersionResponse = ResourceRestUtils.getResourceLatestVersionListMetadata(designerDetails, "VF");
        assertTrue("response code is not 200, returned :" + getResourceLatestVersionResponse.getErrorCode(),
                getResourceLatestVersionResponse.getErrorCode() == 200);

        String json = getResourceLatestVersionResponse.getResponse();
        JSONArray jsonResp = (JSONArray) JSONValue.parse(json);

        AssertJUnit.assertTrue("check vlResource is in response",
                isComponentInArray(vlResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertTrue("check cpResource is in response",
                isComponentInArray(cpResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertTrue("check vfcResource is not in response",
                isComponentInArray(vfcResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfResource is not in response",
                isComponentInArray(vfResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfcmtResource is not in response",
                isComponentInArray(vfcmtResourceDetails.getUniqueId(), jsonResp));

    }

    @Test
    public void testGetLeftPaletteForService() throws IOException {

        RestResponse getResourceLatestVersionResponse = ResourceRestUtils.getResourceLatestVersionListMetadata(designerDetails, "SERVICE");
        assertTrue("response code is not 200, returned :" + getResourceLatestVersionResponse.getErrorCode(),
                getResourceLatestVersionResponse.getErrorCode() == 200);

        String json = getResourceLatestVersionResponse.getResponse();
        JSONArray jsonResp = (JSONArray) JSONValue.parse(json);

        AssertJUnit.assertTrue("check vlResource is in response",
                isComponentInArray(vlResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertTrue("check cpResource is in response",
                isComponentInArray(cpResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfcResource is not in response",
                isComponentInArray(vfcResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertTrue("check vfResource is not in response",
                isComponentInArray(vfResourceDetails.getUniqueId(), jsonResp));

        AssertJUnit.assertFalse("check vfcmtResource is not in response",
                isComponentInArray(vfcmtResourceDetails.getUniqueId(), jsonResp));

    }

    private ResourceReqDetails createResource(ResourceReqDetails resDetails, String name, ResourceTypeEnum resType) throws Exception {
        resDetails = new ResourceReqDetails(name, "desc",Arrays.asList(name), "Generic", Arrays.asList("tosca.nodes.Root"), "c", "1", "111", "myIcon", resType.name());
        resDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS.getCategory(),
                ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS.getSubCategory());
        RestResponse response = ResourceRestUtils.createResource(resDetails, designerDetails);
        assertTrue("response code is not 200, returned :" + response.getErrorCode(),
                response.getErrorCode() == 201);
        resDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(response));
        return resDetails;
    }

    private void checkInResource(ResourceReqDetails resDetails) throws IOException {
        RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resDetails, designerDetails, "0.1",
                LifeCycleStatesEnum.CHECKIN);
        AssertJUnit.assertEquals("check in operation failed", 200, checkInResponse.getErrorCode().intValue());

    }

    protected boolean isComponentInArray(String id, JSONArray component) {
        for (int i = 0; i < component.size(); i++) {
            JSONObject jobject = (JSONObject) component.get(i);
            if (jobject.get("uniqueId").toString().equals(id.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @AfterClass
    public void tearDown() throws IOException {
        ResourceRestUtils.deleteResource(vlResourceDetails.getUniqueId(), designerDetails.getUserId());
        ResourceRestUtils.deleteResource(cpResourceDetails.getUniqueId(), designerDetails.getUserId());
        ResourceRestUtils.deleteResource(vfcResourceDetails.getUniqueId(), designerDetails.getUserId());
        ResourceRestUtils.deleteResource(vfcmtResourceDetails.getUniqueId(), designerDetails.getUserId());
        ResourceRestUtils.deleteResource(vfResourceDetails.getUniqueId(), designerDetails.getUserId());
    }
}
