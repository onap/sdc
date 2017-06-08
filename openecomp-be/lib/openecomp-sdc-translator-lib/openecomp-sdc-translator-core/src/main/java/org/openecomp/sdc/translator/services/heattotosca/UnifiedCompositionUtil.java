package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for consolidation data collection helper methods.
 */
public class UnifiedCompositionUtil {

  protected static Logger logger = (Logger) LoggerFactory.getLogger(UnifiedCompositionUtil.class);

  /**
   * Gets all ports per port type, which are connected to the computes from the input
   * computeTemplateConsolidationDataCollection.
   *
   * @param computeTemplateConsolidationDataCollection collection of compute template
   *                                                   consolidation data
   * @return set of port ids, per port type
   */
  public static Map<String, Set<String>> collectAllPortsFromEachTypesFromComputes(
      Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDataCollection) {
    Map<String, Set<String>> portTypeToIds = new HashMap<>();

    for (ComputeTemplateConsolidationData compute : computeTemplateConsolidationDataCollection) {
      Map<String, List<String>> ports = compute.getPorts();
      if (!MapUtils.isEmpty(ports)) {
        addPortsToMap(portTypeToIds, ports);
      }
    }

    return portTypeToIds;
  }

  private static void addPortsToMap(Map<String, Set<String>> portTypeToIds,
                                    Map<String, List<String>> ports) {
    for (Map.Entry<String, List<String>> portTypeToIdEntry : ports.entrySet()) {
      portTypeToIds.putIfAbsent(portTypeToIdEntry.getKey(), new HashSet<>());
      portTypeToIds.get(portTypeToIdEntry.getKey()).addAll(portTypeToIdEntry.getValue());
    }
  }


}
