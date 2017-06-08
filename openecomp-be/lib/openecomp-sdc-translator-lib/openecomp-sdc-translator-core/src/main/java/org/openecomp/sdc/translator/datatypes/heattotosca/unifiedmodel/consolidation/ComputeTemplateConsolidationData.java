package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Compute template consolidation data.
 */
public class ComputeTemplateConsolidationData extends EntityConsolidationData {
  // key - volume node template id
  // List of requirement id and the requirement assignment on the
  // compute node which connect to this volume
  private Map<String,List<RequirementAssignmentData>> volumes;

  // key - port type (port id excluding index),
  // value - List of connected port node template ids, with this port type
  private Map<String, List<String>> ports;

  /**
   * Gets volumes.
   *
   * @return the volumes
   */
  public Map<String,List<RequirementAssignmentData>> getVolumes() {
    return volumes;
  }

  /**
   * Sets volumes.
   *
   * @param volumes the volumes
   */
  public void setVolumes(Map<String,List<RequirementAssignmentData>> volumes) {
    this.volumes = volumes;
  }

  /**
   * Gets ports.
   *
   * @return the ports
   */
  public Map<String, List<String>> getPorts() {
    return ports;
  }

  /**
   * Sets ports.
   *
   * @param ports the ports
   */
  public void setPorts(Map<String, List<String>> ports) {
    this.ports = ports;
  }

  /**
   * Add port.
   *
   * @param portType           the port type
   * @param portNodeTemplateId the port node template id
   */
  public void addPort(String portType, String portNodeTemplateId) {
    if (this.ports == null) {
      this.ports = new HashMap<>();
    }
    this.ports.putIfAbsent(portType, new ArrayList<>());
    this.ports.get(portType).add(portNodeTemplateId);
  }


  /**
   * Add volume.
   *
   * @param requirementId         the requirement id
   * @param requirementAssignment the requirement assignment
   */
  public void addVolume(String requirementId, RequirementAssignment requirementAssignment) {
    if (this.volumes == null) {
      this.volumes = new HashMap<>();
    }
    this.volumes.computeIfAbsent(requirementAssignment.getNode(), k -> new ArrayList<>())
        .add(new RequirementAssignmentData(requirementId,
            requirementAssignment));
  }
}
