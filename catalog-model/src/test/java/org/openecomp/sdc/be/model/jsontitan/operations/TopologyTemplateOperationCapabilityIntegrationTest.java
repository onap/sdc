package org.openecomp.sdc.be.model.jsontitan.operations;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.TitanSpringConfig;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingTitanDao;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TitanSpringConfig.class, ModelOperationsSpringConfig.class})
public class TopologyTemplateOperationCapabilityIntegrationTest extends ModelTestBase {

    private static final String CONTAINER_ID = "id";
    private Map<String, MapCapabilityProperty> capabilitiesPropsMap;
    private List<CapabilityPropertyDataObject> capabilitiesProperties;

    @Resource
    private TopologyTemplateOperation topologyTemplateOperation;

    @Resource
    private HealingTitanDao titanDao;
    private CapabilityPropertyDataObject capabilityProperty1;
    private CapabilityPropertyDataObject capabilityProperty2;
    private CapabilityPropertyDataObject capabilityProperty3;

    @BeforeClass
    public static void setupBeforeClass() {

        ModelTestBase.init();
    }

    @Before
    public void setUp() throws Exception {

        capabilitiesPropsMap = new HashMap<>();
        capabilityProperty1 = new CapabilityPropertyDataObject("instance1", "capability1", "prop1", "val1");
        capabilityProperty2 = new CapabilityPropertyDataObject("instance1", "capability2", "prop2", "val2");
        capabilityProperty3 = new CapabilityPropertyDataObject("instance2", "capability3", "prop3", "val3");
        capabilitiesProperties = Arrays.asList(capabilityProperty1, capabilityProperty2, capabilityProperty3);


        //capablities props == Map<instance id, Map<capability id, Map<prop id, property>>>
        capabilitiesProperties.forEach(capabilitiesProperty -> {
            capabilitiesPropsMap.computeIfAbsent(capabilitiesProperty.getInstanceId(), k -> new MapCapabilityProperty(new HashMap<>()))
                             .getMapToscaDataDefinition().computeIfAbsent(capabilitiesProperty.getCapabilityId(), k -> new MapPropertiesDataDefinition(new HashMap<>()))
                             .getMapToscaDataDefinition().computeIfAbsent(capabilitiesProperty.getPropName(), k -> new PropertyDefinition(createPropWithValue(capabilitiesProperty.getPropValue())));
        });

        GraphVertex resource = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        resource.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, CONTAINER_ID);
        titanDao.createVertex(resource);
        GraphVertex loadedResource = titanDao.getVertexById(CONTAINER_ID).left().value();
        topologyTemplateOperation.associateElementToData(loadedResource, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, capabilitiesPropsMap).left().value();
    }

    @After
    public void tearDown() {
        titanDao.rollback();
    }

    @Test
    public void overrideCalculatedCapabilityProperties() {
        Map<String, MapCapabilityProperty> loadedCapPropsMap = fetchCapabilitiesProps(CONTAINER_ID);
        compareCapabilitiesProperties(capabilitiesProperties, loadedCapPropsMap);
        capabilityProperty1.setPropValue("newVal1");
        capabilityProperty3.setPropValue("newVal3");
        setPropertyValue(capabilitiesPropsMap, capabilityProperty1);
        setPropertyValue(capabilitiesPropsMap, capabilityProperty3);
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, capabilitiesPropsMap);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        Map<String, MapCapabilityProperty> updatedCapPropsMap = fetchCapabilitiesProps(CONTAINER_ID);
        compareCapabilitiesProperties(capabilitiesProperties, updatedCapPropsMap);
    }

    @Test
    public void updateToscaDataDeepElementsBlockToToscaElement() {
        assertCapabilityPropValue(capabilityProperty1, "val1");
        assertCapabilityPropValue(capabilityProperty2, "val2");
        assertCapabilityPropValue(capabilityProperty3, "val3");

        MapCapabilityProperty instance1Props = capabilitiesPropsMap.get("instance1");
        capabilityProperty1.setPropValue("newVal1");
        setPropertyValue(capabilitiesPropsMap, capabilityProperty1);

        capabilityProperty3.setPropValue("newVal3");
        setPropertyValue(capabilitiesPropsMap, capabilityProperty3);

        Component component = new org.openecomp.sdc.be.model.Resource();
        component.setUniqueId(CONTAINER_ID);
        StorageOperationStatus updateStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, instance1Props, "instance1");

        assertThat(updateStatus).isEqualTo(StorageOperationStatus.OK);
        assertCapabilityPropValue(capabilityProperty1, "newVal1");
        assertCapabilityPropValue(capabilityProperty2, "val2");
        assertCapabilityPropValue(capabilityProperty3, "val3");//only instance1 props should be updated
    }

    private Map<String, MapCapabilityProperty> fetchCapabilitiesProps(String containerId) {
        ComponentParametersView capabilityPropsFilter = new ComponentParametersView(true);
        capabilityPropsFilter.setIgnoreCapabiltyProperties(false);
        return ((TopologyTemplate) topologyTemplateOperation.getToscaElement(containerId, capabilityPropsFilter).left().value()).getCalculatedCapabilitiesProperties();
    }

    private void compareCapabilitiesProperties(List<CapabilityPropertyDataObject> expected, Map<String, MapCapabilityProperty> actual) {
        expected.forEach(expectedCapabilityProp -> {
            assertThat(getPropertyValue(actual, expectedCapabilityProp.instanceId, expectedCapabilityProp.capabilityId, expectedCapabilityProp.propName))
                    .isEqualTo(expectedCapabilityProp.propValue);
        });
    }

    private String getPropertyValue(Map<String, MapCapabilityProperty> capabilityPropertyMap, String instance, String capability, String prop) {
        return capabilityPropertyMap.get(instance).getMapToscaDataDefinition().get(capability).getMapToscaDataDefinition().get(prop).getValue();
    }

    private void setPropertyValue(Map<String, MapCapabilityProperty> capabilityPropertyMap, CapabilityPropertyDataObject capabilityProperty) {
        setPropertyValue(capabilityPropertyMap.get(capabilityProperty.getInstanceId()), capabilityProperty);

    }

    private void setPropertyValue(MapCapabilityProperty capabilitiesInstanceProperties, CapabilityPropertyDataObject capabilityProperty) {
        capabilitiesInstanceProperties.getMapToscaDataDefinition().get(capabilityProperty.getCapabilityId())
                                      .getMapToscaDataDefinition().get(capabilityProperty.getPropName())
                                      .setValue(capabilityProperty.getPropValue());
    }

    private void assertCapabilityPropValue(CapabilityPropertyDataObject prop, String expectedValue) {
        Map<String, MapCapabilityProperty> loadedCapPropsMap = fetchCapabilitiesProps(CONTAINER_ID);
        String propertyValue = getPropertyValue(loadedCapPropsMap, prop.getInstanceId(), prop.getCapabilityId(), prop.getPropName());
        assertThat(propertyValue).isEqualTo(expectedValue);
    }


    private PropertyDefinition createPropWithValue(String val) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setValue(val);
        return propertyDefinition;
    }

    private static class CapabilityPropertyDataObject {
        private String instanceId;
        private String capabilityId;
        private String propName;
        private String propValue;

        CapabilityPropertyDataObject(String instanceId, String capabilityId, String propName, String propValue) {
            this.instanceId = instanceId;
            this.capabilityId = capabilityId;
            this.propName = propName;
            this.propValue = propValue;
        }

        String getInstanceId() {
            return instanceId;
        }

        String getCapabilityId() {
            return capabilityId;
        }

        String getPropName() {
            return propName;
        }

        String getPropValue() {
            return propValue;
        }

        void setPropValue(String propValue) {
            this.propValue = propValue;
        }
    }
}
