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

package org.onap.sdc.backend.ci.tests.execute.lifecycle;


import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.utils.ArtifactUtils;
import org.onap.sdc.backend.ci.tests.utils.DbUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import static org.testng.AssertJUnit.*;

/**
 * @author al714h
 * <p>
 * resourceDetails - create, Add Heat, certify resourceDetails1 - create
 * resource, LCS - CheckOut serviceDetails - create, add RI from
 * resourceDetails serviceDetails2 - create, add RI from resourceDetails
 * serviceDetailsEmpty - create, LCS - CheckOut serviceDetailsEmpty2 -
 * create, LCS - CheckOut
 */
public abstract class LCSbaseTest extends ComponentBaseTest {

    protected ResourceReqDetails resourceDetails;
    protected ResourceReqDetails resourceDetails1;
    protected ServiceReqDetails serviceDetails;
    protected ServiceReqDetails serviceDetails2;
    protected ServiceReqDetails serviceDetailsEmpty;
    protected ServiceReqDetails serviceDetailsEmpty2;
    protected ComponentInstanceReqDetails componentInstanceReqDetails;
    protected ComponentInstanceReqDetails resourceInstanceReqDetails2;
    protected User sdncDesignerDetails1;
    protected User sdncDesignerDetails2;
    protected static User sdncTesterDeatails1;
    protected User sdncAdminDetails1;
    protected ArtifactReqDetails heatArtifactDetails;
    protected ArtifactReqDetails heatVolArtifactDetails;
    protected ArtifactReqDetails heatNetArtifactDetails;

    protected ArtifactReqDetails defaultArtifactDetails;

    protected ArtifactUtils artifactUtils;

    // protected static ServiceUtils serviceUtils = new ServiceUtils();

    @BeforeMethod
    public void before() throws Exception {

        initializeMembers();

        createComponents();

    }

