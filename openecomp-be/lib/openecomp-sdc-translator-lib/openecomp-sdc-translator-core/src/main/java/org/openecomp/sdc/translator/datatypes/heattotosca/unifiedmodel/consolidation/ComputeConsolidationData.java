package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type Compute consolidation data.
 */
public class ComputeConsolidationData {

  //Key - Service template file name
  private Map<String, FileComputeConsolidationData> fileComputeConsolidationData;

  /**
   * Instantiates a new Compute consolidation data.
   */
  public ComputeConsolidationData() {
    fileComputeConsolidationData = new HashMap<>();
  }

  /**
   * Gets all files.
   *
   * @return the all files
   */
  public Set<String> getAllServiceTemplateFileNames() {
    return fileComputeConsolidationData.keySet();
  }

  /**
   * Gets file compute consolidation data.
   *
   * @param serviceTemplateFileName the file name
   * @return the file compute consolidation data
   */
  public FileComputeConsolidationData getFileComputeConsolidationData(String
                                                                          serviceTemplateFileName) {
    return fileComputeConsolidationData.get(serviceTemplateFileName);
  }

  /**
   * Sets file compute consolidation data.
   *
   * @param serviceTemplateFileName      the file name
   * @param fileComputeConsolidationData the file compute consolidation data
   */
  public void setFileComputeConsolidationData(String serviceTemplateFileName,
                                              FileComputeConsolidationData fileComputeConsolidationData) {
    this.fileComputeConsolidationData.put(serviceTemplateFileName, fileComputeConsolidationData);
  }
}
