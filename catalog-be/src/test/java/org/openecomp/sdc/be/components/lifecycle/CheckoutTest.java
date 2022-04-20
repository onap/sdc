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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.csar.CsarArtifactsAndGroupsBusinessLogic;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.CompositionBusinessLogic;
import org.openecomp.sdc.be.components.impl.DataTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.OutputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.utils.MergeInstanceUtils;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertTrue;

public class CheckoutTest extends LifecycleTestBase {

    private CheckoutTransition checkoutObj = null;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
    private final InputsBusinessLogic inputsBusinessLogic = Mockito.mock(InputsBusinessLogic.class);
    private final OutputsBusinessLogic outputsBusinessLogic = Mockito.mock(OutputsBusinessLogic.class);
    private final CompositionBusinessLogic compositionBusinessLogic = Mockito.mock(CompositionBusinessLogic.class);
    private final ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic = Mockito.mock(ResourceDataMergeBusinessLogic.class);
    private final CsarArtifactsAndGroupsBusinessLogic csarArtifactsAndGroupsBusinessLogic = Mockito.mock(CsarArtifactsAndGroupsBusinessLogic.class);
    private final MergeInstanceUtils mergeInstanceUtils = Mockito.mock(MergeInstanceUtils.class);
    private final UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    private final CsarBusinessLogic csarBusinessLogic = Mockito.mock(CsarBusinessLogic.class);
    private final PropertyBusinessLogic propertyBusinessLogic = Mockito.mock(PropertyBusinessLogic.class);
    private final ComponentContactIdValidator componentContactIdValidator = new ComponentContactIdValidator(componentsUtils);
    private final ComponentNameValidator componentNameValidator = new ComponentNameValidator(componentsUtils, toscaOperationFacade);
    private final PolicyBusinessLogic policyBusinessLogic = Mockito.mock(PolicyBusinessLogic.class);
    private final ModelBusinessLogic modelBusinessLogic = Mockito.mock(ModelBusinessLogic.class);
    private final DataTypeBusinessLogic dataTypeBusinessLogic = Mockito.mock(DataTypeBusinessLogic.class);
    private final PolicyTypeBusinessLogic policyTypeBusinessLogic = Mockito.mock(PolicyTypeBusinessLogic.class);
    private final ModelOperation modelOperation = Mockito.mock(ModelOperation.class);
    @InjectMocks
    ResourceBusinessLogic bl = new ResourceBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
        groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation, artifactsBusinessLogic,
        componentInstanceBusinessLogic, resourceImportManager, inputsBusinessLogic, outputsBusinessLogic,compositionBusinessLogic,
        resourceDataMergeBusinessLogic, csarArtifactsAndGroupsBusinessLogic, mergeInstanceUtils,
        uiComponentDataConverter, csarBusinessLogic, artifactToscaOperation, propertyBusinessLogic,
        componentContactIdValidator, componentNameValidator, componentTagsValidator, componentValidator,
        componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator ,policyBusinessLogic, modelBusinessLogic,
        dataTypeBusinessLogic, policyTypeBusinessLogic, modelOperation);

    @Before
    public void setup() {

        super.setup();
        // checkout transition object
        checkoutObj = new CheckoutTransition(componentsUtils, toscaElementLifecycleOperation, toscaOperationFacade,
            janusGraphDao);
        checkoutObj.setLifeCycleOperation(toscaElementLifecycleOperation);
        checkoutObj.setConfigurationManager(configurationManager);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setComponentsUtils(componentsUtils);

    }

    @Test
    public void testCheckoutStateValidation() {
        Either<? extends Component, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        changeStateResult = checkoutObj.changeState(ComponentTypeEnum.RESOURCE, resource, bl, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        changeStateResult = checkoutObj.changeState(ComponentTypeEnum.RESOURCE, resource, bl, user, owner, false, false);
        assertTrue(changeStateResult.isLeft());

    }

    @Test
    public void testAlreadyCheckout() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());

        assertTrue(changeStateResult.isRight());
        assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CHECKOUT_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

    }

    @Test
    public void testRoles() {
        Either<Resource, ResponseFormat> changeStateResult;
        Resource resource = createResourceObject();

        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

        User modifier = new User();
        modifier.setUserId("modifier");
        modifier.setFirstName("Albert");
        modifier.setLastName("Einstein");
        modifier.setRole(Role.DESIGNER.name());
        Either<User, ResponseFormat> ownerResponse = checkoutObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
        assertTrue(ownerResponse.isLeft());
        User owner = ownerResponse.left().value();
        // changeStateResult = checkoutObj.changeStateOperation(resource,
        // modifier, owner);
        Either<Boolean, ResponseFormat> validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isLeft());

        modifier.setRole(Role.TESTER.name());
        // changeStateResult = checkoutObj.changeStateOperation(resource,
        // modifier, owner);
        validateBeforeTransition = checkoutObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, modifier, owner, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        assertTrue(validateBeforeTransition.isRight());
        changeStateResult = Either.right(validateBeforeTransition.right().value());
        assertResponse(changeStateResult, ActionStatus.RESTRICTED_OPERATION);

    }
}
