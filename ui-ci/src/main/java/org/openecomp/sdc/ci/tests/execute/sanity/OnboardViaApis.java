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

package org.openecomp.sdc.ci.tests.execute.sanity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import fj.data.Either;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.dataProvider.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.testng.AssertJUnit.assertTrue;


public class OnboardViaApis {


    private static final String FULL_PATH = "C://tmp//CSARs//";
    protected static String filepath = FileHandling.getVnfRepositoryPath();

    //-------------------------------------------------------------------------------------------------------
    User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
    //	ResourceReqDetails resourceDetails;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    @BeforeMethod
    public void before() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger("org.apache").setLevel(Level.OFF);
        lc.getLogger("org.*").setLevel(Level.OFF);
        lc.getLogger("org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest").setLevel(Level.OFF);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
    public void onboardVNFTestViaApis(String filepath, String vnfFile) throws Exception, Throwable {
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService(); //getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        Service service = runOnboardViaApisOnly(serviceReqDetails, resourceReqDetails, filepath, vnfFile);
    }


    @Test
    public void onboardingAndParser() throws Exception {
        final int fileFromFolderToGet  =7;
        Service service = null;
        List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
        String vnfFile = fileNamesFromFolder.get(fileFromFolderToGet);
        System.err.println(timestamp + " Starting test with VNF: " + vnfFile);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService(); //getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        service = runOnboardViaApisOnly(serviceReqDetails, resourceReqDetails, filepath, vnfFile);
    }

    @Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "randomVNF_List")
    public void updateVSPFullScenario(String filepath, String vnfFile) throws Exception {
        //CREATE DATA REQUIRED FOR TEST
        boolean skipReport = true;
        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(sdncDesignerDetails1);
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource(); //getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject vendorSoftwareProductObject = VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, sdncDesignerDetails1,
            vendorLicenseModel);
        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        ServiceReqDetails serviceReqDetails = ElementFactory.getDefaultService(); //getServiceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();
        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();
        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        // TEST START

        VendorLicenseModelRestUtils.updateVendorLicense(vendorLicenseModel, sdncDesignerDetails1, false);
        VendorLicenseModelRestUtils.validateVlmExist(vendorLicenseModel.getVendorId(), vendorLicenseModel.getVersion(), sdncDesignerDetails1);

        // Update the VSP With the VLM new version and submit the VSP
        vendorSoftwareProductObject = VendorSoftwareProductRestUtils.updateVSPWithNewVLMParameters(vendorSoftwareProductObject,
            vendorLicenseModel, sdncDesignerDetails1);
        VendorSoftwareProductRestUtils.validateVspExist(vendorSoftwareProductObject, sdncDesignerDetails1);
        Boolean distributeAndValidateService = AtomicOperationUtils.distributeAndValidateService(service);
        assertTrue("Distribution status is " + distributeAndValidateService, distributeAndValidateService);
        System.out.println(distributeAndValidateService);
    }

    public Service runOnboardViaApisOnly(ServiceReqDetails serviceReqDetails, ResourceReqDetails resourceReqDetails, String filepath, String vnfFile) throws Exception {

        VendorSoftwareProductObject vendorSoftwareProductObject = OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, filepath, vnfFile, sdncDesignerDetails1);
        vendorSoftwareProductObject.setName(vendorSoftwareProductObject.getName());

        resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
        Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails);
        resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

        Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER, true).left().value();

        Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER, true);
        ComponentInstance componentInstanceDefinition = addComponentInstanceToComponentContainer.left().value();

        service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
        return service;
    }

}
