package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;

/**
 * The type Requirement assignment data.
 */
public class RequirementAssignmentData {

  private String requirementId;
  private RequirementAssignment requirementAssignment;

  /**
   * Instantiates a new Requirement assignment data.
   *
   * @param requirementId         the requirement id
   * @param requirementAssignment the requirement assignment
   */
  public RequirementAssignmentData(String requirementId, RequirementAssignment
      requirementAssignment) {
    this.requirementId = requirementId;
    this.requirementAssignment = requirementAssignment;
  }

  /**
   * Gets requirement id.
   *
   * @return the requirement id
   */
  public String getRequirementId() {
    return requirementId;
  }

  /**
   * Sets requirement id.
   *
   * @param requirementId the requirement id
   */
  public void setRequirementId(String requirementId) {
    this.requirementId = requirementId;
  }

  /**
   * Gets requirement assignment.
   *
   * @return the requirement assignment
   */
  public RequirementAssignment getRequirementAssignment() {
    return requirementAssignment;
  }

  /**
   * Sets requirement assignment.
   *
   * @param requirementAssignment the requirement assignment
   */
  public void setRequirementAssignment(RequirementAssignment requirementAssignment) {
    this.requirementAssignment = requirementAssignment;
  }
}
