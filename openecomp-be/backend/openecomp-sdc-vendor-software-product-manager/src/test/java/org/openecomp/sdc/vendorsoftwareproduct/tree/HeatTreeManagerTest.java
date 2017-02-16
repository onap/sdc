package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;


public class HeatTreeManagerTest extends TreeBaseTest {

  @Test
  public void testHeatTreeManager() {

    INPUT_DIR = "/tree/valid_tree/input/";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    Map<String, List<ErrorMessage>> errorMap = new HashMap<>();

    List<ErrorMessage> errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, "Missing Artifact"));
    errorMap.put("missing-artifact", errorList);
    errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.WARNING, "Missing Nested File"));
    errorMap.put("missingNested.yaml", errorList);
    heatTreeManager.addErrors(errorMap);
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
  }

  @Test
  public void testHeatTreeManagerMissingManifest() {

    INPUT_DIR = "/tree/missing_manifest/input/";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    Map<String, List<ErrorMessage>> errorMap = new HashMap<>();

    List<ErrorMessage> errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, "Missing Artifact"));
    errorMap.put("missing-artifact", errorList);
    errorList = new ArrayList<>();
    errorList.add(new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.WARNING, "Missing Nested File"));
    errorMap.put("missingNested.yaml", errorList);
    heatTreeManager.addErrors(errorMap);
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
    Assert.assertEquals(tree.getHEAT(), null);

  }


  @Test
  public void testResourceGroupShowsAsNestedFileInTree() throws IOException {
    INPUT_DIR = "/tree/nested_resource_group";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();

    Set<HeatStructureTree> heat = tree.getHEAT();
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
    INPUT_DIR = "/tree/nested_volume";
    HeatTreeManager heatTreeManager = initHeatTreeManager();
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();

    Set<HeatStructureTree> heat = tree.getHEAT();
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
