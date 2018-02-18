package org.openecomp.sdc.tosca.datatypes.model;

import java.sql.Timestamp;

public class TimeInterval {
  private Timestamp start_time;
  private Timestamp end_time;

  public Timestamp getStart_time() {
    return start_time;
  }

  public void setStart_time(Timestamp start_time) {
    this.start_time = start_time;
  }

  public Timestamp getEnd_time() {
    return end_time;
  }

  public void setEnd_time(Timestamp end_time) {
    this.end_time = end_time;
  }
}
