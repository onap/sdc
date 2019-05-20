package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.upgrade.ServiceInfo;
import org.openecomp.sdc.be.components.upgrade.UpgradeBusinessLogic;
import org.openecomp.sdc.be.components.upgrade.UpgradeRequest;
import org.openecomp.sdc.be.components.upgrade.UpgradeStatus;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.UpgradeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AutomatedUpgradeEndpointTest extends JerseySpringBaseTest {
    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    private static final String RESOURCE_ID_PREV = "prevVF";
    private static final String RESOURCE_ID_NEW = "newVF";
    private static final String SERVICE_ID_PREV = "prevSer";
    private static final String SERVICE_ID_NEW = "newSer";
    private static final String SERVICE_ID_PROXY = "serProxy";
    private static final String SERVICE_ID_PROXY_PREV = "serProxyContainerPrev";
    private static final String SERVICE_ID_PROXY_NEW = "serProxyContainerNew";
    private static final String INVARIANT_ID = "invariantUUID";
    private static final String USER_ID = "userId";

    private static LifecycleBusinessLogic lifecycleBusinessLogic;
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private static UserValidations userValidations;
    private static ToscaOperationFacade toscaOperationFacade;
    private static ComponentsUtils componentsUtils;
    private static UpgradeOperation upgradeOperation;
    private static JanusGraphDao janusGraphDao;

    private static User user;
    private static Resource vfPrev;
    private static Resource vfNew;
    private static Service servicePrev;
    private static Service serviceNew;
    private static Service serviceNewCheckIn;
    private static ComponentInstance istanceNew;

    private static Service serviceProxy;
    private static Service serviceProxyContainerPrev;
    private static Service serviceProxyContainerNew;

    @org.springframework.context.annotation.Configuration
    @Import(BaseTestConfig.class)
    static class AutomatedUpgradeTestConfig {

        @Bean
        AutomatedUpgradeEndpoint automatedUpgradeEndpoint() {
            return new AutomatedUpgradeEndpoint(upgradeBusinessLogic());
        }

        @Bean
        UpgradeBusinessLogic upgradeBusinessLogic() {
            return new UpgradeBusinessLogic(lifecycleBusinessLogic, componentInstanceBusinessLogic, userValidations, toscaOperationFacade, componentsUtils, upgradeOperation,
                janusGraphDao);
        }
    }

    @BeforeClass
    public static void initClass() {
        lifecycleBusinessLogic = mock(LifecycleBusinessLogic.class);
        componentInstanceBusinessLogic = mock(ComponentInstanceBusinessLogic.class);
        userValidations = mock(UserValidations.class);
        toscaOperationFacade = mock(ToscaOperationFacade.class);
        componentsUtils = mock(ComponentsUtils.class);
        upgradeOperation = mock(UpgradeOperation.class);
        janusGraphDao = mock(JanusGraphDao.class);
        user = mock(User.class);
    }

    @Before
    public void init() {
        prepareComponents();
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), anyBoolean())).thenReturn(user);
        when(toscaOperationFacade.getToscaFullElement(eq(RESOURCE_ID_PREV))).thenReturn(Either.left(vfPrev));
        when(toscaOperationFacade.getToscaFullElement(eq(RESOURCE_ID_NEW))).thenReturn(Either.left(vfNew));
        when(toscaOperationFacade.getToscaFullElement(eq(SERVICE_ID_PREV))).thenReturn(Either.left(servicePrev));
        when(toscaOperationFacade.getToscaFullElement(eq(SERVICE_ID_NEW))).thenReturn(Either.left(serviceNew));

        Either<Service, ResponseFormat> fromLifeCycle = Either.left(serviceNew);
        doReturn(fromLifeCycle).when(lifecycleBusinessLogic).changeComponentState(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID_PREV), any(User.class), eq(LifeCycleTransitionEnum.CHECKOUT), any(LifecycleChangeInfoWithAction.class), eq(false),
                eq(true));

        when(toscaOperationFacade.getToscaElement(eq(RESOURCE_ID_PREV), any(ComponentParametersView.class))).thenReturn(Either.left(vfPrev));
        when(componentInstanceBusinessLogic.changeInstanceVersion(any(Service.class), any(ComponentInstance.class), any(ComponentInstance.class), any(User.class), eq(ComponentTypeEnum.SERVICE))).thenReturn(Either.left(istanceNew));

        doReturn(Either.left(serviceNewCheckIn)).when(lifecycleBusinessLogic).changeComponentState(eq(ComponentTypeEnum.SERVICE), eq(SERVICE_ID_NEW), any(User.class), eq(LifeCycleTransitionEnum.CHECKIN), any(LifecycleChangeInfoWithAction.class),
                eq(false), eq(true));

    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
    }

    @Override
    protected ResourceConfig configure() {
        return super.configure(AutomatedUpgradeTestConfig.class).register(AutomatedUpgradeEndpoint.class);
    }

    @Test
    public void upgradeVfInService_success() {
        List<UpgradeRequest> inputsToUpdate = new ArrayList<>();
        UpgradeRequest request = new UpgradeRequest(SERVICE_ID_PREV);
        inputsToUpdate.add(request);

        Invocation.Builder builder = buildAutomatedUpgradeCall(RESOURCE_ID_NEW);
        UpgradeStatus status = builder.post(Entity.entity(inputsToUpdate, MediaType.APPLICATION_JSON), UpgradeStatus.class);

        assertThat(status.getStatus()).isEqualTo(ActionStatus.OK);
        List<ServiceInfo> expected = new ArrayList<>();
        ServiceInfo serviceInfo = new ServiceInfo(serviceNewCheckIn.getUniqueId(), ActionStatus.OK);
        serviceInfo.setName(serviceNewCheckIn.getName());
        serviceInfo.setVersion(serviceNewCheckIn.getVersion());
        expected.add(serviceInfo);
        assertThat(status.getComponentToUpgradeStatus()).hasSameSizeAs(expected);
        assertThat(status.getComponentToUpgradeStatus()).hasSameElementsAs(expected);

    }

    @Test
    public void upgradeVfInService_IdNotExist() {
        List<UpgradeRequest> inputsToUpdate = new ArrayList<>();
        UpgradeRequest request = new UpgradeRequest(SERVICE_ID_PREV);
        inputsToUpdate.add(request);
        String wrongId = "1234";
        when(toscaOperationFacade.getToscaFullElement(eq(wrongId))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.NOT_FOUND))).thenReturn(ActionStatus.RESOURCE_NOT_FOUND);
        String[] variables = { wrongId };
        ServiceException serviceException = new ServiceException("SVC4063", "Error: Requested '%1' resource was not found.", variables);
        ResponseFormat expected = new ResponseFormat(HttpStatus.NOT_FOUND.value());
        expected.setServiceException(serviceException);
        when(componentsUtils.getResponseFormatByResource(eq(ActionStatus.RESOURCE_NOT_FOUND), eq(wrongId))).thenReturn(expected);

        Response response = buildAutomatedUpgradeCall(wrongId).post(Entity.entity(inputsToUpdate, MediaType.APPLICATION_JSON), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

        ResponseFormat actual = response.readEntity(ResponseFormat.class);
        assertThat(actual.getMessageId()).isEqualTo(expected.getMessageId());
        assertThat(actual.getFormattedMessage()).isEqualTo(expected.getFormattedMessage());

    }

    @Test
    public void upgradeVfInService_NotHihgestCertified() {
        List<UpgradeRequest> inputsToUpdate = new ArrayList<>();
        UpgradeRequest request = new UpgradeRequest(RESOURCE_ID_PREV);
        inputsToUpdate.add(request);

        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.NOT_FOUND))).thenReturn(ActionStatus.RESOURCE_NOT_FOUND);
        String[] variables = { vfPrev.getName() };
        ServiceException serviceException = new ServiceException("SVC4699", "Error: Component %1 is not highest certified", variables);
        ResponseFormat expected = new ResponseFormat(HttpStatus.BAD_REQUEST.value());
        expected.setServiceException(serviceException);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.COMPONENT_IS_NOT_HIHGEST_CERTIFIED), eq(vfPrev.getName()))).thenReturn(expected);

        Response response = buildAutomatedUpgradeCall(RESOURCE_ID_PREV).post(Entity.entity(inputsToUpdate, MediaType.APPLICATION_JSON), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ResponseFormat actual = response.readEntity(ResponseFormat.class);
        assertThat(actual.getMessageId()).isEqualTo(expected.getMessageId());
        assertThat(actual.getFormattedMessage()).isEqualTo(expected.getFormattedMessage());
    }

