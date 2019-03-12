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

package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeatTreeManagerTest extends TreeBaseTest {

  @Test
  public void testHeatTreeManager() throws IOException, URISyntaxException {

    HeatTreeManager heatTreeManager = initHeatTreeManager("./tree/valid_tree/input/");
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
  public void testHeatTreeManagerMissingManifest() throws IOException, URISyntaxException {

    HeatTreeManager heatTreeManager = initHeatTreeManager("./tree/missing_manifest/input/");
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
    Assert.assertNull(tree.getHeat());

  }


  @Test
  public void testResourceGroupShowsAsNestedFileInTree() throws IOException, URISyntaxException {

    HeatTreeManager heatTreeManager = initHeatTreeManager("./tree/nested_resource_group");
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
  public void testVolumeNestedFileIsNotUnderVolumeSubTree() throws IOException, URISyntaxException {

    HeatTreeManager heatTreeManager = initHeatTreeManager("./tree/nested_volume");
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
