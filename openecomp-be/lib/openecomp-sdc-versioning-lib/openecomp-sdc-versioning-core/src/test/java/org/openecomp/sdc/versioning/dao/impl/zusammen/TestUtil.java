package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;

public class TestUtil {

  public static SessionContext createZusammenContext(String user) {
    SessionContext sessionContext = new SessionContext();
    sessionContext.setUser(new UserInfo(user));
    sessionContext.setTenant("dox");
    return sessionContext;
  }
}
