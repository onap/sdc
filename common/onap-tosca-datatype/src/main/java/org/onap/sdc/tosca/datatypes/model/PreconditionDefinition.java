package org.onap.sdc.tosca.datatypes.model;
import lombok.Data;

@Data
public class PreconditionDefinition {

  private String target;
  private String target_relationship;
  private Constraint condition;

}
