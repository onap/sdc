package org.openecomp.core.zusammen.api;

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
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.Optional;

public interface ZusammenAdaptor {

  Collection<Item> listItems(SessionContext context);

  Id createItem(SessionContext context, Info info);

  void updateItem(SessionContext context, Id itemId, Info info);

  // TODO: 4/4/2017 fix this workaround when versionId will be recieved from UI
  Optional<ItemVersion> getFirstVersion(SessionContext context, Id itemId);

  Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                   ItemVersionData itemVersionData);

  void updateVersion(SessionContext context, Id itemId, Id versionId,
                     ItemVersionData itemVersionData);

  void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag);

  void resetVersionHistory(SessionContext context, Id itemId, Id versionId, String changeRef);

  Optional<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext,
                                       Id elementId);

  Optional<Element> getElement(SessionContext context, ElementContext elementContext,
                               String elementId); // TODO: 4/3/2017 change to Id

  Optional<Element> getElementByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName);

  Collection<ElementInfo> listElements(SessionContext context,
                                       ElementContext elementContext,
                                       Id parentElementId);

  Collection<Element> listElementData(SessionContext context,
                                      ElementContext elementContext,
                                      Id parentElementId);

  /**
   * Lists the sub elements of the element named <elementName> which is a sub element of <parentElementId>
   * @param context
   * @param elementContext
   * @param parentElementId
   * @param elementName
   * @return
   */
  Collection<ElementInfo> listElementsByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName);

  Optional<ElementInfo> getElementInfoByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName);

  Optional<Element> saveElement(SessionContext context, ElementContext elementContext,
                                ZusammenElement element, String message);

  Collection<HealthInfo> checkHealth(SessionContext context);

  String getVersion(SessionContext sessionContext);
}
