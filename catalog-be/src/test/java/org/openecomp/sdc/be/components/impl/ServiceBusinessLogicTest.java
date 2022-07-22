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

package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fj.data.Either;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ToscaOperationException;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.plugins.ServiceCreationPlugin;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.http.HttpStatus;

class ServiceBusinessLogicTest extends ServiceBusinessLogicBaseTestSetup {

    private final static String DEFAULT_ICON = "defaulticon";
    private static final String ALREADY_EXIST = "alreadyExist";

    @Test
    void testGetComponentAuditRecordsCertifiedVersion() {
        Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(CERTIFIED_VERSION, COMPONNET_ID,
            user.getUserId());
        assertTrue(componentAuditRecords.isLeft());
        assertEquals(3, componentAuditRecords.left().value().size());
    }

    @Test
    void testGetComponentAuditRecordsUnCertifiedVersion() {
        Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(UNCERTIFIED_VERSION, COMPONNET_ID,
            user.getUserId());
        assertTrue(componentAuditRecords.isLeft());
        assertEquals(4, componentAuditRecords.left().value().size());
    }

    @Test
    void testHappyScenario() {
        Service service = createServiceObject(false);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }

    @Test
    void testServiceCreationPluginCall() {
        final Service service = createServiceObject(false);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        final List<ServiceCreationPlugin> serviceCreationPlugins = new ArrayList<>();
        serviceCreationPlugins.add(new ServiceCreationPlugin() {
            @Override
            public void beforeCreate(Service service) {
                //do nothing
            }

            @Override
            public int getOrder() {
                return 0;
            }
        });
        serviceCreationPlugins.add(new ServiceCreationPlugin() {
            @Override
            public void beforeCreate(Service service) {
                throw new RuntimeException();
            }

            @Override
            public int getOrder() {
                return 0;
            }
        });
        bl.setServiceCreationPluginList(serviceCreationPlugins);
        final Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isLeft());
    }


    @Test
    void testCreateServiceWhenGenericTypeHasProperties() {
        final Service service = createServiceObject(false);

        final Resource genericTypeResource = mockGenericTypeResource();

        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericTypeResource));
        final Service expectedService = createServiceObject(true);
        expectedService.setProperties(mockPropertyList());
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(expectedService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        org.hamcrest.MatcherAssert.assertThat("Service creation should be successful",
            createResponse.isLeft(), is(true));
        final Service actualService = createResponse.left().value();
        org.hamcrest.MatcherAssert.assertThat("Service should not be null", service, is(notNullValue()));

        assertEqualsServiceObject(expectedService, actualService);
    }

    @Test
    void testCreateServiceWhenGenericTypeAndServiceHasProperties() {
        final Service service = createServiceObject(false);
        service.setProperties(mockPropertyList());
        service.getProperties().remove(0);
        final PropertyDefinition serviceProperty = new PropertyDefinition();
        serviceProperty.setName("aServiceProperty");
        service.getProperties().add(serviceProperty);

        final Resource genericTypeResource = mockGenericTypeResource();

        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericTypeResource));
        final Service expectedService = createServiceObject(true);
        expectedService.setProperties(mockPropertyList());
        expectedService.getProperties().add(serviceProperty);
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(expectedService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        org.hamcrest.MatcherAssert.assertThat("Service creation should be successful",
            createResponse.isLeft(), is(true));
        final Service actualService = createResponse.left().value();
        org.hamcrest.MatcherAssert.assertThat("Service should not be null", service, is(notNullValue()));

        assertEqualsServiceObject(expectedService, actualService);
    }

    @Test
    void testHappyScenarioCRNullProjectCode() {
        Service service = createServiceObject(false);
        service.setProjectCode(null);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }

    @Test
    void testHappyScenarioCREmptyStringProjectCode() {
        createServiceValidator();
        Service service = createServiceObject(false);
        service.setProjectCode("");
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }

    private void assertEqualsServiceObject(final Service expectedService, final Service actualService) {
        assertEquals(expectedService.getContactId(), actualService.getContactId());
        assertEquals(expectedService.getCategories(), actualService.getCategories());
        assertEquals(expectedService.getCreatorUserId(), actualService.getCreatorUserId());
        assertEquals(expectedService.getCreatorFullName(), actualService.getCreatorFullName());
        assertEquals(expectedService.getDescription(), actualService.getDescription());
        assertEquals(expectedService.getIcon(), actualService.getIcon());
        assertEquals(expectedService.getLastUpdaterUserId(), actualService.getLastUpdaterUserId());
        assertEquals(expectedService.getLastUpdaterFullName(), actualService.getLastUpdaterFullName());
        assertEquals(expectedService.getName(), actualService.getName());
        assertEquals(expectedService.getUniqueId(), actualService.getUniqueId());
        assertEquals(expectedService.getVersion(), actualService.getVersion());
        assertEquals(expectedService.getArtifacts(), actualService.getArtifacts());
        assertEquals(expectedService.getCreationDate(), actualService.getCreationDate());
        assertEquals(expectedService.getLastUpdateDate(), actualService.getLastUpdateDate());
        assertEquals(expectedService.getLifecycleState(), actualService.getLifecycleState());
        assertEquals(expectedService.getTags(), actualService.getTags());
        if (expectedService.getProperties() == null) {
            org.hamcrest.MatcherAssert.assertThat("Service properties should be null",
                actualService.getProperties(), is(nullValue()));
            return;
        }
        org.hamcrest.MatcherAssert.assertThat("Service properties should be as expected",
            actualService.getProperties(), is(expectedService.getProperties()));
    }

    /* CREATE validations - start ***********************/
    // Service name - start

    @Test
    void testFailedServiceValidations() {

        testServiceNameAlreadyExists();
        testServiceNameEmpty();
        testServiceNameWrongFormat();
        testServiceDescriptionEmpty();
        testServiceDescriptionMissing();
        testServiceDescExceedsLimitCreate();
        testServiceDescNotEnglish();
        testServiceIconEmpty();
        testServiceIconMissing();
        testResourceIconInvalid();
        testTagsNoServiceName();
        testInvalidTag();
        testServiceTagNotExist();
        testServiceTagEmpty();

        testContactIdTooLong();
        testContactIdWrongFormatCreate();
        testInvalidProjectCode();
        testProjectCodeTooLong();
        testProjectCodeTooShort();

        testResourceContactIdMissing();
        testServiceCategoryExist();
        testServiceBadCategoryCreate();
    }

    private void testServiceNameAlreadyExists() {
        String serviceName = ALREADY_EXIST;
        Service serviceExccedsNameLimit = createServiceObject(false);
        // 51 chars, the limit is 50
        serviceExccedsNameLimit.setName(serviceName);
        List<String> tgs = new ArrayList<>();
        tgs.add(serviceName);
        serviceExccedsNameLimit.setTags(tgs);
        try {
            bl.createService(serviceExccedsNameLimit, user);
        } catch (ComponentException exp) {
            assertResponse(exp.getResponseFormat(), ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.SERVICE.getValue(), serviceName);
            return;
        }
        fail();
    }

    private void testServiceNameEmpty() {
        Service serviceExccedsNameLimit = createServiceObject(false);
        serviceExccedsNameLimit.setName(null);
        try {
            bl.createService(serviceExccedsNameLimit, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testServiceNameWrongFormat() {
        Service service = createServiceObject(false);
        // contains :
        String nameWrongFormat = "ljg\\fd";
        service.setName(nameWrongFormat);
        try {
            bl.createService(service, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    // Service name - end
    // Service description - start

    private void testServiceDescriptionEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setDescription("");
        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testServiceDescriptionMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setDescription(null);
        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testServiceDescExceedsLimitCreate() {
        Service serviceExccedsDescLimit = createServiceObject(false);
        // 1025 chars, the limit is 1024
        String tooLongServiceDesc = "1GUODojQ0sGzKR4NP7e5j82ADQ3KHTVOaezL95qcbuaqDtjZhAQGQ3iFwKAy580K4WiiXs3u3zq7RzXcSASl5fm0RsWtCMOIDP"
            + "AOf9Tf2xtXxPCuCIMCR5wOGnNTaFxgnJEHAGxilBhZDgeMNHmCN1rMK5B5IRJOnZxcpcL1NeG3APTCIMP1lNAxngYulDm9heFSBc8TfXAADq7703AvkJT0QPpGq2z2P"
            + "tlikcAnIjmWgfC5Tm7UH462BAlTyHg4ExnPPL4AO8c92VrD7kZSgSqiy73cN3gLT8uigkKrUgXQFGVUFrXVyyQXYtVM6bLBeuCGQf4C2j8lkNg6M0J3PC0PzMRoinOxk"
            + "Ae2teeCtVcIj4A1KQo3210j8q2v7qQU69Mabsa6DT9FgE4rcrbiFWrg0Zto4SXWD3o1eJA9o29lTg6kxtklH3TuZTmpi5KVp1NFhS1RpnqF83tzv4mZLKsx7Zh1fEgYvRFwx1"
            + "ar3RolyDfNoZiGBGTMsZzz7RPFBf2hTnLmNqVGQnHKhhGj0Y5s8t2cbqbO2nmHiJb9uaUVrCGypgbAcJL3KPOBfAVW8PcpmNj4yVjI3L4x5zHjmGZbp9vKshEQODcrmcgsYAoKqe"
            + "uu5u7jk8XVxEfQ0m5qL8UOErXPlJovSmKUmP5B5T0w299zIWDYCzSoNasHpHjOMDLAiDDeHbozUOn9t3Qou00e9POq4RMM0VnIx1H38nJoJZz2XH8CI5YMQe7oTagaxgQTF2aa0qaq2"
            + "V6nJsfRGRklGjNhFFYP2cS4Xv2IJO9DSX6LTXOmENrGVJJvMOZcvnBaZPfoAHN0LU4i1SoepLzulIxnZBfkUWFJgZ5wQ0Bco2GC1HMqzW21rwy4XHRxXpXbmW8LVyoA1KbnmVmROycU4"
            + "scTZ62IxIcIWCVeMjBIcTviXULbPUyqlfEPXWr8IMJtpAaELWgyquPClAREMDs2b9ztKmUeXlMccFES1XWbFTrhBHhmmDyVReEgCwfokrUFR13LTUK1k8I6OEHOs";

        serviceExccedsDescLimit.setDescription(tooLongServiceDesc);
        try {
            bl.createService(serviceExccedsDescLimit, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.SERVICE.getValue(),
                "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
            return;
        }
        fail();
    }

    private void testServiceDescNotEnglish() {
        Service notEnglish = createServiceObject(false);
        // Not english
        String tooLongServiceDesc = "\uC2B5";
        notEnglish.setDescription(tooLongServiceDesc);
        try {
            bl.createService(notEnglish, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    // Service description - stop
    // Service icon - start

    private void testServiceIconEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setIcon("");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist, user, AuditingActionEnum.CREATE_SERVICE);
        assertThat(service.left().value().getIcon()).isEqualTo(DEFAULT_ICON);

    }

    private void testServiceIconMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setIcon(null);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist, user, AuditingActionEnum.CREATE_SERVICE);
        assertThat(service.left().value().getIcon()).isEqualTo(DEFAULT_ICON);
    }

    private void testResourceIconInvalid() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setIcon("kjk3453^&");

        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(resourceExist, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getIcon()).isEqualTo(DEFAULT_ICON);

    }

    private void testTagsNoServiceName() {
        Service serviceExccedsNameLimit = createServiceObject(false);
        String tag1 = "afzs2qLBb";
        List<String> tagsList = new ArrayList<>();
        tagsList.add(tag1);
        serviceExccedsNameLimit.setTags(tagsList);
        try {
            bl.createService(serviceExccedsNameLimit, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_TAGS_NO_COMP_NAME);
            return;
        }
        fail();
    }

    private void testInvalidTag() {
        Service serviceExccedsNameLimit = createServiceObject(false);
        String tag1 = "afzs2qLBb%#%";
        List<String> tagsList = new ArrayList<>();
        tagsList.add(tag1);
        serviceExccedsNameLimit.setTags(tagsList);
        try {
            bl.createService(serviceExccedsNameLimit, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.INVALID_FIELD_FORMAT, "Service", "tag");
            return;
        }
        fail();
    }

    private void testServiceTagNotExist() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setTags(null);

        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getTags().get(0)).isEqualTo(serviceExist.getName());
    }

    private void testServiceTagEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setTags(new ArrayList<>());

        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getTags().get(0)).isEqualTo(serviceExist.getName());
    }

    // Service tags - stop
    // Service contactId - start

    private void testContactIdTooLong() {
        Service serviceContactId = createServiceObject(false);
        // 59 chars instead of 50
        String contactIdTooLong = "thisNameIsVeryLongAndExeccedsTheNormalLengthForContactId";
        serviceContactId.setContactId(contactIdTooLong);
        try {
            bl.createService(serviceContactId, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testContactIdWrongFormatCreate() {
        Service serviceContactId = createServiceObject(false);
        // 3 letters and 3 digits and special characters
        String contactIdTooLong = "yrt134!!!";
        serviceContactId.setContactId(contactIdTooLong);
        try {
            bl.createService(serviceContactId, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testResourceContactIdMissing() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setContactId(null);
        try {
            bl.createService(resourceExist, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    // Service contactId - stop
    // Service category - start

    private void testServiceCategoryExist() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setCategories(null);
        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    @Test
    void markDistributionAsDeployedTestAlreadyDeployed() {
        String notifyAction = "DNotify";
        String requestAction = "DRequest";
        String resultAction = "DResult";
        String did = "123456";

        setupBeforeDeploy(notifyAction, requestAction, did);
        List<DistributionDeployEvent> resultList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        DistributionDeployEvent event = new DistributionDeployEvent();

        event.setAction(resultAction);
        event.setDid(did);
        event.setStatus("200");
        // ESTimeBasedEvent deployEvent = new ESTimeBasedEvent();
        // deployEvent.setFields(params);
        resultList.add(event);
        Either<List<DistributionDeployEvent>, ActionStatus> eventList = Either.left(resultList);

        Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), eq(resultAction), Mockito.anyString())).thenReturn(eventList);

        Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
        assertTrue(markDeployed.isLeft());

        Mockito.verify(auditingDao, Mockito.times(0)).getDistributionRequest(did, requestAction);

    }

    @Test
    void markDistributionAsDeployedTestSuccess() {
        String notifyAction = "DNotify";
        String requestAction = "DRequest";
        String did = "123456";

        setupBeforeDeploy(notifyAction, requestAction, did);
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ADMIN);
        roles.add(Role.DESIGNER);
        Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
        assertTrue(markDeployed.isLeft());
    }

    @Test
    void markDistributionAsDeployedTestNotDistributed() {
        String notifyAction = "DNotify";
        String requestAction = "DRequest";
        String did = "123456";

        setupBeforeDeploy(notifyAction, requestAction, did);
        List<ResourceAdminEvent> emptyList = new ArrayList<>();
        Either<List<ResourceAdminEvent>, ActionStatus> emptyEventList = Either.left(emptyList);
        Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), eq(requestAction))).thenReturn(emptyEventList);

        Either<Component, StorageOperationStatus> notFound = Either.right(StorageOperationStatus.NOT_FOUND);
        Mockito.when(toscaOperationFacade.getToscaElement(did)).thenReturn(notFound);

        Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
        assertTrue(markDeployed.isRight());
        assertEquals(404, markDeployed.right().value().getStatus().intValue());

    }

    private void testServiceBadCategoryCreate() {

        Service serviceExist = createServiceObject(false);
        CategoryDefinition category = new CategoryDefinition();
        category.setName("koko");
        category.setIcons(Arrays.asList(DEFAULT_ICON));
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        serviceExist.setCategories(categories);
        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    // Service category - stop
    // Service projectCode - start

    private void testInvalidProjectCode() {

        Service serviceExist = createServiceObject(false);
        serviceExist.setProjectCode("koko!!");

        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROJECT_CODE);
            return;
        }
        fail();
    }

    private void testProjectCodeTooLong() {

        Service serviceExist = createServiceObject(false);
        String tooLongProjectCode = "thisNameIsVeryLongAndExeccedsTheNormalLengthForProjectCode";
        serviceExist.setProjectCode(tooLongProjectCode);

        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROJECT_CODE);
            return;
        }
        fail();
    }

    private void testProjectCodeTooShort() {

        Service serviceExist = createServiceObject(false);
        serviceExist.setProjectCode("333");

        try {
            bl.createService(serviceExist, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROJECT_CODE);
            return;
        }
        fail();
    }

    @Test
    void testDeleteMarkedServices() {
        List<String> ids = new ArrayList<>();
        List<String> responseIds = new ArrayList<>();
        String resourceInUse = "123";
        ids.add(resourceInUse);
        String resourceFree = "456";
        ids.add(resourceFree);
        responseIds.add(resourceFree);
        Either<List<String>, StorageOperationStatus> eitherNoResources = Either.left(ids);
        when(toscaOperationFacade.getAllComponentsMarkedForDeletion(ComponentTypeEnum.RESOURCE)).thenReturn(eitherNoResources);

        Either<Boolean, StorageOperationStatus> resourceInUseResponse = Either.left(true);
        Either<Boolean, StorageOperationStatus> resourceFreeResponse = Either.left(false);

        List<ArtifactDefinition> artifacts = new ArrayList<>();
        Either<List<ArtifactDefinition>, StorageOperationStatus> getArtifactsResponse = Either.left(artifacts);

        Either<Component, StorageOperationStatus> eitherDelete = Either.left(new Resource());
        when(toscaOperationFacade.deleteToscaComponent(resourceFree)).thenReturn(eitherDelete);
        when(toscaOperationFacade.deleteMarkedElements(ComponentTypeEnum.SERVICE)).thenReturn(Either.left(responseIds));
        Either<List<String>, ResponseFormat> deleteMarkedResources = bl.deleteMarkedComponents();
        assertTrue(deleteMarkedResources.isLeft());
        List<String> resourceIdList = deleteMarkedResources.left().value();
        assertFalse(resourceIdList.isEmpty());
        assertTrue(resourceIdList.contains(resourceFree));
        assertFalse(resourceIdList.contains(resourceInUse));
    }

    @Test
    void testDeleteArchivedService_NotFound() {
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        assertThrows(StorageException.class, () -> bl.deleteServiceAllVersions("1", user));
    }

    @Test
    void testDeleteArchivedService_NotArchived() {
        String serviceId = "12345";
        Either<Component, StorageOperationStatus> eitherService = Either.left(createNewService());
        eitherService.left().value().setArchived(false);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);
        final ComponentException actualException = assertThrows(ComponentException.class, () -> bl.deleteServiceAllVersions(serviceId, user));
        assertEquals(ActionStatus.COMPONENT_NOT_ARCHIVED, actualException.getActionStatus());
        assertEquals(actualException.getParams()[0], serviceId);
    }

    @Test
    void testDeleteArchivedService_DeleteServiceSpecificModel() throws ToscaOperationException {
        String serviceId = "12345";
        String model = "serviceSpecificModel";
        List<String> deletedServcies = new ArrayList<>();
        deletedServcies.add("54321");
        Model normativeExtensionModel = new Model("normativeExtensionModel", ModelTypeEnum.NORMATIVE_EXTENSION);
        Either<Component, StorageOperationStatus> eitherService = Either.left(createNewService());
        eitherService.left().value().setArchived(true);
        eitherService.left().value().setModel(model);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);
        Mockito.when(toscaOperationFacade.deleteService(Mockito.anyString(), eq(true))).thenReturn(deletedServcies);
        Mockito.when(modelOperation.findModelByName(model)).thenReturn(Optional.of(normativeExtensionModel));
        bl.deleteServiceAllVersions(serviceId, user);
        Mockito.verify(modelOperation, Mockito.times(1)).deleteModel(normativeExtensionModel, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testFindGroupInstanceOnRelatedComponentInstance() {

        Class<ServiceBusinessLogic> targetClass = ServiceBusinessLogic.class;
        String methodName = "findGroupInstanceOnRelatedComponentInstance";
        Object invalidId = "invalidId";

        Component service = createNewService();
        List<ComponentInstance> componentInstances = service.getComponentInstances();

        Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> findGroupInstanceRes;
        Object[] argObjects = {service, componentInstances.get(1).getUniqueId(), componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};
        Class[] argClasses = {Component.class, String.class, String.class};
        try {
            Method method = targetClass.getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);

            findGroupInstanceRes = (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjects);
            assertNotNull(findGroupInstanceRes);
            assertEquals(findGroupInstanceRes.left().value().getKey().getUniqueId(), componentInstances.get(1)
                .getUniqueId());
            assertEquals(findGroupInstanceRes.left().value().getValue().getUniqueId(), componentInstances.get(1)
                .getGroupInstances()
                .get(1)
                .getUniqueId());

            Object[] argObjectsInvalidCiId = {service, invalidId, componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};

            findGroupInstanceRes = (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidCiId);
            assertNotNull(findGroupInstanceRes);
            assertTrue(findGroupInstanceRes.isRight());
            assertEquals("SVC4593", findGroupInstanceRes.right().value().getMessageId());

            Object[] argObjectsInvalidGiId = {service, componentInstances.get(1).getUniqueId(), invalidId};

            findGroupInstanceRes = (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidGiId);
            assertNotNull(findGroupInstanceRes);
            assertTrue(findGroupInstanceRes.isRight());
            assertEquals("SVC4653", findGroupInstanceRes.right().value().getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Component createNewComponent() {

        Service service = new Service();
        int listSize = 3;
        service.setName("serviceName");
        service.setUniqueId("serviceUniqueId");
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance ci;
        for (int i = 0; i < listSize; ++i) {
            ci = new ComponentInstance();
            ci.setName("ciName" + i);
            ci.setUniqueId("ciId" + i);
            List<GroupInstance> groupInstances = new ArrayList<>();
            GroupInstance gi;
            for (int j = 0; j < listSize; ++j) {
                gi = new GroupInstance();
                gi.setName(ci.getName() + "giName" + j);
                gi.setUniqueId(ci.getName() + "giId" + j);
                groupInstances.add(gi);
            }
            ci.setGroupInstances(groupInstances);
            componentInstances.add(ci);
        }
        service.setComponentInstances(componentInstances);
        return service;
    }

    protected Service createNewService() {
        return (Service) createNewComponent();
    }

    @Test
    void testDerivedFromGeneric_OK() {
        Service service = createServiceObject(true);
        service.setDerivedFromGenericInfo(genericService);
        service.setProperties(Collections.singletonList(new PropertyDefinition()));
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(service));
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        when(inputsBusinessLogic.declareProperties(eq(user.getUserId()), any(), eq(ComponentTypeEnum.SERVICE), any(ComponentInstInputsMap.class)))
            .thenReturn(Either.left(Collections.singletonList(new InputDefinition())));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isLeft());
        service = createResponse.left().value();
        assertEquals(genericService.getToscaResourceName(), service.getDerivedFromGenericType());
        assertEquals(genericService.getVersion(), service.getDerivedFromGenericVersion());
        // Properties created
        assertNotNull(service.getProperties());
        assertEquals(1, service.getProperties().size());
        // Inputs created
        assertNotNull(service.getInputs());
        assertEquals(1, service.getInputs().size());
    }

    @Test
    void testDerivedFromGeneric_Fail_DeclareProperties() {
        Service service = createServiceObject(true);
        service.setDerivedFromGenericInfo(genericService);
        service.setProperties(Collections.singletonList(new PropertyDefinition()));
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(service));
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        when(inputsBusinessLogic.declareProperties(eq(user.getUserId()), any(), eq(ComponentTypeEnum.SERVICE), any(ComponentInstInputsMap.class)))
            .thenReturn(Either.right(new ResponseFormat(500)));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isLeft());
        service = createResponse.left().value();
        assertEquals(genericService.getToscaResourceName(), service.getDerivedFromGenericType());
        assertEquals(genericService.getVersion(), service.getDerivedFromGenericVersion());
        // Properties created
        assertNotNull(service.getProperties());
        assertEquals(1, service.getProperties().size());
        // Inputs NOT
        assertNull(service.getInputs());
    }

    @Test
    void testDerivedFromGeneric_Fail_CreateToscaComponent() {
        Service service = createServiceObject(true);
        service.setDerivedFromGenericInfo(genericService);
        service.setProperties(Collections.singletonList(new PropertyDefinition()));
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isRight());
        final var responseFormat = createResponse.right().value();
        assertEquals(500, responseFormat.getStatus());
    }

    @Test
    void testServiceWithoutDerivedFromGeneric_OK() {
        final Service service = createServiceObject(true);
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(service));
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service, null)).thenReturn(Either.left(genericService));
        final Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isLeft());
        final Service actualService = createResponse.left().value();
        assertNull(actualService.getDerivedFromGenericType());
        assertNull(actualService.getDerivedFromGenericVersion());
        assertNull(actualService.getProperties());
        assertNull(actualService.getInputs());
    }

    @Test
    void testUpdateMetadataNamingPolicy() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setEcompGeneratedNaming(false);
        newService.setEcompGeneratedNaming(true);
        newService.setNamingPolicy("policy");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.isEcompGeneratedNaming()).isTrue();
        assertThat(updatedService.getNamingPolicy()).isEqualToIgnoringCase("policy");
    }

    @Test
    void testUpdateMetadataToEmptyProjectCode() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setProjectCode("12345");
        newService.setProjectCode("");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.getProjectCode()).isEmpty();
    }

    @Test
    void testUpdateMetadataFromEmptyProjectCode() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setProjectCode("");
        newService.setProjectCode("12345");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.getProjectCode()).isEqualTo("12345");
    }

    @Test
    void testUpdateMetadataProjectCode() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setProjectCode("33333");
        newService.setProjectCode("12345");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.getProjectCode()).isEqualTo("12345");
    }

    @Test
    void testUpdateMetadataServiceType() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setServiceType("alice");
        //valid English word
        newService.setServiceType("bob");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.getServiceType()).isEqualToIgnoringCase("bob");
        //empty string is invalid
        newService.setServiceType("");
        resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        //null is invalid
        newService.setServiceType(null);
        resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isRight()).isTrue();
    }

    @Test
    void testCreateDefaultMetadataServiceFunction() {
        Service currentService = createServiceObject(true);
        assertThat(currentService.getServiceFunction()).isEmpty();
    }

    @Test
    void testCreateCustomMetadataServiceFunction() {
        String customServiceFunctionName = "customName";
        Service currentService = createServiceObject(true);
        currentService.setServiceFunction(customServiceFunctionName);
        assertThat(currentService.getServiceFunction()).isEqualTo(customServiceFunctionName);
    }

    @Test
    void testUpdateMetadataServiceFunction() {
        Service currentService = createServiceObject(true);
        Service newService = createServiceObject(false);
        currentService.setServiceFunction("alice");
        //valid English word
        newService.setServiceFunction("bob");
        Either<Service, ResponseFormat> resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        Service updatedService = resultOfUpdate.left().value();
        assertThat(updatedService.getServiceFunction()).isEqualToIgnoringCase("bob");
        //empty string is valid
        newService.setServiceFunction("");
        resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        //null is valid and assigner to ""
        newService.setServiceFunction(null);
        resultOfUpdate = bl.validateAndUpdateServiceMetadata(user, currentService, newService);
        assertThat(resultOfUpdate.isLeft()).isTrue();
        assertThat(updatedService.getServiceFunction()).isEmpty();
    }

    @Test
    void testServiceFunctionExceedLength() {
        String serviceName = "Service";
        String serviceFunction = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        Service serviceFunctionExceedLength = createServiceObject(false);
        serviceFunctionExceedLength.setName(serviceName);
        serviceFunctionExceedLength.setServiceFunction(serviceFunction);
        List<String> tgs = new ArrayList<>();
        tgs.add(serviceName);
        serviceFunctionExceedLength.setTags(tgs);
        try {
            serviceFunctionValidator.validateAndCorrectField(user, serviceFunctionExceedLength, AuditingActionEnum.CREATE_SERVICE);
        } catch (ComponentException exp) {
            assertResponse(exp.getResponseFormat(), ActionStatus.PROPERTY_EXCEEDS_LIMIT, SERVICE_FUNCTION);
        }
    }

    @Test
    void testServiceFunctionInvalidCharacter() {
        String serviceName = "Service";
        String serviceFunction = "a?";
        Service serviceFunctionExceedLength = createServiceObject(false);
        serviceFunctionExceedLength.setName(serviceName);
        serviceFunctionExceedLength.setServiceFunction(serviceFunction);
        List<String> tgs = new ArrayList<>();
        tgs.add(serviceName);
        serviceFunctionExceedLength.setTags(tgs);
        try {
            serviceFunctionValidator.validateAndCorrectField(user, serviceFunctionExceedLength, AuditingActionEnum.CREATE_SERVICE);
        } catch (ComponentException exp) {
            assertResponse(exp.getResponseFormat(), ActionStatus.INVALID_PROPERY, SERVICE_FUNCTION);
        }
    }

    @Test
    void testAddPropertyServiceConsumptionServiceNotFound() {
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", "2", "3",
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    void testAddPropertyServiceConsumptionParentServiceIsEmpty() {
        Either<Component, StorageOperationStatus> eitherService = Either.left(createNewComponent());
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", "2", "3",
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    void testAddPropertyServiceConsumptionNoMatchingComponent() {
        Service aService = createNewService();
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        String weirdUniqueServiceInstanceId = UUID.randomUUID().toString();

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", weirdUniqueServiceInstanceId, "3",
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    void testAddPropertyServiceConsumptionNotComponentInstancesInterfacesOnParentService() {
        Service aService = createNewService();
        aService.getComponentInstances().get(0).setUniqueId(aService.getUniqueId());
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", aService.getUniqueId(), "3",
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    void testAddPropertyServiceConsumptionInterfaceCandidateNotPresent() {
        Service aService = createNewService();
        aService.getComponentInstances().get(0).setUniqueId(aService.getUniqueId());
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap =
            Maps.newHashMap();
        componentInstancesInterfacesMap.put(aService.getUniqueId(),
            Lists.newArrayList(new ComponentInstanceInterface("1", new InterfaceInstanceDataDefinition())));

        aService.setComponentInstancesInterfaces(componentInstancesInterfacesMap);

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", aService.getUniqueId(), "3",
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    void testAddPropertyServiceConsumptionNoInputsCandidate() {
        Service aService = createNewService();
        aService.getComponentInstances().get(0).setUniqueId(aService.getUniqueId());
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        String operationId = "operationId";
        ComponentInstanceInterface componentInstanceInterface =
            new ComponentInstanceInterface("interfaceId", new InterfaceInstanceDataDefinition());
        Map<String, Operation> operationsMap = Maps.newHashMap();
        operationsMap.put(operationId, new Operation(new ArtifactDataDefinition(), "1",
            new ListDataDefinition<>(), new ListDataDefinition<>()));
        componentInstanceInterface.setOperationsMap(operationsMap);

        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfacesMap = Maps.newHashMap();
        componentInstancesInterfacesMap.put(aService.getUniqueId(), Lists.newArrayList(componentInstanceInterface));
        aService.setComponentInstancesInterfaces(componentInstancesInterfacesMap);

        Either<Operation, ResponseFormat> operationEither =
            bl.addPropertyServiceConsumption("1", aService.getUniqueId(), operationId,
                user.getUserId(), new ServiceConsumptionData());
        assertTrue(operationEither.isRight());
        assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    private Resource mockGenericTypeResource() {
        final Resource genericTypeResource = new Resource();
        genericTypeResource.setProperties(mockPropertyList());
        return genericTypeResource;
    }

    private List<PropertyDefinition> mockPropertyList() {
        final List<PropertyDefinition> propertyList = new ArrayList<>();
        final PropertyDefinition propertyDefinition1 = new PropertyDefinition();
        propertyDefinition1.setName("property1");
        propertyDefinition1.setType("string");
        propertyList.add(propertyDefinition1);

        final PropertyDefinition propertyDefinition2 = new PropertyDefinition();
        propertyDefinition2.setName("property2");
        propertyDefinition2.setType("boolean");
        propertyList.add(propertyDefinition2);

        final PropertyDefinition propertyDefinition3 = new PropertyDefinition();
        propertyDefinition3.setName("property3");
        propertyDefinition3.setType("string");
        propertyList.add(propertyDefinition3);
        return propertyList;
    }

}
