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

package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionSubInterfaceFullTest extends BaseFullTranslationTest {

  private static final String PATTERN_1A_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern1a/";
  private static final String PATTERN_1B_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern1b/";
  private static final String PATTERN_1C1_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern1c1/";
  private static final String PATTERN_1C2_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern1c2/";
  private static final String PATTERN_4_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern4/";
  private static final String PATTERN_5_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/pattern5/";
  private static final String VFC_INSTANCE_GROUP_BASE_DIRECTORY =
      "/mock/services/heattotosca/fulltest/subinterface/vlantagging/vfcInstanceGroup/";

  @Test
  public void testSubInterfaceComputePortNetworkWithGroup() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with only port
     * connected to network
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "computePortNetworkSubInterface");
  }

  @Test
  public void testSubInterfaceNodesConnectedOut() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to same network
     * 3. Sub-interface has depends on (out) connectivity with network resource
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "subInterfaceNodesConnectedOut");
  }

  @Test
  public void testSubInterfaceNodesConnectedIn() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to different network
     * 3. Sub-interface has depends on (in) connectivity from network resource
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "subInterfaceNodesConnectedIn");
  }

  @Test
  public void testSubInterfaceGetAttrInOut() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to different network
     * 3. Sub-interface has get attribute (in) connectivity from network resource
     * 4. Sub-interface has get attribute (out) connectivity to second network resource
     * 5. Sub-interface has get attribute (in) connectivity from output param
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "subInterfaceGetAttrInOut");
  }

  @Test
  public void testSubInterfaceMultipleVlanSameNestedFile() throws IOException {
    /*
     * Heat file with one compute, one port and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces with same nested files
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "multipleVlanSameType");
  }

  @Test
  public void testSubInterfaceMultipleVlanDiffNestedFile() throws IOException {
    /*
     * Heat file with one compute, one port and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces with different nested files
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "multipleVlanDiffType");
  }

  @Test
  public void testSubInterfaceMultiplePortsMultipleVlans() throws IOException {
    /*
     * Heat file with one compute, two ports and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces each with different nested files
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "multiplePortsMultipleVlans");
  }

  @Test
  public void testSubInterfaceRegularNested() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource represented through a
     * regular nested resource and not using a resource group
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "regularNestedSubInterface");
  }

  @Test
  public void testSubInterfaceNotBoundToPort() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Resource group missing virtual_machine_interface_refs property
     * 2. Resource group missing virtual_machine_interface_properties_sub_interface_vlan_tag
     *    property
     * 3. Resource group parent port as get_param
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "notBoundToParentPort");
  }

  @Test
  public void testIfSubInfNotPresentPortPropShouldBeFalse() throws IOException {
    /*
     * Nested Heat file with one compute, two port and one subinterface resource group with
     * 1. Port connected with compute
     * 2. Sub-interface connected to only on Port
     * 3. Need to verify if sub interface resource is not bind with Port then
     * subinterface_indicator property under port value should be false
     */
    testTranslationWithInit(PATTERN_1A_BASE_DIRECTORY + "twoPortOnlyOneConnectedWithSubInf");
  }

  //Pattern 1B test
  @Test
  public void testDiffComputeSubInterface() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. There are two compute both of different type
     */
    testTranslationWithInit(PATTERN_1B_BASE_DIRECTORY + "diffComputeSubInterface");
  }

  @Test
  public void testOnePortIsConnectedWithSubInf() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Two compute of same type
     * 2. Two port of same type but only one of them is having sub interface binding
     */
    testTranslationWithInit(PATTERN_1B_BASE_DIRECTORY + "onePortIsConnectedWithSubInf");
  }

  @Test
  public void testRegularNestedSubInterface() throws IOException {
    /*
     * Heat file with one compute, one port and one sub interface without resource group
     * 1. Two compute of same type
     * 2. Two port are of different type
     * 3. Sub interface properties are same in both port
     */
    testTranslationWithInit(PATTERN_1B_BASE_DIRECTORY + "regularNestedSubInterface");
  }

  @Test
  public void testSameComputeDiffPortSubInterface() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Two compute of same type
     * 2. Two port are of different type
     * 3. Sub interface properties are same in both port
     */
    testTranslationWithInit(PATTERN_1B_BASE_DIRECTORY + "sameComputeDiffPortSubInterface");
  }

  @Test
  public void testSameComputeSamePortDiffSubInterfaceFile() throws IOException {
    /*
     * Heat file with one compute, one port and one subinterface resource group with
     * 1. Two compute of same type
     * 2. Two port are of same type connected with different sub interface nested file
     * 3. Sub interface property are same for both port
     */
    testTranslationWithInit(PATTERN_1B_BASE_DIRECTORY + "sameComputeSamePortDiffSubInterfaceFile");
  }

  //****************** PATTERN 1C1 Tests ***************************

  @Test
  public void testSubInterfaceScalingOnePortVlanSameType() throws IOException {
    /*
     * Heat file with two computes of same type, two ports of same type and two subinterface resource groups of same
     * type with
     * 1. Compute has one port each
     * 2. Port has one sub-interface each
     * 3. Port connected to network
     * 3. Sub-interface not connected to network
     */
    testTranslationWithInit(PATTERN_1C1_BASE_DIRECTORY + "onePortVlanSameType");
  }

  @Test
  public void testSubInterfaceScalingMultiplePortsMultipleVlansWithGroup() throws IOException {
    /*
     * Heat file with two computes of different type, four ports of two types each and four subinterface resource groups
     * of two types each
     * 1. Each compute has two ports, one of each type
     * 2. Port has one sub-interface each
     * 3. Ports connected to different networks
     * 4. Sub-interface of one type connected to network
     */
    testTranslationWithInit(PATTERN_1C1_BASE_DIRECTORY + "multiplePortsMultipleVlans");
  }

  @Test
  public void testSubInterfaceScalingOptionalPropertiesConsolidation() throws IOException {
    /*
     * Heat file with two computes of same type, four ports of two types each and two subinterface resource groups
     * of same type
     * 1. Each compute has two ports, one of each type
     * 2. One of the ports has a subinterface
     * 3. Ports connected to network
     * 4. Subinterfaces not connected to network
     * 5. Optional properties contained in one subinterface and not in other
     */
    testTranslationWithInit(PATTERN_1C1_BASE_DIRECTORY + "optionalPropertiesConsolidation");
  }

  @Test
  public void testSubInterfaceScalingRegularNestedSubInterface() throws IOException {
    /*
     * Heat file with two computes, two ports of same type connected to network and two subinterface resource
     * represented through a regular nested resource and not using a resource group not connected to network
     */
    testTranslationWithInit(PATTERN_1C1_BASE_DIRECTORY + "regularNestedSubInterface");
  }

  @Test
  public void testSubInterfaceScalingAllConnectivities() throws IOException {
    /*
     * Heat file with two computes of same type, four ports of two types each and two subinterface resource groups
     * of same type
     * 1. Each compute has two ports, one of each type
     * 2. One of the ports has a subinterface
     * 3. Port connected to network
     * 4. Both ports and subinterfaces connected to network
     * 5. All connectivities including dependency, get attribute from nodes and output param
     */
    testTranslationWithInit(PATTERN_1C1_BASE_DIRECTORY + "allConnectivities");
  }

  @Test
  public void testSubInterfaceWithDifferentResourceGroupCount() throws IOException {
    /*
     * Heat file with two computes of the same type, each one has one port with one sub interface
     * (from same type)
     * resource group have a different count which creates a catalog instance
     * */

    testTranslationWithInit(PATTERN_1C2_BASE_DIRECTORY + "differentResourceGroupCount");
  }

  @Test
  public void testSubInterfaceDifferentNodesConnectedOutToResourceGroup() throws IOException {
    testTranslationWithInit(PATTERN_1C2_BASE_DIRECTORY + "differentNodesConnectedOut");
  }

  @Test
  public void testSubInterfaceDifferentNetwork() throws IOException {
    testTranslationWithInit(PATTERN_1C2_BASE_DIRECTORY + "differentNetwork");
  }

  //Pattern5 Test Cases
  @Test
  public void testBaseScenarioPattern5() throws IOException {
    /*
     * Base heat has only nested component and nested heat has reference to subinterface and
     * two VFCs of different type
     * 1. Each server connected with two port
     * 2. One Port of each server connected to network
     * 3. Only one port out of four is having sub interface connectivity
     * 4. Sub-interface connected to different network then port
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "baseScenario");
  }

  @Test
  public void testComplexVFCScalingScenarioPattern5() throws IOException {
    /*
     * 1. Base heat has only nested component and nested has reference to subinterface and
     * two VFCs of same type
     * 1. Each server connected with two port
     * 2. One Port of each server connected to network
     * 3. Only one port out of four is having sub interface
     * 4. Sub-interface connected to different network then port
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCScalingScenario");
  }

  @Test
  public void testComplexVFCMultiLevelNestedScenarioPattern5() throws IOException {
    /*
     * 1. First level nested is having one VFC and one Port with Sub interface
     * 2. Second level nested is having one VFC and one Port with Sub interface which are of same
     * type as parent heat file
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCMultiLevelNestedScenario");
  }

  @Test
  public void testComplexVFCMultiLevelNestedDiffComputeScenarioPattern5() throws
      IOException {
    /*
     * 1. First level nested is having one VFC and one Port with Sub interface
     * 2. Second level nested is having one VFC and one Port with Sub interface which are of
     * different type as parent heat file
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCMultiLevelNestedDiffComputeScenario");
  }

  @Test
  public void testComplexVFCMultiComputeInAllHeatPattern5() throws
      IOException {
    /*
     * 1. Main is having 4 VFC of 2 type and 2 port and all having sub interface
     * 2. Second level nested is having 4 VFC of 2 type and 2 port and all having sub interface
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCMultiVFCInAllHeatScalingScenario");
  }

  @Test
  public void testComplexVFCMultiLevelNestedMultiComputeScenarioPattern5() throws
      IOException {
    /*
     * 1. First level nested is having one VFC and one Port with Sub interface
     * 2. Second level nested is having two VFC of same type and two Port with Sub interface
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCMultiLevelNestedMultiComputeScenario");
  }

  @Test
  public void testComplexVFCWithDiffSubInfConnectivityScenarioPattern5() throws IOException {
    /*
     * 1. First level nested is having one VFC and one Port with Sub interface
     * 2. Second level nested is having two VFC of same type and one Port with only one port
     * connected to Sub interface
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCWithDiffSubInfConnectivityScenario");
  }

  @Test
  public void testComplexVFCWithDiffSubInfCountScenarioPattern5() throws IOException {
    /*
     * 1. First level nested is having one VFC and one Port with Sub interface
     * 2. Second level nested is having two VFC of same type and connected to Sub interface but
     * having different count
     */
    testTranslationWithInit(PATTERN_5_BASE_DIRECTORY + "complexVFCWithDiffSubInfCountScenario");
  }

  //Pattern 4 Test Cases
  @Test
  public void testSubInterfaceComputePortNetworkPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource group with only
     * port connected to network
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "computePortNetworkSubInterface");
  }

  @Test
  public void testSubInterfaceMultiplePortsMultipleVlansPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, two ports and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces each with different nested files
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "multiplePortsMultipleVlans");
  }

  @Test
  public void testSubInterfaceMultipleVlanDiffFilePattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces with different nested files
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "multipleVlanDiffFile");
  }

  @Test
  public void testSubInterfaceMultipleVlanSameFilePattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and two subinterface resource groups with
     * 1. Port connected to network
     * 2. Sub-interfaces with same nested files
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "multipleVlanSameFile");
  }

  @Test
  public void testSubInterfaceRegularNestedPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource represented
     * through a regular nested resource and not using a resource group
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "regularNestedSubInterface");
  }

  @Test
  public void testSubInterfaceGetAttrInOutPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to different network
     * 3. Sub-interface has get attribute (in) connectivity from network resource
     * 4. Sub-interface has get attribute (out) connectivity to second network resource
     * 5. Sub-interface has get attribute (in) connectivity from output param
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "subInterfaceGetAttrInOut");
  }

  @Test
  public void testSubInterfaceNodesConnectedInPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to different network
     * 3. Sub-interface has depends on (in) connectivity from network resource
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "subInterfaceNodesConnectedIn");
  }

  @Test
  public void testSubInterfaceNodesConnectedOutPattern4() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to same network
     * 3. Sub-interface has depends on (out) connectivity with network resource
     */
    testTranslationWithInit(PATTERN_4_BASE_DIRECTORY + "subInterfaceNodesConnectedOut");
  }


  //****************** VFC Instance Group Tests ***************************

  @Test
  public void testGroupingOneSubInterfaceTypeOneAbstractNode() throws IOException {
    testTranslationWithInit(VFC_INSTANCE_GROUP_BASE_DIRECTORY + "oneSubInterfaceNetworkRole");
  }

  @Test
  public void testGroupingOneSubInterfaceTypeAndTwoAbstractNodes() throws IOException {
    testTranslationWithInit(VFC_INSTANCE_GROUP_BASE_DIRECTORY + "oneSubInterfaceNetworkRoleAndTwoAbstractNodes");
  }

  @Test
  public void testGroupingTwoSubInterfaceTypeAndTwoAbstractNodes() throws IOException {
    testTranslationWithInit(VFC_INSTANCE_GROUP_BASE_DIRECTORY + "twoSubInterfaceNetworkRoleAndTwoAbstractNodes");
  }

  @Test
  public void testSubInterfacePattern4WithGroup() throws IOException {
    /*
     * Nested Heat file with one compute, one port and one subinterface resource group with
     * 1. Port connected to network
     * 2. Sub-interface connected to same network
     * 3. Sub-interface has depends on (out) connectivity with network resource
     */
    testTranslationWithInit(VFC_INSTANCE_GROUP_BASE_DIRECTORY + "pattern4SubInterfaceNodesConnectedOut");
  }

  @Test
  public void testSubInterfaceVdbeav() throws IOException {
    /*
     * vlan tagging vdbeav production heat
     */
    testTranslationWithInit(VFC_INSTANCE_GROUP_BASE_DIRECTORY + "vdbeav");
  }

}