    public void initializeMembers() throws IOException, Exception {
        resourceDetails = new ElementFactory().getDefaultResource();
        // resourceDetails =
        // new ElementFactory().getDefaultResource("myNewResource1234567890",
        // NormativeTypesEnum.ROOT, ResourceServiceCategoriesEnum.ROUTERS,
        // UserRoleEnum.DESIGNER.getUserId());
        resourceDetails1 = new ElementFactory().getDefaultResource("secondResource", NormativeTypesEnum.ROOT);
        serviceDetails = new ElementFactory().getDefaultService();
		serviceDetails2 = new ElementFactory().getDefaultService("newTestService2", ServiceCategoriesEnum.MOBILITY, "al1976", ServiceInstantiationType.A_LA_CARTE.getValue());
        serviceDetailsEmpty = new ElementFactory().getDefaultService("newEmptyService", ServiceCategoriesEnum.MOBILITY,
				"al1976", ServiceInstantiationType.A_LA_CARTE.getValue());
        serviceDetailsEmpty2 = new ElementFactory().getDefaultService("newEmptyService2", ServiceCategoriesEnum.MOBILITY,
				"al1976", ServiceInstantiationType.A_LA_CARTE.getValue());
        sdncDesignerDetails1 = new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER);
        sdncDesignerDetails2 = new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER2);
        sdncTesterDeatails1 = new ElementFactory().getDefaultUser(UserRoleEnum.TESTER);
        sdncAdminDetails1 = new ElementFactory().getDefaultUser(UserRoleEnum.ADMIN);
        heatArtifactDetails = new ElementFactory().getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
        heatNetArtifactDetails = new ElementFactory()
                .getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType());
        heatVolArtifactDetails = new ElementFactory()
                .getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_VOL.getType());
        componentInstanceReqDetails = new ElementFactory().getDefaultComponentInstance();
        resourceInstanceReqDetails2 = new ElementFactory().getDefaultComponentInstance();

    }

    protected void createComponents() throws Exception {

        RestResponse response = new ResourceRestUtils().createResource(resourceDetails1, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("resource uniqueId is null:", resourceDetails1.getUniqueId());

        response = new ResourceRestUtils().createResource(resourceDetails, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("resource uniqueId is null:", resourceDetails.getUniqueId());

        response = new ServiceRestUtils().createService(serviceDetails, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

        ArtifactReqDetails heatArtifactDetails = new ElementFactory()
                .getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
        response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1,
                resourceDetails.getUniqueId());
        assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
                response.getErrorCode() == 200);

        // certified resource
        response = LCSbaseTest.certifyResource(resourceDetails, sdncDesignerDetails1);
        assertTrue("certify resource request returned status:" + response.getErrorCode(),
                response.getErrorCode() == 200);

        // add resource instance with HEAT deployment artifact to the service
        componentInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
        response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails, sdncDesignerDetails1,
                serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
        assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);

        response = new ServiceRestUtils().createService(serviceDetails2, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("service uniqueId is null:", serviceDetails2.getUniqueId());

        componentInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
        response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails, sdncDesignerDetails1,
                serviceDetails2.getUniqueId(), ComponentTypeEnum.SERVICE);
        assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);

        response = new ServiceRestUtils().createService(serviceDetailsEmpty, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("service uniqueId is null:", serviceDetailsEmpty.getUniqueId());

        response = new ServiceRestUtils().createService(serviceDetailsEmpty2, sdncDesignerDetails1);
        assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
        assertNotNull("service uniqueId is null:", serviceDetailsEmpty2.getUniqueId());

        DbUtils.cleanAllAudits();

    }

    public static RestResponse certifyResource(ResourceReqDetails resourceDetails, User user) throws Exception {
/*		RestResponse restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails, user.getUserId(),
                LifeCycleStatesEnum.CHECKIN);
		// if (restResponseResource.getErrorCode() == 200){
		restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails, user.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		// }else
		// return restResponseResource;
		sdncTesterDeatails1 = new ElementFactory().getDefaultUser(UserRoleEnum.TESTER);
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails,
					sdncTesterDeatails1.getUserId(), LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponseResource;
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails,
					sdncTesterDeatails1.getUserId(), LifeCycleStatesEnum.CERTIFY);
		}
		return restResponseResource;*/
        return new LifecycleRestUtils().changeResourceState(resourceDetails,
                sdncTesterDeatails1.getUserId(), LifeCycleStatesEnum.CERTIFY);
    }

    public static RestResponse certifyService(ServiceReqDetails serviceDetails, User user) throws Exception {
        RestResponse restResponseService = new LifecycleRestUtils().changeServiceState(serviceDetails, user,
                LifeCycleStatesEnum.CHECKIN);
        // if (restResponseService.getErrorCode() == 200){
        restResponseService = new LifecycleRestUtils().changeServiceState(serviceDetails, user,
                LifeCycleStatesEnum.CERTIFICATIONREQUEST);
        // }else
        // return restResponseService;

        sdncTesterDeatails1 = new ElementFactory().getDefaultUser(UserRoleEnum.TESTER);
        if (restResponseService.getErrorCode() == 200) {
            restResponseService = new LifecycleRestUtils().changeServiceState(serviceDetails, sdncTesterDeatails1,
                    LifeCycleStatesEnum.STARTCERTIFICATION);
        } else
            return restResponseService;
        if (restResponseService.getErrorCode() == 200) {
            restResponseService = new LifecycleRestUtils().changeServiceState(serviceDetails, sdncTesterDeatails1,
                    LifeCycleStatesEnum.CERTIFY);
        }
        return restResponseService;
    }

    protected static RestResponse raiseResourceToTargetVersion(ResourceReqDetails resourceDetails, String targetVersion,
                                                               User user) throws Exception {
        return raiseResourceToTargetVersion(resourceDetails, targetVersion, null, user);
    }

    protected static RestResponse raiseResourceToTargetVersion(ResourceReqDetails resourceDetails, String targetVersion,
                                                               RestResponse prevResponse, User user) throws Exception {

        String[] splitParts = targetVersion.split("\\.");

        int version = Integer.parseInt(splitParts[1]);
        String checkinComment = "good checkin";
        String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";

        if (prevResponse != null) {
            Resource resourceRespJavaObject = ResponseParser
                    .convertResourceResponseToJavaObject(prevResponse.getResponse());
            if (resourceRespJavaObject.getLifecycleState().equals(LifecycleStateEnum.CERTIFIED)) {
                RestResponse restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails,
                        user.getUserId(), LifeCycleStatesEnum.CHECKOUT);
            }
        }

        RestResponse restResponseResource = null;
        for (int i = 0; i < (version - 1); i++) {

            restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails, user, null,
                    LifeCycleStatesEnum.CHECKIN, checkinComentJson);
            if (restResponseResource.getErrorCode() == 200) {
                restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails, user.getUserId(),
                        LifeCycleStatesEnum.CHECKOUT);
                if (restResponseResource.getErrorCode() == 200) {

                } else
                    break;

            } else
                break;

        }

        restResponseResource = new LifecycleRestUtils().changeResourceState(resourceDetails, user, null,
                LifeCycleStatesEnum.CHECKIN, checkinComentJson);
        assertEquals("Check response code ", 200, restResponseResource.getErrorCode().intValue());
        return restResponseResource;
    }

}
