package org.openecomp.core.zusammen.api;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;

public class ZusammenUtil {
  // TODO: 3/19/2017 add user and tenant args
  public static SessionContext createSessionContext() {
    SessionContext sessionContext = new SessionContext();
    sessionContext.setUser(new UserInfo("GLOBAL_USER"));
    sessionContext.setTenant("dox");
    return sessionContext;
  }

  public static ZusammenElement buildStructuralElement(String structureElementName,
                                                       Action action) {
    ZusammenElement element = new ZusammenElement();
    Info info = new Info();
    info.setName(structureElementName);
    element.setInfo(info);
    if (action != null) {
      element.setAction(action);
    }
    return element;
  }

  // TODO: 4/24/2017 remove upon working with more than one single version
  public static ItemVersionData createFirstVersionData() {
    Info info = new Info();
    info.setName("main version");
    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(info);
    return itemVersionData;
  }

}
