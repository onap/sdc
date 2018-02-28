package org.openecomp.core.zusammen.api;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.types.ElementPropertyName;

public class ZusammenUtil {

  private ZusammenUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static SessionContext createSessionContext() {
    org.openecomp.sdc.common.session.SessionContext asdcSessionContext =
        SessionContextProviderFactory.getInstance().createInterface().get();

    return createSessionContext(asdcSessionContext.getUser().getUserId(), asdcSessionContext
        .getTenant());
  }

  private static SessionContext createSessionContext(String user, String tenant) {
    SessionContext sessionContext = new SessionContext();
    sessionContext.setUser(new UserInfo(user));
    sessionContext.setTenant(tenant);
    return sessionContext;
  }

  public static ZusammenElement buildStructuralElement(ElementType elementType, Action action) {
    return buildStructuralElement(elementType.name(), action);
  }


  public static ZusammenElement buildStructuralElement(String elementType, Action action) {
    ZusammenElement element = buildElement(null, action);
    Info info = new Info();
    info.setName(elementType);
    info.addProperty(ElementPropertyName.elementType.name(), elementType);
    element.setInfo(info);
    return element;
  }

  public static ZusammenElement buildElement(Id elementId, Action action) {
    ZusammenElement element = new ZusammenElement();
    element.setElementId(elementId);
    element.setAction(action);
    return element;
  }
}
