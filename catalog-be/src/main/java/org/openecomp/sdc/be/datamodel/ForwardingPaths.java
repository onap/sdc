package org.openecomp.sdc.be.datamodel;

import java.io.Serializable;
import java.util.Set;

public class ForwardingPaths implements Serializable{

  private static final long serialVersionUID=1L;

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