//    @Test
//    public void upgradeProxyInService_successful() {
//        List<UpgradeRequest> inputsToUpdate = new ArrayList<>();
//        UpgradeRequest request = new UpgradeRequest(SERVICE_ID_PROXY_PREV);
//        inputsToUpdate.add(request);
//
//        when(toscaOperationFacade.getToscaElement(eq(SERVICE_ID_PROXY))).thenReturn(Either.left(serviceProxy));
//        
//        UpgradeStatus status = buildAutomatedUpgradeCall(SERVICE_ID_PROXY).post(Entity.entity(inputsToUpdate, MediaType.APPLICATION_JSON), UpgradeStatus.class);
//        assertThat(status.getStatus()).isEqualTo(ActionStatus.OK);
//    }

    private Invocation.Builder buildAutomatedUpgradeCall(String id) {
        return target("/v1/catalog/resources/{id}/automatedupgrade").resolveTemplate("id", id).request(MediaType.APPLICATION_JSON).header(Constants.USER_ID_HEADER, USER_ID);
    }

    private void prepareComponents() {
        createVF();

        createService();
    }

    private void createService() {
        servicePrev = createService("service1", SERVICE_ID_PREV, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        
        ComponentInstance ci = new ComponentInstance();
        ci.setComponentUid(RESOURCE_ID_PREV);
        ci.setName("inst 1");
        List<ComponentInstance> resourceInstances = new ArrayList<>();
        resourceInstances.add(ci);
        servicePrev.setComponentInstances(resourceInstances);

        serviceNew = createService("service1", SERVICE_ID_NEW, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        serviceNew.setComponentInstances(resourceInstances);

        serviceNewCheckIn = createService("service1", SERVICE_ID_NEW, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        
        serviceNewCheckIn.setComponentInstances(resourceInstances);

        istanceNew = new ComponentInstance();
        istanceNew.setComponentUid(RESOURCE_ID_NEW);
        istanceNew.setName("inst 1");

        serviceProxy = createService("serviceProxy", SERVICE_ID_PROXY, LifecycleStateEnum.CERTIFIED);
        serviceProxyContainerPrev = createService("serviceProxyContainer", SERVICE_ID_PROXY_PREV, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        serviceProxyContainerNew = createService("serviceProxyContainer", SERVICE_ID_PROXY_NEW, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
       
        

    }

    private Service createService(String name, String id, LifecycleStateEnum state){
        Service service = new Service();
        service.setName(name);
        service.setUniqueId(id);
        service.setLifecycleState(state);
        service.setHighestVersion(true);
        return service;
    }
    private void createVF() {
        vfPrev = new Resource();
        vfPrev.setName("vf1");
        vfPrev.setUniqueId(RESOURCE_ID_PREV);
        vfPrev.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        vfPrev.setHighestVersion(false);
        vfPrev.setResourceType(ResourceTypeEnum.VF);
        vfPrev.setVersion("1.0");
        vfPrev.setInvariantUUID(INVARIANT_ID);
        vfPrev.setComponentType(ComponentTypeEnum.RESOURCE);

        vfNew = new Resource();
        vfNew.setName("vf1");
        vfNew.setUniqueId(RESOURCE_ID_PREV);
        vfNew.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        vfNew.setHighestVersion(true);
        vfNew.setResourceType(ResourceTypeEnum.VF);
        vfNew.setVersion("2.0");
        vfNew.setInvariantUUID(INVARIANT_ID);
        vfNew.setComponentType(ComponentTypeEnum.RESOURCE);

    }
}
