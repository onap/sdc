package org.openecomp.sdc.versioning.dao.types;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class Revision {
  private String id;
  private String message;
  private Date time;
  private String user;
}
