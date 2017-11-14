package org.openecomp.sdc.healing.healers;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.healers.util.TestUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;

public class ForwarderCapabilityHealerTest {
  private static final String IN_SUFFIX = "/in";
  private static final String OUT_SUFFIX = "/out";
  private static final String BASE_DIRECTORY = "/mock/healers/forwarder";
  private static final String ENTRY_DEFINITION_SERVICE_TEMPLATE = "MainServiceTemplate.yaml";
  private static TestFeatureManager manager;

  private Map<String,Object> params = new HashMap<>();

  @Mock
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  @Mock
  private ToscaAnalyzerService toscaAnalyzerService;
  @InjectMocks
  private ForwarderCapabilityHealer forwarderCapabilityHealer;

  @BeforeClass
  public static void enableForwarderFeature(){
    manager = new TestFeatureManager(ToggleableFeature.class);
    if (!ToggleableFeature.FORWARDER_CAPABILITY.isActive()) {
      manager.enable(ToggleableFeature.FORWARDER_CAPABILITY);
    }
  }

  @AfterClass
  public static void disableForwarderFeature() {
    manager.disable(ToggleableFeature.FORWARDER_CAPABILITY);
    manager = null;
    TestFeatureManagerProvider.setFeatureManager(null);
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(ForwarderCapabilityHealerTest.this);
    params.put(SdcCommon.VSP_ID,"1");
    params.put(SdcCommon.VERSION, new Version(1,1));
  }


  @Test
  public void testHealingSubstitutionMappingsNeutronPort() throws Exception {
    testForwarderHealer(
        "/testSubMappingNeutronPort", "org.openecomp.resource.cp.nodes.heat.network.neutron.Port", true);
  }

  @Test
  public void testHealingSubstitutionMappingsContrailPort() throws Exception {
    testForwarderHealer(
        "/testSubMappingContrailPort", "org.openecomp.resource.cp.nodes.heat.network.contrail.Port", true);
  }

  @Test
  public void testHealingSubstitutionMappingsExtNeutronPort() throws Exception {
    testForwarderHealer(
        "/testSubMappingExtNeutronPort", "org.openecomp.resource.cp.v2.extNeutronCP", true);
  }

  @Test
  public void testHealingSubstitutionMappingsExtContrailPort() throws Exception {
    testForwarderHealer(
        "/testSubMappingExtContrailPort", "org.openecomp.resource.cp.v2.extContrailCP", true);
  }

  @Test
  public void testHealingGlobalServiceTemplates () throws Exception {
    testForwarderHealer("/testGlobalServiceTemplates", null, false);
  }

  @Test
  public void testHealingNoPorts() throws Exception {
    testForwarderHealer("/testNoPorts", null, false);
  }

  private void testForwarderHealer(String testDirectory,
                                   String portType,
                                   boolean needToTestSubMapping) throws Exception {

    ToscaServiceModel toscaServiceModel = TestUtil.loadToscaServiceModel(
        BASE_DIRECTORY + testDirectory + IN_SUFFIX, null, ENTRY_DEFINITION_SERVICE_TEMPLATE);

    Mockito.doReturn(toscaServiceModel)
        .when(serviceModelDao).getServiceModel(any(), any());

    if(needToTestSubMapping) {
      Mockito.doReturn(true)
          .when(toscaAnalyzerService).isTypeOf(
          eq(getMockPortNodeTemplate(portType)),
          eq(ToscaNodeType.NATIVE_NETWORK_PORT),
          anyObject(), anyObject());
    }

    validateServiceModelAfterHealing(testDirectory);
  }

  private void validateServiceModelAfterHealing(String path) throws Exception {
    Optional<ToscaServiceModel> serviceModelObject =
        (Optional<ToscaServiceModel>) forwarderCapabilityHealer.heal(params);

    Assert.assertTrue(serviceModelObject.isPresent());
    TestUtil
        .compareToscaServiceModels(
            BASE_DIRECTORY + path + OUT_SUFFIX, serviceModelObject.get());
  }

  private NodeTemplate getMockPortNodeTemplate(String portType) {
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(portType);

    Map<String, Object> properties = new HashMap<>();
    properties.put("exCP_naming", "port_pd01_port_exCP_naming");
    nodeTemplate.setProperties(properties);

    return nodeTemplate;
  }

}
