package org.onap.sdc.tosca.datatypes.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeInterval {
  private Timestamp start_time;
  private Timestamp end_time;

}
