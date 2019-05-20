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

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.ElementOperationMock;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

public class ServiceBusinessLogicTest {

    private static final String SERVICE_CATEGORY = "Mobility";
    private static final String INSTANTIATION_TYPE = "A-la-carte";
    private final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    private WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    private ServiceBusinessLogic bl = new ServiceBusinessLogic();
    private ResponseFormatManager responseManager = null;
    private ComponentsUtils componentsUtils;
    private AuditCassandraDao auditingDao = Mockito.mock(AuditCassandraDao.class);
    private ArtifactsBusinessLogic artifactBl = Mockito.mock(ArtifactsBusinessLogic.class);
    private GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    private JanusGraphDao mockJanusGraphDao = Mockito.mock(JanusGraphDao.class);
    private ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private CacheMangerOperation cacheManager = Mockito.mock(CacheMangerOperation.class);
    private GenericTypeBusinessLogic genericTypeBusinessLogic = Mockito.mock(GenericTypeBusinessLogic.class);
    private UserValidations userValidations = Mockito.mock(UserValidations.class);
    private ResourceAdminEvent auditArchive1 = Mockito.mock(ResourceAdminEvent.class);
    private ResourceAdminEvent auditArchive2 = Mockito.mock(ResourceAdminEvent.class);
    private ResourceAdminEvent auditRestore = Mockito.mock(ResourceAdminEvent.class);

    private User user = null;
    private Resource genericService = null;

    private static final String CERTIFIED_VERSION = "1.0";
    private static final String UNCERTIFIED_VERSION = "0.2";
    private static final String COMPONNET_ID = "myUniqueId";
    private static final String GENERIC_SERVICE_NAME = "org.openecomp.resource.abstract.nodes.service";

    public ServiceBusinessLogicTest() {

    }

