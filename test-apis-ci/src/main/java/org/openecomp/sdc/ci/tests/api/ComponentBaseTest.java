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

package org.openecomp.sdc.ci.tests.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.rest.*;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public abstract class ComponentBaseTest {

    protected static Logger logger = Logger.getLogger(ComponentBaseTest.class);

    protected static final String REPORT_FOLDER = "." + File.separator + "ExtentReport" + File.separator;
    private static final String VERSIONS_INFO_FILE_NAME = "versions.info";
    private static final String REPORT_FILE_NAME = "SDC_CI_Extent_Report.html";
    protected static TitanGraph titanGraph;
    public static Config config;
    protected static ITestContext myContext;


    /**************** METHODS ****************/
    public static ExtentTest getExtendTest() {
        SomeInterface testManager = new ExtentTestManager();
        return testManager.getTest();
    }

    public static enum ComponentOperationEnum {
        CREATE_COMPONENT, UPDATE_COMPONENT, GET_COMPONENT, DELETE_COMPONENT, CHANGE_STATE_CHECKIN, CHANGE_STATE_CHECKOUT, CHANGE_STATE_UNDO_CHECKOUT
    }

    public ComponentBaseTest(TestName name, String name2) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("com.thinkaurelius").setLevel(Level.INFO);
        lc.getLogger("com.datastax").setLevel(Level.INFO);
        lc.getLogger("io.netty").setLevel(Level.INFO);
        lc.getLogger("c.d").setLevel(Level.INFO);
    }

    public static String getReportFolder() {
        return REPORT_FOLDER;
    }

    @BeforeSuite(alwaysRun = true)
    public void setupBeforeSuite(ITestContext context) throws Exception {
        config = Utils.getConfig();
        myContext = context;
        ExtentManager.initReporter(getReportFolder(), REPORT_FILE_NAME, context);
        AtomicOperationUtils.createDefaultConsumer(true);
        performClean();
    }

    @BeforeMethod(alwaysRun = true)
    public void setupBeforeTest(java.lang.reflect.Method method, ITestContext context) throws Exception {

        System.out.println(" method.getName() " + method.getName());
        if (!method.getName().equals("onboardVNFShotFlow") &&
            !method.getName().equals("onboardPNFFlow") ) {
            logger.info("ExtentReport instance started from BeforeMethod...");
            ExtentTestManager.startTest(method.getName());
            ExtentTestManager.assignCategory(this.getClass());

        } else {
            logger.debug("ExtentReport instance started from Test...");
        }

    }

    @AfterMethod(alwaysRun = true)
    public void quitAfterTest(ITestResult result, ITestContext context) throws Exception {

        String testName = result.getName();
        Throwable throwable = result.getThrowable();
        int status = result.getStatus();

        switch (status) {
            case ITestResult.SUCCESS:
                getExtendTest().log(Status.PASS, "Test Result : <span class='label success'>Success</span>");
                break;

            case ITestResult.FAILURE:
                getExtendTest().log(Status.ERROR, "ERROR - The following exepction occured");
                getExtendTest().log(Status.ERROR, result.getThrowable());
                getExtendTest().log(Status.FAIL, "<span class='label failure'>Failure</span>");
                break;

            case ITestResult.SKIP:
                getExtendTest().log(Status.SKIP, "SKIP - The following exepction occured");
                break;
            default:
                break;
        }


        ExtentTestManager.endTest();


    }

    @AfterSuite(alwaysRun = true)
    public static void shutdownTitan() throws Exception {
        performClean();
    }

    public void setLog(String fromDataProvider) {

        String suiteName = ExtentManager.getSuiteName(myContext);
        ExtentTestManager.startTest(Thread.currentThread().getStackTrace()[2].getMethodName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fromDataProvider);
        ExtentTestManager.assignCategory(this.getClass());

    }

    protected static void performClean() throws Exception, FileNotFoundException {
        if (!config.getSystemUnderDebug()) {
            deleteCreatedComponents(getCatalogAsMap());
            FileHandling.overWriteExistindDir("outputCsar");
        } else {
            System.out.println("Accordindig to configuration components will not be deleted, in case to unable option to delete, please change systemUnderDebug parameter value to false ...");
        }
    }

    public void verifyErrorCode(RestResponse response, String action, int expectedCode) {
        assertNotNull("check response object is not null after " + action, response);
        assertNotNull("check error code exists in response after " + action, response.getErrorCode());
        assertEquals("Check response code after  + action" + action, expectedCode, response.getErrorCode().intValue());
    }

    private static void deleteCreatedComponents(Map<String, List<Component>> convertCatalogResponseToJavaObject) throws IOException {
        final String userId = UserRoleEnum.DESIGNER.getUserId();

    /*    List<Component> resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.PRODUCT_PARAM_NAME);
        if (resourcesArrayList.size() > 0) {
            List<String> collect = buildCollectionUniqueId(resourcesArrayList);
            for (String uId : collect) {
                ProductRestUtils.deleteProduct(uId, userId);
            }
        }*/

        List<Component> resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.RESOURCE_PARAM_NAME);
        if (!CollectionUtils.isEmpty(resourcesArrayList)) {
            List<String> collect = buildCollectionUniqueId(resourcesArrayList);
            for (String uId : collect) {
                ResourceRestUtils.markResourceToDelete(uId, userId);
            }
            ResourceRestUtils.deleteMarkedResources(userId);
        }

        resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.SERVICE_PARAM_NAME);
        if (resourcesArrayList.size() > 0) {
            List<String> collect = buildCollectionUniqueId(resourcesArrayList);
            for (String uId : collect) {
                ServiceRestUtils.markServiceToDelete(uId, userId);
            }
            ServiceRestUtils.deleteMarkedServices(userId);
        }
    }

    protected static List<String> buildCollectionUniqueId(List<Component> resourcesArrayList) {


        List<String> genericCollection = new ArrayList<>();
        if(resourcesArrayList.get(0) != null) {
            ComponentTypeEnum componentTypeEnum = resourcesArrayList.get(0).getComponentType();
            resourcesArrayList.stream().filter(a -> a != null).
                    filter(s -> s.getName().toLowerCase().startsWith("ci") && !s.getName().toLowerCase().equals("cindervolume")).
                    filter(f -> f.getUniqueId() != null).
                    map(e -> e.getUniqueId()).
                    collect(Collectors.toList()).
                    forEach((i) -> {
                        buildCollectionBaseOnComponentType(componentTypeEnum, genericCollection, i);
                    });
        }
        return genericCollection;
    }

    public static void buildCollectionBaseOnComponentType(ComponentTypeEnum componentTypeEnum,
                                                          List<String> genericCollection, String i) {
        try {
            switch (componentTypeEnum) {
                case RESOURCE:
                    RestResponse resource = ResourceRestUtils.getResource(i);
                    Resource convertResourceResponseToJavaObject = ResponseParser.convertResourceResponseToJavaObject(resource.getResponse());
                    Map<String, String> allVersions = convertResourceResponseToJavaObject.getAllVersions();
                    Collection<String> values = allVersions.values();
                    genericCollection.addAll(values);

                    break;
                case SERVICE:
                    RestResponse service = ServiceRestUtils.getService(i);
                    Service convertServiceResponseToJavaObject = ResponseParser.convertServiceResponseToJavaObject(service.getResponse());
                    allVersions = convertServiceResponseToJavaObject.getAllVersions();
                    values = allVersions.values();
                    genericCollection.addAll(values);

                    break;


                case PRODUCT:
                    RestResponse product = ProductRestUtils.getProduct(i);
                    Product convertProductResponseToJavaObject = ResponseParser.convertProductResponseToJavaObject(product.getResponse());
                    allVersions = convertProductResponseToJavaObject.getAllVersions();
                    values = allVersions.values();
                    genericCollection.addAll(values);

                    break;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    protected static Map<String, List<Component>> getCatalogAsMap() throws Exception {
        RestResponse catalog = CatalogRestUtils.getCatalog(UserRoleEnum.DESIGNER.getUserId());
        Map<String, List<Component>> convertCatalogResponseToJavaObject = ResponseParser.convertCatalogResponseToJavaObject(catalog.getResponse());
        return convertCatalogResponseToJavaObject;
    }

    protected Resource createVfFromCSAR(User sdncModifierDetails, String csarId) throws Exception {
        // create new resource from Csar
        ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();

        resourceDetails.setCsarUUID(csarId);
        resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
        RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
        BaseRestUtils.checkCreateResponse(createResource);
        Resource createdResource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());
        return createdResource;
    }


}
