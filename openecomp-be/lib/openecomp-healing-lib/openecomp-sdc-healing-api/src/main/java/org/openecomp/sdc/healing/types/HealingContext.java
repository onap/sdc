package org.openecomp.sdc.healing.types;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * Created by TALIO on 7/3/2017.
 */
public class HealingContext {
  private String vspId;
  private Version version;
  private String user;

  public HealingContext(String vspId, Version version, String user) {
    this.vspId = vspId;
    this.version = version;
    this.user = user;
  }

  public String getVspId() {
    return vspId;
  }

  public void setVspId(String vspId) {
    this.vspId = vspId;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
