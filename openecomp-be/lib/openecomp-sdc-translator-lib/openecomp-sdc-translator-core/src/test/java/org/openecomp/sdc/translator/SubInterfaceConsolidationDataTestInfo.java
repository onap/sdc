package org.openecomp.sdc.translator;

import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;

import java.util.List;

public class SubInterfaceConsolidationDataTestInfo {
  private final String serviceTemplateFileName;
  private final String portNodeTemplateId;
  private final String subInterfaceType;
  private final int resourceGroupCount;
  private final String networkRole;
  private final String subInterfaceId;
  private final List<String> nodesConnectedIn;
  private final List<String> nodesConnectedOut;
  private final List<String> nodesGetAttrIn;
  private final List<Pair<String, GetAttrFuncData>> nodesGetAttrOut;
  private final ConsolidationData consolidationData;

  public SubInterfaceConsolidationDataTestInfo(String serviceTemplateFileName,
                                               String portNodeTemplateId, String subInterfaceType,
                                               int resourceGroupCount, String networkRole,
                                               String subInterfaceId, List<String> nodesConnectedIn,
                                               List<String> nodesConnectedOut,
                                               List<String> nodesGetAttrIn,
                                               List<Pair<String, GetAttrFuncData>> nodesGetAttrOut,
                                               ConsolidationData consolidationData) {
    this.serviceTemplateFileName = serviceTemplateFileName;
    this.portNodeTemplateId = portNodeTemplateId;
    this.subInterfaceType = subInterfaceType;
    this.resourceGroupCount = resourceGroupCount;
    this.networkRole = networkRole;
    this.subInterfaceId = subInterfaceId;
    this.nodesConnectedIn = nodesConnectedIn;
    this.nodesConnectedOut = nodesConnectedOut;
    this.nodesGetAttrIn = nodesGetAttrIn;
    this.nodesGetAttrOut = nodesGetAttrOut;
    this.consolidationData = consolidationData;
  }

  public String getServiceTemplateFileName() {
    return serviceTemplateFileName;
  }

  public String getPortNodeTemplateId() {
    return portNodeTemplateId;
  }

  public String getSubInterfaceType() {
    return subInterfaceType;
  }

  public int getResourceGroupCount() {
    return resourceGroupCount;
  }

  public String getNetworkRole() {
    return networkRole;
  }

  public String getSubInterfaceId() {
    return subInterfaceId;
  }

  public List<String> getNodesConnectedIn() {
    return nodesConnectedIn;
  }

  public List<String> getNodesConnectedOut() {
    return nodesConnectedOut;
  }

  public List<String> getNodesGetAttrIn() {
    return nodesGetAttrIn;
  }

  public List<Pair<String, GetAttrFuncData>> getNodesGetAttrOut() {
    return nodesGetAttrOut;
  }

  public ConsolidationData getConsolidationData() {
    return consolidationData;
  }
}
