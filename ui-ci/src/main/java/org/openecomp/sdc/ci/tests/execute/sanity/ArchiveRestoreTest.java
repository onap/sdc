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

package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.GovernorOperationPage;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.OpsOperationPage;
import org.openecomp.sdc.ci.tests.utilities.CatalogUIUtilitis;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.HomeUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class ArchiveRestoreTest extends SetupCDTest {

    private User sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);


    @Test
    public void updateVSP_WhenVF_Archived() throws Throwable {

        String vnfFile1 = "1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip";
        String vnfFile2 = "2-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0_Added2TestParameters.zip";

//      1. Import VSP v1.0
        String filePath = org.openecomp.sdc.ci.tests.utilities.FileHandling.getUpdateVSPVnfRepositoryPath();
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", vendorLicenseModel
            .getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile1));
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile1, filePath, this.sdncDesignerDetails,
            vendorLicenseModel, null);
//		2. Create VF from VSP, certify - v1.0 is created
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//		3. Create Service add to it the certified VF and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
//		4. archive VF(1.0)
        GeneralPageElements.clickArchivedButtonFromCatalog(resource.getName());
//      5. service certification should send error message - unable to certify, service contains archived resource  via UI
        HomeUtils.findComponentAndClick(service.getName());
        GeneralPageElements.clickSubmitForTestingButtonErrorCase(service.getName());
//		6. Update VSP to v2.0 - onboard level
        getExtendTest().log(Status.INFO, "Upgrading the VSP with new file: " + vnfFile2);
        VendorSoftwareProductRestUtils.updateVendorSoftwareProductToNextVersion(vendorSoftwareProductObject, sdncDesignerDetails, filePath, vnfFile2);
        getExtendTest().log(Status.INFO, String.format("Validating VSP %s upgrade to version 2.0: ", vnfFile2));
        VendorSoftwareProductRestUtils.validateVspExist(vendorSoftwareProductObject, sdncDesignerDetails);
//		7. Update the VF from VSP when it archived  and restore - via UI
        getExtendTest().log(Status.INFO, String.format("Going to update VF %s with VSP v2.0", resourceReqDetails.getName()));
        CompositionPage.moveToHomeScreen();
        HomePage.showVspRepository();
        OnboardingUiUtils.updateVSP(vendorSoftwareProductObject, true);
//		8. Certify and update restored VF from with VSP v2.0
        GeneralPageElements.clickCertifyButton(resource.getName());
//		9. Update the Service with the VFi version 2.0 and certify
        HomeUtils.findComponentAndClick(service.getName());
        GeneralPageElements.clickSubmitForTestingButton(service.getName());
    }

    //    https://sdp.web.att.com/fa3qm1/web/console/Application_Development_Tools_QM_20.20.01#action=com.ibm.rqm.planning.home.actionDispatcher&subAction=viewTestScript&id=896098
    @Test
    public void certifyVF_WhenVSP_Archived() throws Exception {

        String vnfFile1 = "1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip";

//      1. Import VSP v1.0
        String filePath = org.openecomp.sdc.ci.tests.utilities.FileHandling.getUpdateVSPVnfRepositoryPath();
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", vendorLicenseModel
            .getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile1));
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile1, filePath, this.sdncDesignerDetails,
            vendorLicenseModel, null);
//		2. Create VF from VSP, certify - v1.0 is created
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        getExtendTest().log(Status.INFO, String.format("Certify the VF"));
//      3. Archive VSP (onboarding page-Amdocs side)
        getExtendTest().log(Status.INFO, String.format("Going to archive component OB side: %s", resource.getName()));
        VendorSoftwareProductRestUtils.archiveVendorSoftwareProduct(vendorSoftwareProductObject, sdncDesignerDetails);
        getExtendTest().log(Status.INFO, String.format("Succeed to archive component %s, OB side", resource.getName()));
//      4. chekout resource and check that VF is archived
        CatalogUIUtilitis.clickTopMenuButton(TopMenuButtonsEnum.CATALOG);
        HomeUtils.findComponentAndClick(resource.getName());
        GeneralPageElements.clickCheckoutButton();
        resource = AtomicOperationUtils.getResourceObject(resource.getUniqueId());
        getExtendTest().log(Status.INFO, String.format("Going to validate is VSP archived: %s", resource.getName()));
        assertTrue("Validate isVspArchived flag, expected: true, but was: " + resource.isVspArchived(), resource.isVspArchived().equals(true));
        String expectedText = "VSP is archived";
        getExtendTest().log(Status.INFO, String.format("Going to validate text box message, expected: %s", expectedText));
        String actualElementText = GeneralPageElements.getWebElementTextByTestId(DataTestIdEnum.ResourceMetadataEnum.SELECT_VSP.getValue());
        assertTrue("Validate text box message, expected: [" + expectedText + "], but was: " + actualElementText, actualElementText.equals(expectedText));
