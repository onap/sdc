package org.onap.sdc.tosca.datatypes.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityDefinition {

  private String delegate;
  private String set_state;
  private String call_operation;
  private String inline;

}
