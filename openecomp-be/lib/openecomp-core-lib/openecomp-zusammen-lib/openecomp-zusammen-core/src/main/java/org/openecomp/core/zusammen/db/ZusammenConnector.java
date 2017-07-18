package org.openecomp.core.zusammen.db;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.Response;

import java.util.Collection;
import java.util.Optional;

public interface ZusammenConnector {

  Collection<Item> listItems(SessionContext context);

  Id createItem(SessionContext context, Info info);

  void updateItem(SessionContext context, Id itemId, Info info);


  Collection<ItemVersion> listVersions(SessionContext context, Id itemId);

  Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                   ItemVersionData itemVersionData);

  void updateVersion(SessionContext context, Id itemId, Id versionId,
                     ItemVersionData itemVersionData);

  void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag);

  void resetVersionHistory(SessionContext context, Id itemId, Id versionId, String changeRef);


  Collection<ElementInfo> listElements(SessionContext context, ElementContext elementContext,
                                       Id parentElementId);

  Response<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext, Id
      elementId);

  Response<Element> getElement(SessionContext context, ElementContext elementContext, Id elementId);

  Optional<Element> saveElement(SessionContext context, ElementContext elementContext,
                                ZusammenElement element, String message);

  Collection<HealthInfo> checkHealth(SessionContext sessionContext);

  String getVersion(SessionContext sessionContext);
}