    @Before
    public void setup() {

        ExternalConfiguration.setAppName("catalog-be");
        // init Configuration
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));

        // Elements
        IElementOperation mockElementDao = new ElementOperationMock();

        // User data and management
        user = new User();
        user.setUserId("jh0003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        Either<User, ActionStatus> eitherGetUser = Either.left(user);
        when(mockUserAdmin.getUser("jh0003", false)).thenReturn(eitherGetUser);
        when(userValidations.validateUserExists(eq("jh0003"), anyString(), eq(false))).thenReturn(user);
        when(userValidations.validateUserNotEmpty(eq(user), anyString())).thenReturn(user);
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(IElementOperation.class)).thenReturn(mockElementDao);
        when(graphLockOperation.lockComponent(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.lockComponentByName(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service))).thenReturn(StorageOperationStatus.OK);

        // artifact bussinesslogic
        ArtifactDefinition artifactDef = new ArtifactDefinition();
        when(artifactBl.createArtifactPlaceHolderInfo(Mockito.any(), Mockito.anyString(), Mockito.anyMap(), Mockito.any(User.class), Mockito.any(ArtifactGroupTypeEnum.class))).thenReturn(artifactDef);

        // createService
        Service serviceResponse = createServiceObject(true);
        Either<Component, StorageOperationStatus> eitherCreate = Either.left(serviceResponse);
        when(toscaOperationFacade.createToscaComponent(Mockito.any(Component.class))).thenReturn(eitherCreate);
        Either<Boolean, StorageOperationStatus> eitherCount = Either.left(false);
        when(toscaOperationFacade.validateComponentNameExists("Service", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCount);
        Either<Boolean, StorageOperationStatus> eitherCountExist = Either.left(true);
        when(toscaOperationFacade.validateComponentNameExists("alreadyExist", null, ComponentTypeEnum.SERVICE)).thenReturn(eitherCountExist);

        genericService = setupGenericServiceMock();
        Either<Resource, StorageOperationStatus> findLatestGeneric = Either.left(genericService);
        when(toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(GENERIC_SERVICE_NAME)).thenReturn(findLatestGeneric);


        bl = new ServiceBusinessLogic();
        bl.setElementDao(mockElementDao);
        bl.setUserAdmin(mockUserAdmin);
        bl.setArtifactBl(artifactBl);
        bl.setGraphLockOperation(graphLockOperation);
        bl.setJanusGraphGenericDao(mockJanusGraphDao);
        bl.setToscaOperationFacade(toscaOperationFacade);
        bl.setGenericTypeBusinessLogic(genericTypeBusinessLogic);
        bl.setComponentsUtils(componentsUtils);
        bl.setCassandraAuditingDao(auditingDao);
        bl.setCacheManagerOperation(cacheManager);
        bl.setUserValidations(userValidations);

        mockAuditingDaoLogic();

        responseManager = ResponseFormatManager.getInstance();

    }

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

    private void assertResponse(Either<Service, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
        assertResponse(createResponse.right().value(), expectedStatus, variables);
    }

    private void assertComponentException(ComponentException e, ActionStatus expectedStatus, String... variables) {
        ResponseFormat actualResponse = e.getResponseFormat() != null ?
                e.getResponseFormat() : componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
        assertResponse(actualResponse, expectedStatus, variables);
    }

    private void assertResponse(ResponseFormat actualResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
        assertEquals("assert error description", expectedResponse.getFormattedMessage(), actualResponse.getFormattedMessage());
    }


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
        testResourceIconExceedsLimit();
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
        testMissingProjectCode();
    }

    private void testServiceNameAlreadyExists() {
        String serviceName = "alreadyExist";
        Service serviceExccedsNameLimit = createServiceObject(false);
        // 51 chars, the limit is 50
        serviceExccedsNameLimit.setName(serviceName);
        List<String> tgs = new ArrayList<>();
        tgs.add(serviceName);
        serviceExccedsNameLimit.setTags(tgs);
        validateUserRoles(Role.ADMIN, Role.DESIGNER);
        Either<Service, ResponseFormat> createResponse = bl.createService(serviceExccedsNameLimit, user);
        assertTrue(createResponse.isRight());
        assertResponse(createResponse, ActionStatus.COMPONENT_NAME_ALREADY_EXIST, ComponentTypeEnum.SERVICE.getValue(), serviceName);
    }

    private void testServiceNameEmpty() {
        Service serviceExccedsNameLimit = createServiceObject(false);
        serviceExccedsNameLimit.setName(null);
        try{
            bl.createService(serviceExccedsNameLimit, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testServiceNameWrongFormat() {
        Service service = createServiceObject(false);
        // contains :
        String nameWrongFormat = "ljg\fd";
        service.setName(nameWrongFormat);
        try{
            bl.createService(service, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.INVALID_COMPONENT_NAME, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testServiceDescriptionEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setDescription("");
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testServiceDescriptionMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setDescription(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e){
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_DESCRIPTION, ComponentTypeEnum.SERVICE.getValue());
        }
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
        }
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
        }
    }

    // Service description - stop
    // Service icon - start
    private void testServiceIconEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setIcon("");
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testServiceIconMissing() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setIcon(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_ICON, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testResourceIconInvalid() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setIcon("kjk3453^&");
        try{
            bl.createService(resourceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_ICON, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    private void testResourceIconExceedsLimit() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setIcon("dsjfhskdfhskjdhfskjdhkjdhfkshdfksjsdkfhsdfsdfsdfsfsdfsf");
        try{
            bl.createService(resourceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_ICON_EXCEEDS_LIMIT, "Service", "25");
        }
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
        }
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
        }
    }

    private void testServiceTagNotExist() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setTags(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_TAGS);
        }
    }

    private void testServiceTagEmpty() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setTags(new ArrayList<>());
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_TAGS);
        }
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
        }
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
        }
    }

    private void testResourceContactIdMissing() {
        Service resourceExist = createServiceObject(false);
        resourceExist.setContactId(null);
        try{
            bl.createService(resourceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_MISSING_CONTACT, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    // Service contactId - stop
    // Service category - start
    private void testServiceCategoryExist() {
        Service serviceExist = createServiceObject(false);
        serviceExist.setCategories(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.SERVICE.getValue());
        }
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
        roles.add(Role.OPS);
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
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        serviceExist.setCategories(categories);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
        }
    }

    // Service category - stop
    // Service projectCode - start
    private void testInvalidProjectCode() {

        Service serviceExist = createServiceObject(false);
        serviceExist.setProjectCode("koko!!");

        Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
        assertTrue(createResponse.isRight());

        assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
    }

    private void testProjectCodeTooLong() {

        Service serviceExist = createServiceObject(false);
        String tooLongProjectCode = "thisNameIsVeryLongAndExeccedsTheNormalLengthForProjectCode";
        serviceExist.setProjectCode(tooLongProjectCode);

        Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
        assertTrue(createResponse.isRight());

        assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
    }

    private void testProjectCodeTooShort() {

        Service serviceExist = createServiceObject(false);
        serviceExist.setProjectCode("333");

        Either<Service, ResponseFormat> createResponse = bl.createService(serviceExist, user);
        assertTrue(createResponse.isRight());

        assertResponse(createResponse, ActionStatus.INVALID_PROJECT_CODE);
    }

    private void testMissingProjectCode() {

        Service serviceExist = createServiceObject(false);
        serviceExist.setProjectCode(null);
        try{
            bl.createService(serviceExist, user);
        } catch(ComponentException e) {
            assertComponentException(e, ActionStatus.MISSING_PROJECT_CODE);
        }
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

    private Service createServiceObject(boolean afterCreate) {
        Service service = new Service();
        service.setUniqueId("sid");
        service.setName("Service");
        CategoryDefinition category = new CategoryDefinition();
        category.setName(SERVICE_CATEGORY);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        service.setCategories(categories);
        service.setInstantiationType(INSTANTIATION_TYPE);

        service.setDescription("description");
        List<String> tgs = new ArrayList<>();
        tgs.add(service.getName());
        service.setTags(tgs);
        service.setIcon("MyIcon");
        service.setContactId("aa1234");
        service.setProjectCode("12345");

        if (afterCreate) {
            service.setVersion("0.1");
            service.setUniqueId(service.getName() + ":" + service.getVersion());
            service.setCreatorUserId(user.getUserId());
            service.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
        }
        return service;
    }

    private void mockAuditingDaoLogic() {
        final ResourceAdminEvent createResourceAudit = new ResourceAdminEvent();
        createResourceAudit.setModifier("Carlos Santana(cs0008)");
        createResourceAudit.setCurrState("NOT_CERTIFIED_CHECKOUT");
        createResourceAudit.setCurrVersion("0.1");
        createResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
        createResourceAudit.setRequestId("3e65cea1-7403-4bc7-b461-e2544d83799f");
        createResourceAudit.setDesc("OK");
        createResourceAudit.setResourceType("Resource");
        createResourceAudit.setStatus("201");
        createResourceAudit.setPrevVersion("");
        createResourceAudit.setAction("Create");
        // fields.put("TIMESTAMP", "2015-11-22 09:19:12.977");
        createResourceAudit.setPrevState("");
        createResourceAudit.setResourceName("MyTestResource");
        // createResourceAudit.setFields(fields);

        final ResourceAdminEvent checkInResourceAudit = new ResourceAdminEvent();
        checkInResourceAudit.setModifier("Carlos Santana(cs0008)");
        checkInResourceAudit.setCurrState("NOT_CERTIFIED_CHECKIN");
        checkInResourceAudit.setCurrVersion("0.1");
        checkInResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
        checkInResourceAudit.setRequestId("ffacbf5d-eeb1-43c6-a310-37fe7e1cc091");
        checkInResourceAudit.setDesc("OK");
        checkInResourceAudit.setComment("Stam");
        checkInResourceAudit.setResourceType("Resource");
        checkInResourceAudit.setStatus("200");
        checkInResourceAudit.setPrevVersion("0.1");
        checkInResourceAudit.setAction("Checkin");
        // fields.put("TIMESTAMP", "2015-11-22 09:25:03.797");
        checkInResourceAudit.setPrevState("NOT_CERTIFIED_CHECKOUT");
        checkInResourceAudit.setResourceName("MyTestResource");

        final ResourceAdminEvent checkOutResourceAudit = new ResourceAdminEvent();
        checkOutResourceAudit.setModifier("Carlos Santana(cs0008)");
        checkOutResourceAudit.setCurrState("NOT_CERTIFIED_CHECKOUT");
        checkOutResourceAudit.setCurrVersion("0.2");
        checkOutResourceAudit.setServiceInstanceId("82eddd99-0bd9-4742-ab0a-1bdb5e262a05");
        checkOutResourceAudit.setRequestId("7add5078-4c16-4d74-9691-cc150e3c96b8");
        checkOutResourceAudit.setDesc("OK");
        checkOutResourceAudit.setComment("");
        checkOutResourceAudit.setResourceType("Resource");
        checkOutResourceAudit.setStatus("200");
        checkOutResourceAudit.setPrevVersion("0.1");
        checkOutResourceAudit.setAction("Checkout");
        // fields.put("TIMESTAMP", "2015-11-22 09:39:41.024");
        checkOutResourceAudit.setPrevState("NOT_CERTIFIED_CHECKIN");
        checkOutResourceAudit.setResourceName("MyTestResource");
        List<ResourceAdminEvent> list = new ArrayList<ResourceAdminEvent>() {
            {
                add(createResourceAudit);
                add(checkInResourceAudit);
                add(checkOutResourceAudit);
            }
        };
        Either<List<ResourceAdminEvent>, ActionStatus> result = Either.left(list);
        Mockito.when(auditingDao.getByServiceInstanceId(Mockito.anyString())).thenReturn(result);

        List<ResourceAdminEvent> listPrev = new ArrayList<>();
        Either<List<ResourceAdminEvent>, ActionStatus> resultPrev = Either.left(listPrev);
        Mockito.when(auditingDao.getAuditByServiceIdAndPrevVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultPrev);

        List<ResourceAdminEvent> listCurr = new ArrayList<ResourceAdminEvent>() {
            {
                add(checkOutResourceAudit);
            }
        };
        Either<List<ResourceAdminEvent>, ActionStatus> resultCurr = Either.left(listCurr);
        Mockito.when(auditingDao.getAuditByServiceIdAndCurrVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(resultCurr);

        Either<List<ResourceAdminEvent>, ActionStatus> archiveAuditList = Either.left(Arrays.asList(auditArchive1, auditArchive2));
        when(auditingDao.getArchiveAuditByServiceInstanceId(anyString())).thenReturn(archiveAuditList);

        Either<List<ResourceAdminEvent>, ActionStatus> restoreAuditList = Either.left(Arrays.asList(auditRestore));
        when(auditingDao.getRestoreAuditByServiceInstanceId(anyString())).thenReturn(restoreAuditList);

    }

    private void setupBeforeDeploy(String notifyAction, String requestAction, String did) {

        DistributionNotificationEvent notifyEvent = new DistributionNotificationEvent();
        notifyEvent.setAction(notifyAction);
        notifyEvent.setDid(did);
        notifyEvent.setStatus("200");

        ResourceAdminEvent requestEvent = new ResourceAdminEvent();
        requestEvent.setAction(requestAction);
        requestEvent.setDid(did);
        requestEvent.setStatus("200");

        List<DistributionNotificationEvent> notifyResults = Collections.singletonList(notifyEvent);
        Either<List<DistributionNotificationEvent>, ActionStatus> eitherNotify = Either.left(notifyResults);

        Mockito.when(auditingDao.getDistributionNotify(Mockito.anyString(), Mockito.eq(notifyAction))).thenReturn(eitherNotify);

        List<ResourceAdminEvent> requestResults = Collections.singletonList(requestEvent);
        Either<List<ResourceAdminEvent>, ActionStatus> eitherRequest = Either.left(requestResults);
        Mockito.when(auditingDao.getDistributionRequest(Mockito.anyString(), Mockito.eq(requestAction))).thenReturn(eitherRequest);

        Either<Component, StorageOperationStatus> eitherService = Either.left(createServiceObject(true));
        Mockito.when(toscaOperationFacade.getToscaElement(Mockito.anyString())).thenReturn(eitherService);

        Either<List<DistributionDeployEvent>, ActionStatus> emptyEventList = Either.left(Collections.emptyList());
        Mockito.when(auditingDao.getDistributionDeployByStatus(Mockito.anyString(), Mockito.eq("DResult"), Mockito.anyString())).thenReturn(emptyEventList);
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

    private Component createNewService() {

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

    private Resource setupGenericServiceMock(){
        Resource genericService = new Resource();
        genericService.setVersion("1.0");
        genericService.setToscaResourceName(GENERIC_SERVICE_NAME);
        return genericService;
    }
}
