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
import org.junit.BeforeClass;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.version.VesionUpdateHandler;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.components.utils.ComponentBusinessLogicMock;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LifecycleTestBase extends ComponentBusinessLogicMock {
    private static final Logger log = LoggerFactory.getLogger(LifecycleTestBase.class);
    @InjectMocks
    protected final ServletContext servletContext = Mockito.mock(ServletContext.class);
    protected UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
    protected WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    protected WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
    protected ToscaElementLifecycleOperation toscaElementLifecycleOperation = Mockito.mock(ToscaElementLifecycleOperation.class);
    protected VesionUpdateHandler vesionUpdateHandler = Mockito.mock(VesionUpdateHandler.class);
    protected ArtifactsBusinessLogic artifactsManager = Mockito.mock(ArtifactsBusinessLogic.class);;
    protected User user = null;
    protected Resource resourceResponse;
    protected Service serviceResponse;
    protected ResponseFormatManager responseManager = null;
    protected JanusGraphDao janusGraphDao = Mockito.mock(JanusGraphDao.class);
    protected ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    protected static ComponentsUtils componentsUtils;
    protected final IDistributionEngine distributionEngine = Mockito.mock(IDistributionEngine.class);
    protected final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    protected final ServiceDistributionValidation serviceDistributionValidation = Mockito.mock(ServiceDistributionValidation.class);
    protected final ForwardingPathValidator forwardingPathValidator = Mockito.mock(ForwardingPathValidator.class);
    protected final UiComponentDataConverter uiComponentDataConverter = Mockito.mock(UiComponentDataConverter.class);
    protected final NodeFilterOperation serviceFilterOperation = Mockito.mock(NodeFilterOperation.class);
    protected final NodeFilterValidator serviceFilterValidator = Mockito.mock(NodeFilterValidator.class);

    @BeforeClass
    public static void setupClass() {
        ExternalConfiguration.setAppName("catalog-be");
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
    }

    public void setup() {

        // User data and management
        user = new User();
        user.setUserId("jh003");
        user.setFirstName("Jimmi");
        user.setLastName("Hendrix");
        user.setRole(Role.ADMIN.name());

        when(mockUserAdmin.getUser("jh003", false)).thenReturn(user);
        // Servlet Context attributes
        when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);
        when(webAppContext.getBean(ToscaElementLifecycleOperation.class)).thenReturn(toscaElementLifecycleOperation);
        when(webAppContext.getBean(ArtifactsBusinessLogic.class)).thenReturn(artifactsManager);

        // Resource Operation mock methods
        // getCount

        // createResource
        resourceResponse = createResourceObject();
        Either<ToscaElement, StorageOperationStatus> eitherComponent = Either.left(ModelConverter.convertToToscaElement(resourceResponse));
        when(toscaElementLifecycleOperation.checkoutToscaElement(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenAnswer(createAnswer(eitherComponent));

        when(toscaElementLifecycleOperation.checkinToscaELement(Mockito.any(LifecycleStateEnum.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenAnswer(createAnswer(eitherComponent));

        Either<User, StorageOperationStatus> getOwnerResult = Either.left(user);
        when(toscaElementLifecycleOperation.getToscaElementOwner(Mockito.anyString())).thenReturn(getOwnerResult);

        Either<Component, StorageOperationStatus> eitherlatestDerived = Either.right(StorageOperationStatus.OK);
        when(toscaOperationFacade.shouldUpgradeToLatestDerived(Mockito.any(Resource.class))).thenReturn(eitherlatestDerived);

        responseManager = ResponseFormatManager.getInstance();

    }

    public static <T> Answer<T> createAnswer(final T value) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                return value;
            }

        };
    }

    protected Resource createResourceObject() {
        return createResourceObject(ComponentTypeEnum.RESOURCE, "uid");
    }

    protected Resource createResourceObject(String uid) {
        return createResourceObject(ComponentTypeEnum.RESOURCE, uid);
    }

    protected Resource createResourceObject(ComponentTypeEnum componentType, String uid) {
        Resource resource = new Resource();
        resource.setUniqueId(uid);
        resource.setComponentType(componentType);
        resource.setName("MyResourceName");
        resource.addCategory("VoIP", "INfra");
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        resource.setTags(tgs);
        List<String> template = new ArrayList<>();
        template.add("Root");
        resource.setDerivedFrom(template);
        resource.setVendorName("Motorola");
        resource.setVendorRelease("1.0.0");
        resource.setContactId("yavivi");
        resource.setIcon("MyIcon.jpg");
        resource.setToscaType(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());

        return resource;
    }

    protected Resource createResourceVFCMTObject() {
        ResourceMetadataDataDefinition rMetadataDataDefinition = new ResourceMetadataDataDefinition();
        rMetadataDataDefinition.setResourceType(ResourceTypeEnum.VFCMT);
        ComponentMetadataDefinition cMetadataDataDefinition = new ComponentMetadataDefinition(rMetadataDataDefinition) ;

        Resource resource = new Resource(cMetadataDataDefinition);
        resource.setUniqueId("rid");
        resource.setName("MyResourceVFCMTName");
        resource.addCategory("VoIP", "INfra");
        resource.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test1");
        resource.setTags(tgs);
        List<String> template = new ArrayList<>();
        template.add("Root");
        resource.setDerivedFrom(template);
        resource.setVendorName("Motorola");
        resource.setVendorRelease("1.0.0");
        resource.setContactId("yavivi");
        resource.setIcon("MyIcon.jpg");
        resource.setToscaType(ToscaElementTypeEnum.NODE_TYPE.getValue());

        return resource;
    }
    protected Service createServiceObject() {
        return createServiceObject("sid");
    }

    protected Service createServiceObject(String uid) {
        Service service = new Service();
        service.setName("MyServiceName");
        service.setUniqueId(uid);
        service.addCategory("VoIP", null);
        service.setDescription("My short description");
        List<String> tgs = new ArrayList<>();
        tgs.add("test");
        service.setTags(tgs);
        List<String> template = new ArrayList<>();
        template.add("Root");
        service.setContactId("aa0001");
        service.setIcon("MyIcon.jpg");

        return service;
    }

    protected void assertResponse(Either<? extends Component, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        ResponseFormat actualResponse = createResponse.right().value();
        assertThat(expectedResponse.getMessageId()).isEqualTo(actualResponse.getMessageId());
    }

    protected void assertServiceResponse(Either<Service, ResponseFormat> createResponse, ActionStatus expectedStatus, String... variables) {
        ResponseFormat expectedResponse = responseManager.getResponseFormat(expectedStatus, variables);
        ResponseFormat actualResponse = createResponse.right().value();
        assertThat(expectedResponse.getMessageId()).isEqualTo(actualResponse.getMessageId());
    }

    protected static ArtifactDefinition getArtifactPlaceHolder(String resourceId, String logicalName) {
        ArtifactDefinition artifact = new ArtifactDefinition();

        artifact.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, logicalName.toLowerCase()));
        artifact.setArtifactLabel(logicalName.toLowerCase());

        return artifact;
    }
}
