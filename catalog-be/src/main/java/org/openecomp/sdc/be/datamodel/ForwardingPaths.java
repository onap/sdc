package org.openecomp.sdc.be.datamodel;

import java.util.Set;

public class ForwardingPaths {

  public ForwardingPaths() {
  }

  private Set<String> forwardingPathToDelete;
  public Set<String> getForwardingPathToDelete() {
    return forwardingPathToDelete;
  }

  public void setForwardingPathToDelete(Set<String> forwardingPathToDelete) {
    this.forwardingPathToDelete = forwardingPathToDelete;
  }
}