//      5. certify new resource - should fail, via API
        getExtendTest().log(Status.INFO, String.format("Going to certify archived on OB side resource %s , expected failure certification", resource.getName()));
        GeneralPageElements.clickCertifyButtonNoUpgradePopupDismissErrorCase(resource.getName());
//      6. restore
        getExtendTest().log(Status.INFO, String.format("Going to restore component OB side: %s", resource.getName()));
        VendorSoftwareProductRestUtils.restoreVendorSoftwareProduct(vendorSoftwareProductObject, sdncDesignerDetails);
        SetupCDTest.getExtendTest().log(Status.INFO, String.format("Succeed to restore component %s, OB side", resource.getName()));
//      7. certify - should pass
        getExtendTest().log(Status.INFO, String.format("Going to certify resource %s ", resource.getName()));
        GeneralPageElements.clickCertifyButtonNoUpgradePopupDismiss(resource.getName());
        resource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resource.getName(), "2.0");
        assertTrue("Validate isVspArchived flag, expected: false, but was: " + resource.isVspArchived(), resource.isVspArchived().equals(false));
    }

    @Test
    public void distributeServiceIncludedArchivedResource() throws Exception {

        String vnfFile1 = "1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip";
//      1. Import VSP v1.0
        String filePath = org.openecomp.sdc.ci.tests.utilities.FileHandling.getUpdateVSPVnfRepositoryPath();
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails);
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software License (VLM): %s v1.0", vendorLicenseModel
            .getVendorLicenseName()));
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
        getExtendTest().log(Status.INFO, String.format("Creating Vendor Software Product (VSP): %s v1.0 from heat file: %s ", resourceReqDetails.getName(), vnfFile1));
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createAndFillVendorSoftwareProduct(resourceReqDetails, vnfFile1, filePath, this.sdncDesignerDetails,
            vendorLicenseModel, null);
//		2. Create VF from VSP, certify - v1.0 is created
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        getExtendTest().log(Status.INFO, String.format("Creating Virtual Function (VF): %s v1.0", resourceReqDetails.getName()));
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        getExtendTest().log(Status.INFO, String.format("Certify the VF"));
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
//		3. Create Service add to it the certified VF and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        getExtendTest().log(Status.INFO, String.format("Adding VF instance to Service"));
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        reloginWithNewRole(UserRoleEnum.GOVERNOR);
        GeneralUIUtils.findComponentAndClick(service.getName());
        GovernorOperationPage.approveService(service.getName());
//		4. archive VF(1.0)
        reloginWithNewRole(UserRoleEnum.DESIGNER);
        GeneralPageElements.clickArchivedButtonFromCatalog(resource.getName());
//      5. Distribute service - should pass
        reloginWithNewRole(UserRoleEnum.OPS);
        GeneralUIUtils.findComponentAndClick(service.getName());
        OpsOperationPage.distributeService();

    }


    @Test
    public void certificationOfArchivedCR() throws Exception {

        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.CR, getUser());
        ResourceUIUtils.createCR(resourceReqDetails, sdncDesignerDetails);
        GeneralPageElements.clickCertifyButtonNoUpgradePopupDismiss(resourceReqDetails.getName());
        GeneralUIUtils.ultimateWait();
        Resource resource = (Resource) AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceReqDetails.getName(), "1.0");
//		1. Create Service add to it the certified CR and certify the Service v1.0
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService();
        org.openecomp.sdc.be.model.Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        getExtendTest().log(Status.INFO, String.format("Creating Service: %s v1.0", serviceReqDetails.getName()));
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstance = addComponentInstanceToComponentContainer.left().value();
        getExtendTest().log(Status.INFO, String.format("Adding CR instance to Service"));
//		2. archive CR(1.0)
        GeneralPageElements.clickArchivedButtonFromCatalog(resource.getName());
//      3. service certification should send error message - unable to certify, service contains archived resource  via UI
        HomeUtils.findComponentAndClick(service.getName());
        GeneralPageElements.clickSubmitForTestingButtonErrorCase(service.getName());
//      4. restore CR
        GeneralPageElements.restoreComponentFromArchivedCatalog(resource.getName());
//		5. Certify and update restored CR(v2.0)
        GeneralPageElements.clickCheckoutButton();
        GeneralPageElements.clickCertifyButton(resource.getName());
//		6. Update the Service with the CRi version 2.0 and certify service
        HomeUtils.findComponentAndClick(service.getName());
        GeneralPageElements.clickSubmitForTestingButton(service.getName());
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }
}
