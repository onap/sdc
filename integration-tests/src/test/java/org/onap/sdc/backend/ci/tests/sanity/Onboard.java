/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Nokia. All rights reserved.
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


package org.onap.sdc.backend.ci.tests.sanity;

import static org.testng.Assert.assertThrows;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.data.providers.OnboardingDataProviders;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.api.ExtentTestActions;
import org.testng.annotations.*;

public class Onboard extends ComponentBaseTest {

    private String makeDistributionValue;
    private String makeToscaValidationValue;

    @Rule
    public static final TestName name = new TestName();

    @Parameters({"makeDistribution"})
    @BeforeMethod
    public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
        System.out.println("[beforeTestReadParams] Enter method");
        System.out.println("[beforeTestReadParams] Param makeDistributionReadValue = " + makeDistributionReadValue);

        makeDistributionValue = makeDistributionReadValue;
        System.out.println("[beforeTestReadParams] Set makeDistributionValue = " + makeDistributionValue);

        logger.info("makeDistributionReadValue -> " + makeDistributionValue);
        System.out.println("[beforeTestReadParams] Logged makeDistributionValue");

        System.out.println("[beforeTestReadParams] Exit method");
    }

    @Parameters({"makeToscaValidation"})
    @BeforeClass
    public void makeToscaValidation(@Optional("false") String makeToscaValidationReadValue) {
        System.out.println("[makeToscaValidation] Enter method");
        System.out.println("[makeToscaValidation] Param makeToscaValidationReadValue = " + makeToscaValidationReadValue);

        makeToscaValidationValue = makeToscaValidationReadValue;
        System.out.println("[makeToscaValidation] Set makeToscaValidationValue = " + makeToscaValidationValue);

        logger.info("makeToscaValidationReadValue -> " + makeToscaValidationValue);
        System.out.println("[makeToscaValidation] Logged makeToscaValidationValue");

        System.out.println("[makeToscaValidation] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "VNF_List")
    public void onboardVNFShotFlow(String filePath, String vnfFile) throws Exception {
        System.out.println("[onboardVNFShotFlow] Enter method");
        System.out.println("[onboardVNFShotFlow] Inputs -> filePath: " + filePath + ", vnfFile: " + vnfFile);

        setLog(vnfFile);
        System.out.println("[onboardVNFShotFlow] Called setLog with vnfFile");

        runOnboardToDistributionFlow(filePath, vnfFile, ResourceTypeEnum.VF);
        System.out.println("[onboardVNFShotFlow] Called runOnboardToDistributionFlow with ResourceTypeEnum.VF");

        System.out.println("[onboardVNFShotFlow] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "PNF_List")
    public void onboardPNFFlow(String filePath, String pnfFile) throws Exception {
        System.out.println("[onboardPNFFlow] Enter method");
        System.out.println("[onboardPNFFlow] Inputs -> filePath: " + filePath + ", pnfFile: " + pnfFile);

        setLog(pnfFile);
        System.out.println("[onboardPNFFlow] Called setLog with pnfFile");

        runOnboardToDistributionFlow(filePath, pnfFile, ResourceTypeEnum.PNF);
        System.out.println("[onboardPNFFlow] Called runOnboardToDistributionFlow with ResourceTypeEnum.PNF");

        System.out.println("[onboardPNFFlow] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "ASD_List")
    public void onboardASDFlow(String filePath, String asdFile) throws Exception {
        System.out.println("[onboardASDFlow] Enter method");
        System.out.println("[onboardASDFlow] Inputs -> filePath: " + filePath + ", asdFile: " + asdFile);

        setLog(asdFile);
        System.out.println("[onboardASDFlow] Called setLog with asdFile");

        runOnboardToDistributionFlow(filePath, asdFile, ResourceTypeEnum.VF);
        System.out.println("[onboardASDFlow] Called runOnboardToDistributionFlow with ResourceTypeEnum.VF");

        System.out.println("[onboardASDFlow] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "CNF_List")
    public void onboardCNFFlow(String filePath, String cnfFile) throws Exception {
        System.out.println("[onboardCNFFlow] Enter method");
        System.out.println("[onboardCNFFlow] Inputs -> filePath: " + filePath + ", cnfFile: " + cnfFile);

        setLog(cnfFile);
        System.out.println("[onboardCNFFlow] Called setLog with cnfFile");

        runOnboardToDistributionFlow(filePath, cnfFile, ResourceTypeEnum.VF);
        System.out.println("[onboardCNFFlow] Called runOnboardToDistributionFlow with ResourceTypeEnum.VF");

        System.out.println("[onboardCNFFlow] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Invalid_CNF_List")
    public void onboardCNFFlowShouldFailForInvalidHelmPackage(String filePath, String cnfFile) {
        System.out.println("[onboardCNFFlowShouldFailForInvalidHelmPackage] Enter method");
        System.out.println("[onboardCNFFlowShouldFailForInvalidHelmPackage] Inputs -> filePath: " + filePath + ", cnfFile: " + cnfFile);

        setLog(cnfFile);
        System.out.println("[onboardCNFFlowShouldFailForInvalidHelmPackage] Called setLog with cnfFile");

        assertThrows(() -> runOnboardToDistributionFlow(filePath, cnfFile, ResourceTypeEnum.VF));
        System.out.println("[onboardCNFFlowShouldFailForInvalidHelmPackage] Asserted exception for invalid Helm package");

        System.out.println("[onboardCNFFlowShouldFailForInvalidHelmPackage] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "CNF_Helm_Validator_List")
    public void onboardCNFWithHelmValidatorFlow(String filePath, String cnfFile) throws Exception {
        System.out.println("[onboardCNFWithHelmValidatorFlow] Enter method");
        System.out.println("[onboardCNFWithHelmValidatorFlow] Inputs -> filePath: " + filePath + ", cnfFile: " + cnfFile);

        setLog(cnfFile);
        System.out.println("[onboardCNFWithHelmValidatorFlow] Called setLog with cnfFile");

        runOnboardToDistributionFlow(filePath, cnfFile, ResourceTypeEnum.VF);
        System.out.println("[onboardCNFWithHelmValidatorFlow] Called runOnboardToDistributionFlow with ResourceTypeEnum.VF");

        System.out.println("[onboardCNFWithHelmValidatorFlow] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "CNF_With_Warning_Helm_Validator_List")
    public void onboardCNFWithHelmValidatorFlowWithWarning(String filePath, String cnfFile) throws Exception {
        System.out.println("[onboardCNFWithHelmValidatorFlowWithWarning] Enter method");
        System.out.println("[onboardCNFWithHelmValidatorFlowWithWarning] Inputs -> filePath: " + filePath + ", cnfFile: " + cnfFile);

        setLog(cnfFile);
        System.out.println("[onboardCNFWithHelmValidatorFlowWithWarning] Called setLog with cnfFile");

        runOnboardToDistributionFlow(filePath, cnfFile, ResourceTypeEnum.VF);
        System.out.println("[onboardCNFWithHelmValidatorFlowWithWarning] Called runOnboardToDistributionFlow with ResourceTypeEnum.VF");

        System.out.println("[onboardCNFWithHelmValidatorFlowWithWarning] Exit method");
    }

    @Test(dataProviderClass = OnboardingDataProviders.class, dataProvider = "Invalid_CNF_Helm_Validator_List")
    public void onboardCNFWithHelmValidatorShouldFailTest(String filePath, String cnfFile) {
        System.out.println("[onboardCNFWithHelmValidatorShouldFailTest] Enter method");
        System.out.println("[onboardCNFWithHelmValidatorShouldFailTest] Inputs -> filePath: " + filePath + ", cnfFile: " + cnfFile);

        setLog(cnfFile);
        System.out.println("[onboardCNFWithHelmValidatorShouldFailTest] Called setLog with cnfFile");

        assertThrows(() -> runOnboardToDistributionFlow(filePath, cnfFile, ResourceTypeEnum.VF));
        System.out.println("[onboardCNFWithHelmValidatorShouldFailTest] Asserted exception for invalid Helm validation");

        System.out.println("[onboardCNFWithHelmValidatorShouldFailTest] Exit method");
    }

    private void runOnboardToDistributionFlow(String packageFilePath, String packageFileName, ResourceTypeEnum resourceTypeEnum) throws Exception {
        System.out.println("[runOnboardToDistributionFlow] Enter method");
        System.out.println("[runOnboardToDistributionFlow] Inputs -> packageFilePath: " + packageFilePath +
                           ", packageFileName: " + packageFileName +
                           ", resourceTypeEnum: " + resourceTypeEnum);

        ExtentTestActions.log(Status.INFO, String.format("Going to onboard the %s %s", resourceTypeEnum.getValue(), packageFileName));
        System.out.println("[runOnboardToDistributionFlow] Logged start of onboarding");

        User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        System.out.println("[runOnboardToDistributionFlow] Created default user (DESIGNER): " + (user != null ? user.getUserId() : "null"));

        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        System.out.println("[runOnboardToDistributionFlow] Created default ResourceReqDetails: " + resourceReqDetails);

        resourceReqDetails.setResourceType(resourceTypeEnum.getValue());
        System.out.println("[runOnboardToDistributionFlow] Set resource type: " + resourceReqDetails.getResourceType());

        VendorSoftwareProductObject vendorSoftwareProductObject =
            OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, packageFilePath, packageFileName, user);
        System.out.println("[runOnboardToDistributionFlow] Created VSP via APIs: " + vendorSoftwareProductObject);

        // prepare details before resource create
        OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        System.out.println("[runOnboardToDistributionFlow] Prepared resource details before create: " + resourceReqDetails);

        resourceReqDetails.setResourceType(resourceTypeEnum.getValue());
        System.out.println("[runOnboardToDistributionFlow] Re-set resource type (safety): " + resourceReqDetails.getResourceType());

        ExtentTestActions.log(Status.INFO, String.format("Create %s %s From VSP", resourceTypeEnum.getValue(), resourceReqDetails.getName()));
        System.out.println("[runOnboardToDistributionFlow] Logged create resource from VSP");

        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails, UserRoleEnum.DESIGNER);
        System.out.println("[runOnboardToDistributionFlow] Created resource from VSP: " + (resource != null ? resource.getName() : "null"));

        ExtentTestActions.log(Status.INFO, String.format("Certify %s", resourceTypeEnum.getValue()));
        System.out.println("[runOnboardToDistributionFlow] Logged certify resource");

        resource = (Resource) AtomicOperationUtils
                .changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
                .getLeft();
        System.out.println("[runOnboardToDistributionFlow] Resource certified: " + (resource != null ? resource.getName() : "null"));

        // -------------------------- SERVICE --------------------------------
        ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(user);
        System.out.println("[runOnboardToDistributionFlow] Prepared ServiceReqDetails: " + serviceReqDetails);

        ExtentTestActions.log(Status.INFO, String.format("Create Service %s", serviceReqDetails.getName()));
        System.out.println("[runOnboardToDistributionFlow] Logged create service");

        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        System.out.println("[runOnboardToDistributionFlow] Created service: " + (service != null ? service.getName() : "null"));

        ExtentTestActions.log(Status.INFO, String.format("Add %s to Service", resourceTypeEnum.getValue()));
        System.out.println("[runOnboardToDistributionFlow] Logged add component instance to service");

        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer =
                AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        System.out.println("[runOnboardToDistributionFlow] addComponentInstanceToComponentContainer returned: " + addComponentInstanceToComponentContainer);

        addComponentInstanceToComponentContainer.left().value();
        System.out.println("[runOnboardToDistributionFlow] Added component instance to service");

        ExtentTestActions.log(Status.INFO, "Certify Service");
        System.out.println("[runOnboardToDistributionFlow] Logged certify service");

        service = (Service) AtomicOperationUtils
                .changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
                .getLeft();
        System.out.println("[runOnboardToDistributionFlow] Service certified: " + (service != null ? service.getName() : "null"));

        if (makeDistributionValue.equals("true")) {
            System.out.println("[runOnboardToDistributionFlow] makeDistributionValue == true, distributing service");
            ExtentTestActions.log(Status.INFO, "Distribute Service");
            AtomicOperationUtils.distributeService(service, true);
            System.out.println("[runOnboardToDistributionFlow] Service distribution triggered");
        } else {
            System.out.println("[runOnboardToDistributionFlow] makeDistributionValue != true, distribution skipped");
        }

        if (makeToscaValidationValue.equals("true")) {
            System.out.println("[runOnboardToDistributionFlow] makeToscaValidationValue == true, starting TOSCA validation");
            ExtentTestActions.log(Status.INFO, "Start tosca validation");
            AtomicOperationUtils.toscaValidation(service, packageFileName);
            System.out.println("[runOnboardToDistributionFlow] TOSCA validation executed");
        } else {
            System.out.println("[runOnboardToDistributionFlow] makeToscaValidationValue != true, TOSCA validation skipped");
        }

        ExtentTestActions.log(Status.INFO, String.format("The onboarding process for '%s' finished with success", packageFileName));
        System.out.println("[runOnboardToDistributionFlow] Logged success message");
        System.out.println("[runOnboardToDistributionFlow] Exit method");
    }
}
