package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type Nested Node consolidation data.
 */
public class NestedConsolidationData {

  //Key - Service template file name
  private Map<String, FileNestedConsolidationData> fileNestedConsolidationData;

  public NestedConsolidationData() {
    this.fileNestedConsolidationData = new HashMap<>();
  }

  /**
   * Gets all files.
   *
   * @return the all files
   */
  public Set<String> getAllServiceTemplateFileNames() {
    return fileNestedConsolidationData.keySet();
  }

  /**
   * Gets file nested consolidation data.
   *
   * @param serviceTemplateFileName the service template file name
   * @return the file nested consolidation data
   */
  public FileNestedConsolidationData getFileNestedConsolidationData(String
                                                                        serviceTemplateFileName) {
    return fileNestedConsolidationData.get(serviceTemplateFileName);
  }

  /**
   * Sets file nested consolidation data.
   *
   * @param serviceTemplateFileName   the service template file name
   * @param fileNestedConsolidationData the file nested consolidation data
   */
  public void setFileNestedConsolidationData(String serviceTemplateFileName,
                                             FileNestedConsolidationData
                                                 fileNestedConsolidationData) {
    this.fileNestedConsolidationData.put(serviceTemplateFileName, fileNestedConsolidationData);
  }
}
