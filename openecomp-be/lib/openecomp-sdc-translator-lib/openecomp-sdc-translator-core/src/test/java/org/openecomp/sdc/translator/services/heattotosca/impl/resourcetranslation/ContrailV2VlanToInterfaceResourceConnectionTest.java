/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.HeatResourceUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

public class ContrailV2VlanToInterfaceResourceConnectionTest extends BaseResourceTranslationTest {

  private static final String PORT_NODE_TEMPLATE_ID_FOR_ATTR_TEST = "vdbe_untr_vmi";
  private static final int ONE = 1;
  private static final int TWO = 2;
  private static final String NETWORK_ROLE_INOUT_ATTR_TEST = "untr";
  private static final String NESTED_FILE_NAME_INOUT_ATTR_TEST = "nested.yml";
  private static final String INPUT_FILE_PATH_FOR_INOUT_ATTR_TEST =
      "/mock/services/heattotosca/subInterfaceToInterfaceConnection/inoutattr/inputfiles";
  private static final String INPUT_FILE_PATH_FOR_PORT_NETWORK_ROLE =
      "/mock/services/heattotosca/subInterfaceToInterfaceConnection/portNetworkRole/inputfiles";
  private static final String MAIN_SERVICE_TEMPLATE_YAML = "MainServiceTemplate.yaml";


  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVlanToInterfaceNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVlanToInterfaceNestedInOutAttr() throws Exception {
    inputFilesPath = INPUT_FILE_PATH_FOR_INOUT_ATTR_TEST;
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/inoutattr" +
            "/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();

    Assert.assertNotNull(this.translationContext.getConsolidationData().getPortConsolidationData()
        .getFilePortConsolidationData(MAIN_SERVICE_TEMPLATE_YAML)
        .getPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_FOR_ATTR_TEST));

    PortTemplateConsolidationData portTemplateConsolidationData =
        this.translationContext.getConsolidationData().getPortConsolidationData()
            .getFilePortConsolidationData(MAIN_SERVICE_TEMPLATE_YAML)
            .getPortTemplateConsolidationData(PORT_NODE_TEMPLATE_ID_FOR_ATTR_TEST);
    ListMultimap<String, SubInterfaceTemplateConsolidationData> subInfMap = ArrayListMultimap.create();
    portTemplateConsolidationData.copyMappedInto(subInfMap);
    List<SubInterfaceTemplateConsolidationData> subInfList =
        subInfMap.get("org.openecomp.resource.abstract.nodes.heat.subinterface.nested");

    Assert.assertEquals(ONE, subInfList.size());
    SubInterfaceTemplateConsolidationData data = subInfList.get(0);

    Assert.assertEquals(NETWORK_ROLE_INOUT_ATTR_TEST, data.getNetworkRole());
    Assert.assertEquals(ONE, data.getNodesGetAttrIn().size());
    Assert.assertEquals(ONE, data.getNodesGetAttrOut().size());
    Assert.assertEquals(ONE, data.getOutputParametersGetAttrIn().size());

  }

  @Test
  public void testGetNetworkRoleFromResourceUtil() throws Exception {
    inputFilesPath = INPUT_FILE_PATH_FOR_INOUT_ATTR_TEST;
    initTranslatorAndTranslate();
    Resource targetResource = new Resource();
    targetResource.setType(NESTED_FILE_NAME_INOUT_ATTR_TEST);

    Optional<String> networkRole = HeatToToscaUtil.getNetworkRoleFromSubInterfaceId(targetResource, this
        .translationContext);

    Assert.assertEquals(NETWORK_ROLE_INOUT_ATTR_TEST,networkRole.get());
  }

  @Test
  public void testGetNetworkRoleFromResourceUtil_Port() throws Exception {
    inputFilesPath = INPUT_FILE_PATH_FOR_PORT_NETWORK_ROLE;
    initTranslatorAndTranslate();
    List<String> validNeutronPortTemplateIds = Arrays.asList("vdbe_0_oam_port_1", "vdbe_oam_port", "vdbe_oam_port_2",
        "vdbe_0_int_oam_port_1", "vdbe_int_oam_port", "vdbe_int_oam_port_2");
    validatePortNetworkRole(validNeutronPortTemplateIds, "oam");

    List<String> validVmiPortTemplateIds = Arrays.asList("vdbe_0_untr_vmi_0", "vdbe_untr_vmi");
    validatePortNetworkRole(validVmiPortTemplateIds, "untr");

    List<String> portIdsNotFollowingHeatGuidelines = Arrays.asList("vdbe_0_oam_neutronNotFollowingHeatGuidelines_2",
        "vdbe_0_untr_vmiNotFollowingHeatGuidelines_1");
    validatePortNetworkRole(portIdsNotFollowingHeatGuidelines, null);

  }

  @Test
  public void testSubInterfaceResourceNetworkRolePositive() throws Exception {
    List<String> subInterfaceResourceIds=Arrays.asList("vm_type_11_subint_networkrole_vmi_11",
        "v_subint_networkrole_vmi", "v_1_subint_networkrole_vmi", "v_subint_networkrole_vmi_11",
        "vm_type_subint_networkrole_vmi_11", "vm_type_11_subint_networkrole_vmi",
        "vm_type_subint_networkrole_vmi");

    subInterfaceResourceIds.forEach(resourceId -> {
      Optional<String> networkRole = HeatResourceUtil.extractNetworkRoleFromSubInterfaceId(
        resourceId,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource());
      Assert.assertTrue(networkRole.isPresent()
          && "networkrole".equals(networkRole.get()));
      }
    );
  }

  @Test
  public void testSubInterfaceResourceNetworkRoleNegative() throws Exception {
    List<String> subInterfaceResourceIds=Arrays.asList("vm_type_11_subint_vmi_11",
        "vm_type_11_subint_11_vmi_11");

    subInterfaceResourceIds.forEach(resourceId -> {
        Optional<String> networkRole = HeatResourceUtil.extractNetworkRoleFromSubInterfaceId(
          resourceId,
          HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource());
        Assert.assertFalse(networkRole.isPresent());
      }
    );
  }

  @Test
  public void testIsSubInterfaceResourceUtil() throws Exception {
    inputFilesPath = INPUT_FILE_PATH_FOR_INOUT_ATTR_TEST;
    initTranslatorAndTranslate();
    Resource targetResource = new Resource();
    targetResource.setType(HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource());
    Map<String, Object> propetyMap = new HashMap<>();
    Map<String, Object> resourceDefPropsMap = new HashMap<>();
    resourceDefPropsMap.put("type", NESTED_FILE_NAME_INOUT_ATTR_TEST);
    propetyMap.put(HeatConstants.RESOURCE_DEF_PROPERTY_NAME, resourceDefPropsMap);
    targetResource.setProperties(propetyMap);

    Assert.assertTrue(HeatToToscaUtil.isSubInterfaceResource(targetResource, this
        .translationContext));
  }

  @Test
  public void testTranslateVlanToNetMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels" +
            "/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  private void validatePortNetworkRole(List<String> portNodeTemplateIds, String expectedNetworkRole) {
    FilePortConsolidationData filePortConsolidationData =
        translationContext.getConsolidationData().getPortConsolidationData()
            .getFilePortConsolidationData(MAIN_SERVICE_TEMPLATE_YAML);
    for (String portNodeTemplateId : portNodeTemplateIds) {
      PortTemplateConsolidationData portTemplateConsolidationData =
          filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId);
      Assert.assertEquals(expectedNetworkRole, portTemplateConsolidationData.getNetworkRole());
    }
  }
}
