package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type Port consolidation data.
 */
public class PortConsolidationData {

  //Key - Service template file name
  private Map<String, FilePortConsolidationData> filePortConsolidationData;

  public PortConsolidationData() {
    this.filePortConsolidationData = new HashMap<>();
  }

  /**
   * Gets all files.
   *
   * @return the all files
   */
  public Set<String> getAllServiceTemplateFileNames() {
    return filePortConsolidationData.keySet();
  }

  /**
   * Gets file port consolidation data.
   *
   * @param serviceTemplateFileName the service template file name
   * @return the file port consolidation data
   */
  public FilePortConsolidationData getFilePortConsolidationData(String serviceTemplateFileName) {
    return filePortConsolidationData.get(serviceTemplateFileName);
  }

  /**
   * Sets file port consolidation data.
   *
   * @param serviceTemplateFileName   the service template file name
   * @param filePortConsolidationData the file port consolidation data
   */
  public void setFilePortConsolidationData(String serviceTemplateFileName, FilePortConsolidationData
      filePortConsolidationData) {
    this.filePortConsolidationData.put(serviceTemplateFileName, filePortConsolidationData);
  }
}
