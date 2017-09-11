package org.openecomp.sdc.heat.services.tree;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.Artifact;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ToscaTreeManager {

  private static Logger logger = (Logger) LoggerFactory.getLogger(ToscaTreeManager.class);

  private FileContentHandler csarContentMap = new FileContentHandler();
  private byte[] manifest;
  private HeatStructureTree tree = new HeatStructureTree();
  private Map<String, Artifact> artifactRef = new HashMap<>();
  private Map<String, HeatStructureTree> fileTreeRef = new HashMap<>();


  public void addFile(String fileName, byte[] content) {
    if (fileName.equals(SdcCommon.CSAR_MANIFEST_NAME)) {
      manifest = content;

    } else {
      csarContentMap.addFile(fileName, content);
    }
  }

  public void createTree(){
    if (manifest == null) {
      logger.error("Missing manifest file in the zip.");
      return;
    }

    for(Map.Entry<String, byte[]> fileEntry : csarContentMap.getFiles().entrySet()){
      String[] splitFilename = getFullFileNameAsArray(fileEntry.getKey());
      addFileToTree(splitFilename, 0, tree);
    }


  }

  private void addFileToTree(String[] splitFilename, int startIndex, HeatStructureTree parent){
    fileTreeRef.putIfAbsent(splitFilename[startIndex], new HeatStructureTree());
    HeatStructureTree heatStructureTree = fileTreeRef.get(splitFilename[startIndex]);
    heatStructureTree.setFileName(splitFilename[startIndex]);
    if(startIndex < splitFilename.length - 1){
      addFileToTree(splitFilename, startIndex + 1, heatStructureTree);
    }
    parent.addHeatStructureTreeToNestedHeatList(heatStructureTree);
  }

  public void addErrors(Map<String, List<ErrorMessage>> validationErrors){
    validationErrors.entrySet().stream().filter(entry -> {
      return fileTreeRef.get(entry.getKey()) != null;
    }).forEach(entry -> entry.getValue().stream().forEach(error ->
        fileTreeRef.get(entry.getKey()).addErrorToErrorsList(error)));
  }

  private String[] getFullFileNameAsArray(String filename){
    if(filename.contains("/")){
      return filename.split("/");
    }

    return filename.split(Pattern.quote(File.separator));
  }

  public HeatStructureTree getTree(){
    return tree;
  }
}
