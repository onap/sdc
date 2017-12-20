package org.openecomp.core.zusammen.db;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Tag;

import java.util.Collection;

public interface ZusammenConnector {

  Collection<HealthInfo> checkHealth(SessionContext sessionContext);

  String getVersion(SessionContext sessionContext);

  Collection<Item> listItems(SessionContext context);

  Item getItem(SessionContext context, Id itemId);

  Id createItem(SessionContext context, Info info);

  void updateItem(SessionContext context, Id itemId, Info info);


  Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId);

  ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId);

  Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                   ItemVersionData itemVersionData);

  void updateVersion(SessionContext context, Id itemId, Id versionId,
                     ItemVersionData itemVersionData);

  ItemVersion getVersion(SessionContext context, Id itemId, Id versionId);

  ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId);

  void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag);

  void resetVersionRevision(SessionContext context, Id itemId, Id versionId, Id revisionId);

  void revertVersionRevision(SessionContext context, Id itemId, Id versionId, Id revisionId);

  ItemVersionRevisions listVersionRevisions(SessionContext context, Id itemId, Id versionId);

  void publishVersion(SessionContext context, Id itemId, Id versionId, String message);

  void syncVersion(SessionContext context, Id itemId, Id versionId);

  void forceSyncVersion(SessionContext context, Id itemId, Id versionId);

  ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId);


  Collection<ElementInfo> listElements(SessionContext context, ElementContext elementContext,
                                       Id parentElementId);

  ElementInfo getElementInfo(SessionContext context, ElementContext elementContext, Id elementId);

  Element getElement(SessionContext context, ElementContext elementContext, Id elementId);

  ElementConflict getElementConflict(SessionContext context, ElementContext elementContext,
                                     Id elementId);

  Element saveElement(SessionContext context, ElementContext elementContext,
                      ZusammenElement element, String message);

  void resolveElementConflict(SessionContext context, ElementContext elementContext,
                              ZusammenElement element, Resolution resolution);

  void resetVersionHistory(SessionContext context, Id itemId, Id versionId, String changeRef);
}
