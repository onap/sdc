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

package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HeatTreeManagerTest extends TreeBaseTest {

  @Test
  public void testHeatTreeManager() {

    INPUT_DIR = "./tree/valid_tree/input/";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    Map<String, List<ErrorMessage>> errorMap = new HashMap<>();

    List<ErrorMessage> errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(ErrorLevel.ERROR, "Missing Artifact"));
    errorMap.put("missing-artifact", errorList);
    errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(ErrorLevel.WARNING, "Missing Nested File"));
    errorMap.put("missingNested.yaml", errorList);
    heatTreeManager.addErrors(errorMap);
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
  }

  @Test
  public void testHeatTreeManagerMissingManifest() {

    INPUT_DIR = "./tree/missing_manifest/input/";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    Map<String, List<ErrorMessage>> errorMap = new HashMap<>();

    List<ErrorMessage> errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(ErrorLevel.ERROR, "Missing Artifact"));
    errorMap.put("missing-artifact", errorList);
    errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(ErrorLevel.WARNING, "Missing Nested File"));
    errorMap.put("missingNested.yaml", errorList);
    heatTreeManager.addErrors(errorMap);
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
    Assert.assertEquals(tree.getHeat(), null);

  }


  @Test
  public void testResourceGroupShowsAsNestedFileInTree() throws IOException {
    INPUT_DIR = "./tree/nested_resource_group";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();

    Set<HeatStructureTree> heat = tree.getHeat();
    Assert.assertNotNull(heat);

    HeatStructureTree addOnHeatSubTree =
        HeatStructureTree.getHeatStructureTreeByName(heat, "addOn.yml");
    Assert.assertNotNull(addOnHeatSubTree);

    Set<HeatStructureTree> addOnNestedFiles = addOnHeatSubTree.getNested();
    Assert.assertNotNull(addOnNestedFiles);

    HeatStructureTree nestedFile =
        HeatStructureTree.getHeatStructureTreeByName(addOnNestedFiles, "nested.yml");
    Assert.assertNotNull(nestedFile);
  }


  @Test
  public void testVolumeNestedFileIsNotUnderVolumeSubTree() {
    INPUT_DIR = "./tree/nested_volume";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();

    Set<HeatStructureTree> heat = tree.getHeat();
    Set<HeatStructureTree> volume = tree.getVolume();
    Assert.assertNotNull(heat);
    Assert.assertNull(volume);

    HeatStructureTree baseMobtSubTree =
        HeatStructureTree.getHeatStructureTreeByName(heat, "base_mobt.yaml");
    Assert.assertNotNull(baseMobtSubTree);

    Set<HeatStructureTree> baseMobtNestedFiles = baseMobtSubTree.getNested();
    Assert.assertNotNull(baseMobtNestedFiles);

    HeatStructureTree nestedVolumeFile = HeatStructureTree
        .getHeatStructureTreeByName(baseMobtNestedFiles, "hot_mobt_volume_attach_nested.yaml");
    Assert.assertNotNull(nestedVolumeFile);
  }
}
