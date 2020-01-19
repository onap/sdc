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

package org.openecomp.sdc.be.components.lifecycle;

import fj.data.Either;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CertificationChangeTransitionTests extends LifecycleTestBase {

    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    private CertificationChangeTransition changeTransition;

    Resource resource, resourceAfterCertify;
    Service service, serviceAfterCertify;
    User owner;
    private static String RES_ID = "resId";
    private static String RES_ID_CERTIFIED = "resIdCert";
    private static String SERVICE_ID = "serviceId";
    private static String SERVICE_ID_CERTIFIED = "serviceIdCert";




    @Before
    public void setup() {
        super.setup();
        changeTransition = new CertificationChangeTransition(serviceBusinessLogic, LifeCycleTransitionEnum.CERTIFY, componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade, janusGraphDao);
        changeTransition.setConfigurationManager(configurationManager);
        resource = createResourceObject(RES_ID);
        resourceAfterCertify = createResourceObject(RES_ID_CERTIFIED);
        resourceAfterCertify.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        service = createServiceObject(SERVICE_ID);
        serviceAfterCertify = createServiceObject(SERVICE_ID_CERTIFIED);
        User user = new User();
        user.setUserId("cs0008");
        user.setFirstName("Carlos");
        user.setLastName("Santana");
        user.setRole(Role.DESIGNER.name());
        Either<User, ResponseFormat> ownerResponse = changeTransition.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        owner = ownerResponse.left().value();


    }

    @Test
    public void testVFCMTStateValidation(){
        Either<? extends Component, ResponseFormat> changeStateResult;
        resource = createResourceVFCMTObject();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(toscaElementLifecycleOperation.certifyToscaElement(resource.getUniqueId(), user.getUserId(), owner.getUserId()))
                .thenReturn(Either.left(ModelConverter.convertToToscaElement(resource)));

        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testCheckoutStateValidation() {
        Either<? extends Component, ResponseFormat> changeStateResult;

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(toscaElementLifecycleOperation.certifyToscaElement(RES_ID, user.getUserId(), owner.getUserId()))
                .thenReturn(Either.left(ModelConverter.convertToToscaElement(resourceAfterCertify)));

        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testPnfValidation() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        resource.setResourceType(ResourceTypeEnum.PNF);
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(toscaElementLifecycleOperation.certifyToscaElement(RES_ID, user.getUserId(), owner.getUserId()))
                .thenReturn(Either.left(ModelConverter.convertToToscaElement(resourceAfterCertify)));

        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testCRValidation() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        resource.setResourceType(ResourceTypeEnum.CR);
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(toscaElementLifecycleOperation.certifyToscaElement(RES_ID, user.getUserId(), owner.getUserId()))
                .thenReturn(Either.left(ModelConverter.convertToToscaElement(resourceAfterCertify)));

        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        changeStateResult = changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testVSPIsArchivedValidation(){
        Resource resource = createResourceObject();
        resource.setVspArchived(true);

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = changeTransition.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        User user = new User();
        user.setUserId("cs0008");
        user.setFirstName("Carlos");
        user.setLastName("Santana");
        user.setRole(Role.DESIGNER.name());
        try {
            changeTransition.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        } catch (ComponentException exp) {
            assertResponse(Either.right(exp.getResponseFormat()), ActionStatus.ARCHIVED_ORIGINS_FOUND, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
            return;
        }
        fail();
    }


    @Test
    public void testValidateAllResourceInstanceCertified_SuccessWithoutRI() {
        Resource resource = new Resource();
        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = changeTransition.validateAllResourceInstanceCertified(resource);
        assertTrue(validateAllResourceInstanceCertified.isLeft());
    }

    @Test
    public void testValidateAllResourceInstanceCertified_SuccessWithCertifiedResources() {
        Resource resource = new Resource();
        List<ComponentInstance> riList = new ArrayList<>();
        ComponentInstance ri = new ComponentInstance();
        ri.setComponentVersion("2.0");
        riList.add(ri);
        resource.setComponentInstances(riList);

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = changeTransition.validateAllResourceInstanceCertified(resource);
        assertTrue(validateAllResourceInstanceCertified.isLeft());
    }

    @Test
    public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMinorVersion() {
        Resource resource = createVFWithRI("0.3");

        simulateCertifiedVersionExistForRI();

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = changeTransition.validateAllResourceInstanceCertified(resource);

        assertTrue(validateAllResourceInstanceCertified.isRight());
        ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
        assertEquals((int) responseFormat.getStatus(), HttpStatus.SC_FORBIDDEN);
        assertEquals("SVC4559", responseFormat.getMessageId());

    }

    @Test
    public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMajorVersion() {
        Resource resource = createVFWithRI("1.3");

        simulateCertifiedVersionExistForRI();

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = changeTransition.validateAllResourceInstanceCertified(resource);

        assertTrue(validateAllResourceInstanceCertified.isRight());
        ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
        assertEquals((int) responseFormat.getStatus(), HttpStatus.SC_FORBIDDEN);
        assertEquals("SVC4559", responseFormat.getMessageId());

    }

    @Test
    public void testDeploymentArtifactRestriction() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Either<Service, ResponseFormat> result = Either.left(service);
        Either<ArtifactDefinition, Operation> resultArtifacts = Either.left(new ArtifactDefinition());
        when(toscaElementLifecycleOperation.certifyToscaElement(SERVICE_ID, user.getUserId(), owner.getUserId()))
                .thenReturn(Either.left(ModelConverter.convertToToscaElement(serviceAfterCertify)));
        when(serviceBusinessLogic.generateHeatEnvArtifacts(service, owner, false, true)).thenReturn(result);
        when(serviceBusinessLogic.generateVfModuleArtifacts(service, owner, false, true)).thenReturn(result);
        when(serviceBusinessLogic.populateToscaArtifacts(service, owner, true, false, false)).thenReturn(resultArtifacts);
        changeStateResult = changeTransition.changeState(ComponentTypeEnum.SERVICE, service, serviceBusinessLogic, user, owner, false, true);
        assertTrue(changeStateResult.isLeft());
    }

    private void simulateCertifiedVersionExistForRI() {
        Component dummyResource = new Resource();
        Either<Component, StorageOperationStatus> result = Either.left(dummyResource);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(Either.left(dummyResource));
        Mockito.when(toscaOperationFacade.findLastCertifiedToscaElementByUUID(Mockito.any(Component.class))).thenReturn(result);
    }

    private Resource createVFWithRI(String riVersion) {
        Resource resource = new Resource();
        List<ComponentInstance> riList = new ArrayList<>();
        ComponentInstance ri = new ComponentInstance();

        ri.setComponentVersion(riVersion);
        ri.setComponentUid("someUniqueId");
        riList.add(ri);
        resource.setComponentInstances(riList);
        return resource;
    }

}