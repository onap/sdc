package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.OperationalEnvironmentBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DistributionEngineTest {

    public static final String DISTRIBUTION_ID = "distId";
    public static final String ENV_ID = "envId";
    public static final String USER_ID = "userId";
    public static final String MODIFIER = "modifier";

    @InjectMocks
    private DistributionEngine testInstance;

    @Mock
    private EnvironmentsEngine environmentsEngine;

    @Mock
    private DistributionNotificationSender distributionNotificationSender;

    private DummyDistributionConfigurationManager distributionEngineConfigurationMock;

    private Map<String, OperationalEnvironmentEntry> envs;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        distributionEngineConfigurationMock = new DummyDistributionConfigurationManager();
        envs = getEnvs(ENV_ID);
    }

    @Test
    public void notifyService() throws Exception {
        NotificationDataImpl notificationData = new NotificationDataImpl();
        Service service = new Service();
        when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
        when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName()).thenReturn("topic");
        when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID), any(EnvironmentMessageBusData.class),
                any(NotificationDataImpl.class), any(Service.class), eq(USER_ID), eq(MODIFIER)))
        .thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID, USER_ID, MODIFIER);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void notifyService_couldNotResolveEnvironment() throws Exception {
        when(environmentsEngine.getEnvironments()).thenReturn(envs);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, new Service(), new NotificationDataImpl(), "someNonExisitngEnv", USER_ID, MODIFIER);
        assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, actionStatus);
        verifyZeroInteractions(distributionNotificationSender);
    }

    @Test
    public void notifyService_failedWhileSendingNotification() throws Exception {
        NotificationDataImpl notificationData = new NotificationDataImpl();
        Service service = new Service();
        when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
        when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName()).thenReturn("topic");
        when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID), any(EnvironmentMessageBusData.class),
                any(NotificationDataImpl.class), any(Service.class), eq(USER_ID), eq(MODIFIER)))
                .thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID, USER_ID, MODIFIER);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }

    private Map<String, OperationalEnvironmentEntry> getEnvs(String ... environmentIds) {
        Set<String> uebAddress = new HashSet<>();
        uebAddress.add("someAddress");
        return Stream.of(environmentIds)
                .map(id -> new OperationalEnvironmentBuilder().setEnvId(id).setDmaapUebAddress(uebAddress).build())
                .collect(Collectors.toMap(OperationalEnvironmentEntry::getEnvironmentId, Function.identity()));
    }
}