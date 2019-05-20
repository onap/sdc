package org.openecomp.sdc.be.components.merge.instance;

import com.google.common.collect.ImmutableMap;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.merge.capability.SimpleCapabilityResolver;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.utils.CapabilityDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ComponentInstancePropertyBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceCapabilitiesMergeBLTest {

    private static final String INSTANCE1 = "inst1";
    private static final String INSTANCE2 = "inst2";

    private ComponentCapabilitiesPropertiesMergeBL testInstance;
    private ComponentsUtils componentsUtils;
    @Mock
    private SimpleCapabilityResolver capabilityResolver;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private DataDefinitionsValuesMergingBusinessLogic dataDefinitionsValuesMergingBusinessLogic;
    @Captor
    private ArgumentCaptor<List<ComponentInstanceProperty>> prevCapPropertiesCaptor;
    @Captor
    private ArgumentCaptor<List<ComponentInstanceProperty>> currCapPropertiesCaptor;
    @Captor
    private ArgumentCaptor<ComponentParametersView> getCurrResourceFilterCapture;

    private CapabilityDefinition oldCap;
    private CapabilityDefinition oldCap2;
    private CapabilityDefinition newCap;
    private CapabilityDefinition newCap2;

    private ComponentParametersView getCapabilitiesPropsFilter;

    protected static ConfigurationManager configurationManager = null;

    @Before
    public void setUp() throws Exception {
        oldCap = new CapabilityDefinitionBuilder()
                .setOwnerId(INSTANCE1)
                .setOwnerName(INSTANCE1)
                .setId("cap1")
                .setType("type1")
                .setName("cap1")
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop1").build())
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop2").build())
                .build();

        oldCap2 = new CapabilityDefinitionBuilder()
                .setOwnerId(INSTANCE2)
                .setOwnerName(INSTANCE2)
                .setId("cap2")
                .setType("type1")
                .setName("cap2")
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop1").build())
                .build();

        newCap = new CapabilityDefinitionBuilder()
                .setOwnerId(INSTANCE1)
                .setOwnerName(INSTANCE1)
                .setId("cap1")
                .setType("type1")
                .setName("cap1")
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop1").build())
                .build();

        newCap2 = new CapabilityDefinitionBuilder()
                .setOwnerId(INSTANCE2)
                .setOwnerName(INSTANCE2)
                .setId("cap2")
                .setType("type1")
                .setName("cap2")
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop2").build())
                .addProperty(new ComponentInstancePropertyBuilder().setName("prop3").build())
                .build();

        getCapabilitiesPropsFilter = new ComponentParametersView();
        getCapabilitiesPropsFilter.disableAll();
        getCapabilitiesPropsFilter.setIgnoreComponentInstances(false);
        getCapabilitiesPropsFilter.setIgnoreCapabilities(false);
        getCapabilitiesPropsFilter.setIgnoreCapabiltyProperties(false);

        ExternalConfiguration.setAppName("catalog-be");
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
        testInstance = new ComponentCapabilitiesPropertiesMergeBL(dataDefinitionsValuesMergingBusinessLogic, toscaOperationFacade, componentsUtils, capabilityResolver);
    }

    @Test
    public void mergeCapabilityProperties_singleCapability() {
        ComponentInstance prevInst1 = new ComponentInstanceBuilder().addCapability(oldCap).setName(oldCap.getOwnerName()).build();
        ComponentInstance newInst1 = new ComponentInstanceBuilder().addCapability(newCap).setName(newCap.getOwnerName()).build();
        Resource prevResource = new ResourceBuilder().addComponentInstance(prevInst1).build();
        Resource newResource = new ResourceBuilder().addComponentInstance(newInst1).build();
        when(toscaOperationFacade.getToscaElement(eq(newResource.getUniqueId()), getCurrResourceFilterCapture.capture())).thenReturn(Either.left(newResource));
        when(capabilityResolver.resolvePrevCapIdToNewCapability(prevInst1, newInst1)).thenReturn(ImmutableMap.of(oldCap, newCap));
        when(toscaOperationFacade.updateComponentCalculatedCapabilitiesProperties(newResource)).thenReturn(StorageOperationStatus.OK);

        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);

        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verify(dataDefinitionsValuesMergingBusinessLogic).mergeInstanceDataDefinitions(prevCapPropertiesCaptor.capture(), currCapPropertiesCaptor.capture());
        assertThat(prevCapPropertiesCaptor.getValue()).extracting("name").containsExactly("prop1", "prop2");
        assertThat(currCapPropertiesCaptor.getValue()).extracting("name").containsExactly("prop1");
        assertThat(getCurrResourceFilterCapture.getValue()).isEqualToComparingFieldByField(getCapabilitiesPropsFilter);
    }

    @Test
    public void mergeCapabilityProperties_failToGetResourceWithPropsCapabilities() {
        Resource currResource = new ResourceBuilder().setUniqueId("rid").build();
        when(toscaOperationFacade.getToscaElement(eq(currResource.getUniqueId()), any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        ActionStatus actionStatus = testInstance.mergeComponents(new Resource(), currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    @Test
    public void mergeCapabilityProperties_multipleCapabilitiesToMerge() {
        ComponentInstance prevInst1 = new ComponentInstanceBuilder().addCapability(oldCap).setName(oldCap.getOwnerName()).build();
        ComponentInstance prevInst2 = new ComponentInstanceBuilder().addCapability(oldCap2).setName(oldCap2.getOwnerName()).build();

        ComponentInstance currInst1 = new ComponentInstanceBuilder().addCapability(newCap).setName(newCap.getOwnerName()).build();
        ComponentInstance currInst2 = new ComponentInstanceBuilder().addCapability(newCap2).setName(newCap2.getOwnerName()).build();

        Resource currResource = new ResourceBuilder().addComponentInstance(currInst1).addComponentInstance(currInst2).build();
        Resource prevResource = new ResourceBuilder().addComponentInstance(prevInst1).addComponentInstance(prevInst2).build();
        when(toscaOperationFacade.getToscaElement(eq(currResource.getUniqueId()), any(ComponentParametersView.class))).thenReturn(Either.left(currResource));
        when(capabilityResolver.resolvePrevCapIdToNewCapability(prevInst1, currInst1)).thenReturn(ImmutableMap.of(oldCap, newCap));
        when(capabilityResolver.resolvePrevCapIdToNewCapability(prevInst2, currInst2)).thenReturn(ImmutableMap.of(oldCap2, newCap2));
        when(toscaOperationFacade.updateComponentCalculatedCapabilitiesProperties(currResource)).thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verify(dataDefinitionsValuesMergingBusinessLogic, times(2)).mergeInstanceDataDefinitions(anyList(), anyList());
    }

    @Test
    public void mergeCapabilityProperties_noNewCapabilitiesResolved() {
        ComponentInstance prevInst1 = new ComponentInstanceBuilder().addCapability(oldCap).setName(oldCap.getOwnerName()).build();
        ComponentInstance newInst1 = new ComponentInstanceBuilder().setName(oldCap.getOwnerName()).build();
        Resource prevResource = new ResourceBuilder().addComponentInstance(prevInst1).build();
        Resource newResource = new ResourceBuilder().addComponentInstance(newInst1).build();
        when(toscaOperationFacade.getToscaElement(eq(newResource.getUniqueId()), any(ComponentParametersView.class))).thenReturn(Either.left(newResource));
        when(capabilityResolver.resolvePrevCapIdToNewCapability(prevInst1, newInst1)).thenReturn(emptyMap());
        when(toscaOperationFacade.updateComponentCalculatedCapabilitiesProperties(newResource)).thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(dataDefinitionsValuesMergingBusinessLogic);
    }

    @Test
    public void mergeCapabilityProperties_noPrevCapabilitiesToMerge() {
        Resource newResource = new Resource();
        Resource prevResource = new Resource();
        when(toscaOperationFacade.getToscaElement(eq(newResource.getUniqueId()), any(ComponentParametersView.class))).thenReturn(Either.left(newResource));
        verifyNoMoreInteractions(toscaOperationFacade);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(dataDefinitionsValuesMergingBusinessLogic);
    }

    @Test
    public void mergeCapabilityProperties_failedToUpdateComponent() {
        ComponentInstance inst1 = new ComponentInstanceBuilder().addCapability(oldCap).setName(oldCap.getOwnerName()).build();
        Resource newResource = new Resource();
        Resource prevResource = new ResourceBuilder().addComponentInstance(inst1).build();
        when(toscaOperationFacade.getToscaElement(eq(newResource.getUniqueId()), any(ComponentParametersView.class))).thenReturn(Either.left(newResource));
        when(toscaOperationFacade.updateComponentCalculatedCapabilitiesProperties(newResource)).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    ////////////////////////////
    @Test
    public void mergeInstanceCapabilityProperties_singleCap() {
        List<CapabilityDefinition> previousInstanceCapabilities = Collections.singletonList(oldCap);
        ComponentInstance inst1 = new ComponentInstanceBuilder().addCapability(newCap).setId(newCap.getOwnerId()).build();
        Resource container = new ResourceBuilder().addComponentInstance(inst1).build();
        Resource origInstanceNode = new Resource();
        when(toscaOperationFacade.updateComponentInstanceCapabilityProperties(container, newCap.getOwnerId())).thenReturn(StorageOperationStatus.OK);
        when(capabilityResolver.resolvePrevCapToNewCapability(container, origInstanceNode, INSTANCE1, previousInstanceCapabilities)).thenReturn(ImmutableMap.of(oldCap, newCap));
        ActionStatus actionStatus = testInstance.mergeComponentInstanceCapabilities(container, origInstanceNode, INSTANCE1, previousInstanceCapabilities);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verify(dataDefinitionsValuesMergingBusinessLogic).mergeInstanceDataDefinitions(prevCapPropertiesCaptor.capture(), currCapPropertiesCaptor.capture());

        assertThat(prevCapPropertiesCaptor.getValue()).extracting("name").containsExactly("prop1", "prop2");
        assertThat(currCapPropertiesCaptor.getValue()).extracting("name").containsExactly("prop1");
    }

    @Test
    public void mergeInstanceCapabilityProperties() {
        List<CapabilityDefinition> previousInstanceCapabilities = Arrays.asList(oldCap, oldCap2);
        ComponentInstance inst1 = new ComponentInstanceBuilder().addCapability(newCap).addCapability(newCap2).setId(newCap.getOwnerId()).build();
        Resource container = new ResourceBuilder().addComponentInstance(inst1).build();
        Resource origInstanceNode = new Resource();
        when(capabilityResolver.resolvePrevCapToNewCapability(container, origInstanceNode, INSTANCE1, previousInstanceCapabilities)).thenReturn(ImmutableMap.of(oldCap, newCap, oldCap2, newCap2));
        when(toscaOperationFacade.updateComponentInstanceCapabilityProperties(container, INSTANCE1)).thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceCapabilities(container, origInstanceNode, INSTANCE1, previousInstanceCapabilities);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verify(dataDefinitionsValuesMergingBusinessLogic, times(2)).mergeInstanceDataDefinitions(anyList(), anyList());
    }

    @Test
    public void mergeInstanceCapabilityProperties_emptyCapabilitiesList() {
        ActionStatus actionStatus = testInstance.mergeComponentInstanceCapabilities(new Resource(), new Resource(),  "instanceId", Collections.emptyList());
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(toscaOperationFacade, dataDefinitionsValuesMergingBusinessLogic, capabilityResolver);
    }

    @Test
    public void mergeInstanceCapabilityProperties_failedToUpdateComponent() {
        Resource container = new Resource();
        Resource resource = new Resource();
        List<CapabilityDefinition> capList = singletonList(oldCap);
        when(capabilityResolver.resolvePrevCapToNewCapability(container, resource, INSTANCE1, capList)).thenReturn(ImmutableMap.of(oldCap, newCap));
        when(toscaOperationFacade.updateComponentInstanceCapabilityProperties(container, INSTANCE1)).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceCapabilities(container, resource, INSTANCE1, capList);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void mergeInstanceCapabilityProperties_noNewCapabilitiesFound() {
        Resource container = new Resource();
        Resource resource = new Resource();
        List<CapabilityDefinition> prevCapabilities = singletonList(oldCap);
        when(capabilityResolver.resolvePrevCapToNewCapability(container, resource, INSTANCE1, prevCapabilities)).thenReturn(emptyMap());
        when(toscaOperationFacade.updateComponentInstanceCapabilityProperties(container, INSTANCE1)).thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceCapabilities(container, resource, INSTANCE1, prevCapabilities);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(dataDefinitionsValuesMergingBusinessLogic);
    }
}