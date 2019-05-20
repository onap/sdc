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
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CertificationRequestTest extends LifecycleTestBase {

    @Mock
    private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;
    @Mock
    private CapabilityOperation capabilityOperation;
    @Mock
    private ToscaExportHandler toscaExportUtils;

    private CertificationRequestTransition rfcObj;


    @Before
    public void setup() {
        super.setup();
        rfcObj = new CertificationRequestTransition(componentsUtils, toscaElementLifecycleOperation, serviceBusinessLogic, toscaOperationFacade,
            janusGraphDao);
        rfcObj.setConfigurationManager(configurationManager);
    }

    @Test
    public void testVFCMTStateValidation(){
        Either<? extends Component, ResponseFormat> changeStateResult;
        Resource resource = createResourceVFCMTObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        User user = new User();
        user.setUserId("cs0008");
        user.setFirstName("Carlos");
        user.setLastName("Santana");
        user.setRole(Role.TESTER.name());

        changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testCheckoutStateValidation() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());
    }

    @Test
    public void testAlreadyRfc() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertResponse(changeStateResult, ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

    }

    @Test
    public void testCertificationInProgress() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

    }

    @Test
    public void testAlreadyCertified() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFIED);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CERTIFIED, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
    }

    @Test
    public void testVSPIsArchivedValidation(){
        Either<? extends Component, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();
        resource.setVspArchived(true);

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        User user = new User();
        user.setUserId("cs0008");
        user.setFirstName("Carlos");
        user.setLastName("Santana");
        user.setRole(Role.TESTER.name());

        changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
        assertTrue(changeStateResult.isRight());
        changeStateResult = Either.right(changeStateResult.right().value());
        assertResponse(changeStateResult, ActionStatus.ARCHIVED_ORIGINS_FOUND, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());
    }


    @Test
    public void testValidateAllResourceInstanceCertified_SuccessWithoutRI() {
        Resource resource = new Resource();
        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);
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

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);
        assertTrue(validateAllResourceInstanceCertified.isLeft());
    }

    @Test
    public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMinorVersion() {
        Resource resource = createVFWithRI("0.3");

        simulateCertifiedVersionExistForRI();

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);

        assertTrue(validateAllResourceInstanceCertified.isRight());
        ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
        assertEquals((int) responseFormat.getStatus(), HttpStatus.SC_FORBIDDEN);
        assertEquals("SVC4559", responseFormat.getMessageId());

    }

    @Test
    public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMajorVersion() {
        Resource resource = createVFWithRI("1.3");

        simulateCertifiedVersionExistForRI();

        Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);

        assertTrue(validateAllResourceInstanceCertified.isRight());
        ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
        assertEquals((int) responseFormat.getStatus(), HttpStatus.SC_FORBIDDEN);
        assertEquals("SVC4559", responseFormat.getMessageId());

    }

    @Test
    public void testDeploymentArtifactRestriction() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        Service service = createServiceObject();
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

        Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(service, ComponentTypeEnum.SERVICE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();

        Either<Service, ResponseFormat> result = Either.left(service);
        Either<ToscaElement, StorageOperationStatus> reqCertRes = Either.left(ModelConverter.convertToToscaElement(service));
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> resultArtifacts = Either.left(Either.left(new ArtifactDefinition()));
        when(serviceBusinessLogic.generateHeatEnvArtifacts(service, owner, false, true)).thenReturn(result);
        when(serviceBusinessLogic.generateVfModuleArtifacts(service, owner, false, true)).thenReturn(result);
        when(serviceBusinessLogic.populateToscaArtifacts(service, owner, true, false, false)).thenReturn(resultArtifacts);
        when(toscaElementLifecycleOperation.requestCertificationToscaElement(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(reqCertRes);
        changeStateResult = rfcObj.changeState(ComponentTypeEnum.SERVICE, service, serviceBusinessLogic, user, owner, false, true);
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
