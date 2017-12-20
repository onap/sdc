package org.openecomp.sdc.versioning.dao.types;

import java.util.Date;

public class Revision {
  private String id;
  private String message;
  private Date time;
  private String user;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getMessage() {
    return message;
  }

  public Date getTime() {
    return time;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
