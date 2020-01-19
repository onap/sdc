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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.http.HttpStatus;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ServiceBusinessLogicTest extends ServiceBussinessLogicBaseTestSetup {

    private final static String DEFAULT_ICON = "defaulticon";
    private static final String ALREADY_EXIST = "alreadyExist";
    private static final String DOES_NOT_EXIST = "doesNotExist";

    @Test
    public void testGetComponentAuditRecordsCertifiedVersion() {
        Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(CERTIFIED_VERSION, COMPONNET_ID, user.getUserId());
        assertTrue(componentAuditRecords.isLeft());
        assertEquals(3, componentAuditRecords.left().value().size());
    }

    @Test
    public void testGetComponentAuditRecordsUnCertifiedVersion() {
        Either<List<Map<String, Object>>, ResponseFormat> componentAuditRecords = bl.getComponentAuditRecords(UNCERTIFIED_VERSION, COMPONNET_ID, user.getUserId());
        assertTrue(componentAuditRecords.isLeft());
        assertEquals(4, componentAuditRecords.left().value().size());
    }

    @Test
    public void testHappyScenario() {
        Service service = createServiceObject(false);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }
    @Test
    public void testHappyScenarioCRNullProjectCode() {
        Service service = createServiceObject(false);
        service.setProjectCode(null);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }
    @Test
    public void testHappyScenarioCREmptyStringProjectCode() {
        createServiceValidator();
        Service service = createServiceObject(false);
        service.setProjectCode("");
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);

        if (createResponse.isRight()) {
            assertEquals(new Integer(200), createResponse.right().value().getStatus());
        }
        assertEqualsServiceObject(createServiceObject(true), createResponse.left().value());
    }

    private void validateUserRoles(Role ... roles) {
        List<Role> listOfRoles = Stream.of(roles).collect(Collectors.toList());
    }

    private void assertEqualsServiceObject(Service origService, Service newService) {
        assertEquals(origService.getContactId(), newService.getContactId());
        assertEquals(origService.getCategories(), newService.getCategories());
        assertEquals(origService.getCreatorUserId(), newService.getCreatorUserId());
        assertEquals(origService.getCreatorFullName(), newService.getCreatorFullName());
        assertEquals(origService.getDescription(), newService.getDescription());
        assertEquals(origService.getIcon(), newService.getIcon());
        assertEquals(origService.getLastUpdaterUserId(), newService.getLastUpdaterUserId());
        assertEquals(origService.getLastUpdaterFullName(), newService.getLastUpdaterFullName());
        assertEquals(origService.getName(), newService.getName());
        assertEquals(origService.getName(), newService.getName());
        assertEquals(origService.getUniqueId(), newService.getUniqueId());
        assertEquals(origService.getVersion(), newService.getVersion());
        assertEquals(origService.getArtifacts(), newService.getArtifacts());
        assertEquals(origService.getCreationDate(), newService.getCreationDate());
        assertEquals(origService.getLastUpdateDate(), newService.getLastUpdateDate());
        assertEquals(origService.getLifecycleState(), newService.getLifecycleState());
        assertEquals(origService.getTags(), newService.getTags());
    }


    /* CREATE validations - start ***********************/
    // Service name - start


    @Test
    public void testFailedServiceValidations() {

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
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
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
        try{
            bl.createService(serviceExccedsNameLimit, user);
        } catch(ComponentException e){
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
        try{
            bl.createService(service, user);
        } catch(ComponentException e){
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
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testServiceDescriptionMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setDescription(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e){
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
        try{
            bl.createService(serviceExccedsDescLimit, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.COMPONENT_DESCRIPTION_EXCEEDS_LIMIT, ComponentTypeEnum.SERVICE.getValue(), "" + ValidationUtils.COMPONENT_DESCRIPTION_MAX_LENGTH);
            return;
        }
        fail();
    }

    private void testServiceDescNotEnglish() {
        Service notEnglish = createServiceObject(false);
        // Not english
        String tooLongServiceDesc = "\uC2B5";
        notEnglish.setDescription(tooLongServiceDesc);
        try{
            bl.createService(notEnglish, user);
        } catch(ComponentException e){
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
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist,user,AuditingActionEnum.CREATE_SERVICE);
        assertThat(service.left().value().getIcon()).isEqualTo(DEFAULT_ICON);

    }

    private void testServiceIconMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setIcon(null);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(serviceExist,user,AuditingActionEnum.CREATE_SERVICE);
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
        try{
            bl.createService(serviceExccedsNameLimit, user);
        } catch(ComponentException e) {
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
        try{
            bl.createService(serviceExccedsNameLimit, user);
        } catch(ComponentException e) {
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
        try{
            bl.createService(serviceContactId, user);
        } catch(ComponentException e) {
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
        try{
            bl.createService(serviceContactId, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    private void testResourceContactIdMissing() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setContactId(null);
        try{
            bl.createService(resourceExist, user);
        } catch(ComponentException e) {
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
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
            return;
        }
        fail();
    }

    @Test
    public void markDistributionAsDeployedTestAlreadyDeployed() {
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

        Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.eq(resultAction), Mockito.anyString())).thenReturn(eventList);

        Either<Service, ResponseFormat> markDeployed = bl.markDistributionAsDeployed(did, did, user);
        assertTrue(markDeployed.isLeft());

        Mockito.verify(auditingDao, Mockito.times(0)).getDistributionRequest(did, requestAction);

    }

    @Test
    public void markDistributionAsDeployedTestSuccess() {
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
    public void markDistributionAsDeployedTestNotDistributed() {
        String notifyAction = "DNotify";
        String requestAction = "DRequest";
        String did = "123456";

        setupBeforeDeploy(notifyAction, requestAction, did);
        List<ResourceAdminEvent> emptyList = new ArrayList<>();
        Either<List<ResourceAdminEvent>, ActionStatus> emptyEventList = Either.left(emptyList);
        Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), Mockito.eq(requestAction))).thenReturn(emptyEventList);

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
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
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
        } catch(ComponentException exp) {
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
        } catch(ComponentException exp) {
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
        } catch(ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROJECT_CODE);
            return;
        }
        fail();
    }

    @Test
    public void testDeleteMarkedServices() {
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


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testFindGroupInstanceOnRelatedComponentInstance() {

        Class<ServiceBusinessLogic> targetClass = ServiceBusinessLogic.class;
        String methodName = "findGroupInstanceOnRelatedComponentInstance";
        Object invalidId = "invalidId";

        Component service = createNewService();
        List<ComponentInstance> componentInstances = service.getComponentInstances();

        Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat> findGroupInstanceRes;
        Object[] argObjects = {service, componentInstances.get(1).getUniqueId(), componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};
        Class[] argClasses = {Component.class, String.class,String.class};
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

            Object[] argObjectsInvalidCiId = {service, invalidId , componentInstances.get(1).getGroupInstances().get(1).getUniqueId()};

            findGroupInstanceRes =    (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidCiId);
            assertNotNull(findGroupInstanceRes);
            assertTrue(findGroupInstanceRes.isRight());
            assertEquals("SVC4593", findGroupInstanceRes.right().value().getMessageId());

            Object[] argObjectsInvalidGiId = {service, componentInstances.get(1).getUniqueId() , invalidId};

            findGroupInstanceRes =    (Either<ImmutablePair<ComponentInstance, GroupInstance>, ResponseFormat>) method.invoke(bl, argObjectsInvalidGiId);
            assertNotNull(findGroupInstanceRes);
            assertTrue(findGroupInstanceRes.isRight());
            assertEquals("SVC4653", findGroupInstanceRes.right().value().getMessageId());
        }
        catch (Exception e) {
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
        for(int i= 0; i<listSize; ++i){
            ci = new ComponentInstance();
            ci.setName("ciName" + i);
            ci.setUniqueId("ciId" + i);
            List<GroupInstance>  groupInstances= new ArrayList<>();
            GroupInstance gi;
            for(int j = 0; j<listSize; ++j){
                gi = new GroupInstance();
                gi.setName(ci.getName( )+ "giName" + j);
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
        return (Service)createNewComponent();
    }


    @Test
    public void testDerivedFromGeneric() {
        Service service = createServiceObject(true);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        when(toscaOperationFacade.createToscaComponent(service)).thenReturn(Either.left(service));
        when(genericTypeBusinessLogic.fetchDerivedFromGenericType(service)).thenReturn(Either.left(genericService));
        Either<Service, ResponseFormat> createResponse = bl.createService(service, user);
        assertTrue(createResponse.isLeft());
        service = createResponse.left().value();
        assertEquals(service.getDerivedFromGenericType(), genericService.getToscaResourceName());
        assertEquals(service.getDerivedFromGenericVersion(), genericService.getVersion());
    }

    @Test
    public void testUpdateMetadataNamingPolicy() {
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
    public void testUpdateMetadataToEmptyProjectCode() {
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
    public void testUpdateMetadataFromEmptyProjectCode() {
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
    public void testUpdateMetadataProjectCode() {
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
    public void testUpdateMetadataServiceType() {
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
    public void testCreateDefaultMetadataServiceFunction() {
        Service currentService = createServiceObject(true);
        assertThat(currentService.getServiceFunction()).isEqualTo("");
    }

    @Test
    public void testCreateCustomMetadataServiceFunction() {
        String customServiceFunctionName = "customName";
        Service currentService = createServiceObject(true);
        currentService.setServiceFunction(customServiceFunctionName);
        assertThat(currentService.getServiceFunction()).isEqualTo(customServiceFunctionName);
    }

    @Test
    public void testUpdateMetadataServiceFunction() {
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
        assertThat(updatedService.getServiceFunction()).isEqualTo("");
    }



    @Test
    public void testServiceFunctionExceedLength() {
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
    public void testServiceFunctionInvalidCharacter(){
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
    public void testAddPropertyServiceConsumptionServiceNotFound() {
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

        Either<Operation, ResponseFormat> operationEither =
                bl.addPropertyServiceConsumption("1", "2", "3",
                        user.getUserId(), new ServiceConsumptionData());
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    public void testAddPropertyServiceConsumptionParentServiceIsEmpty() {
        Either<Component, StorageOperationStatus> eitherService = Either.left(createNewComponent());
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<Operation, ResponseFormat> operationEither =
                bl.addPropertyServiceConsumption("1", "2", "3",
                        user.getUserId(), new ServiceConsumptionData());
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    public void testAddPropertyServiceConsumptionNoMatchingComponent() {
        Service aService = createNewService();
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        String weirdUniqueServiceInstanceId = UUID.randomUUID().toString();

        Either<Operation, ResponseFormat> operationEither =
                bl.addPropertyServiceConsumption("1", weirdUniqueServiceInstanceId, "3",
                        user.getUserId(), new ServiceConsumptionData());
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    public void testAddPropertyServiceConsumptionNotComponentInstancesInterfacesOnParentService() {
        Service aService = createNewService();
        aService.getComponentInstances().get(0).setUniqueId(aService.getUniqueId());
        Either<Component, StorageOperationStatus> eitherService = Either.left(aService);
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<Operation, ResponseFormat> operationEither =
                bl.addPropertyServiceConsumption("1", aService.getUniqueId(), "3",
                        user.getUserId(), new ServiceConsumptionData());
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    public void testAddPropertyServiceConsumptionInterfaceCandidateNotPresent() {
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
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

    @Test
    public void testAddPropertyServiceConsumptionNoInputsCandidate() {
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
        Assert.assertTrue(operationEither.isRight());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), operationEither.right().value().getStatus().intValue());
    }

}
