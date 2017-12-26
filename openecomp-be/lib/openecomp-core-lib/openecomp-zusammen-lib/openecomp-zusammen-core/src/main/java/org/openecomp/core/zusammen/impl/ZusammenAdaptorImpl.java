/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.core.zusammen.impl;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.db.ZusammenConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ZusammenAdaptorImpl implements ZusammenAdaptor {

  private ZusammenConnector connector;

  public ZusammenAdaptorImpl(ZusammenConnector connector) {
    this.connector = connector;
  }

  @Override
  public Optional<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext,
                                              Id elementId) {
    return Optional.ofNullable(connector.getElementInfo(context, elementContext, elementId));
  }

  @Override
  public Optional<Element> getElement(SessionContext context, ElementContext elementContext,
                                      String elementId) {
    return Optional.ofNullable(connector.getElement(context, elementContext, new Id(elementId)));
  }

  @Override
  public Optional<Element> getElementByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName) {
    Collection<ElementInfo> elementInfos =
        connector.listElements(context, elementContext, parentElementId);
    Predicate<ElementInfo> elementInfoPredicate = elementInfo -> elementInfo.getInfo() != null
        && elementName.equals(elementInfo.getInfo().getName());
    return getFirstElementInfo(elementInfos, elementInfoPredicate)
        .flatMap(elementInfo -> getElement(context, elementContext,
            elementInfo.getId().getValue()));
  }

  @Override
  public Collection<ElementInfo> listElements(SessionContext context, ElementContext elementContext,
                                              Id parentElementId) {
    return connector.listElements(context, elementContext, parentElementId);
  }

  @Override
  public Collection<Element> listElementData(SessionContext context,
                                             ElementContext elementContext,
                                             Id parentElementId) {
    Collection<ElementInfo> elementInfoList = connector.listElements(context, elementContext,
        parentElementId);

    return elementInfoList == null
        ? new ArrayList<>()
        : elementInfoList.stream()
            .map(elementInfo -> connector.getElement(context, elementContext, elementInfo.getId()))
            .collect(Collectors.toList());
  }


  @Override
  public Collection<ElementInfo> listElementsByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName) {
    Optional<ElementInfo> elementInfoByName =
        getElementInfoByName(context, elementContext, parentElementId, elementName);

    return elementInfoByName.isPresent()
        ? connector.listElements(context, elementContext, elementInfoByName.get().getId())
        : new ArrayList<>();
  }

  @Override
  public Optional<ElementInfo> getElementInfoByName(
      SessionContext context, ElementContext elementContext, Id parentElementId,
      String elementName) {
    Collection<ElementInfo> elementInfos =
        connector.listElements(context, elementContext, parentElementId);
    return getFirstElementInfo(elementInfos,
        elementInfo -> elementInfo.getInfo() != null
           && elementName.equals(elementInfo.getInfo().getName()));
  }

  @Override
  public Optional<ElementConflict> getElementConflict(SessionContext context,
                                                      ElementContext elementContext,
                                                      Id elementId) {
    return Optional.ofNullable(connector.getElementConflict(context, elementContext, elementId));
  }

  @Override
  public Element saveElement(SessionContext context, ElementContext elementContext,
                             ZusammenElement element, String message) {
    enrichElementHierarchyRec(context, elementContext, null, element);
    return connector.saveElement(context, elementContext, element, message);
  }

  @Override
  public void resolveElementConflict(SessionContext context, ElementContext elementContext,
                                     ZusammenElement element, Resolution resolution) {
    connector.resolveElementConflict(context, elementContext, element, resolution);
  }

  private void enrichElementHierarchyRec(SessionContext context, ElementContext
      elementContext, Id parentElementId, ZusammenElement element) {
    if (element.getAction() == Action.CREATE) {
      return;
    }
    locateElementAndUpdateAction(context, elementContext, parentElementId, element);
    element.getSubElements().forEach(subElement -> enrichElementHierarchyRec(
        context, elementContext, element.getElementId(), (ZusammenElement) subElement));
  }

  // should be applied only for structural elements
  private void locateElementAndUpdateAction(SessionContext context, ElementContext elementContext,
                                            Id parentElementId, ZusammenElement element) {
    if (element.getElementId() != null) {
      return;
    }
    Optional<ElementInfo> elementInfo =
        getElementInfoByName(context, elementContext, parentElementId, element.getInfo().getName());
    if (elementInfo.isPresent()) {
      element.setElementId(elementInfo.get().getId());
      if (element.getAction() == null) {
        element.setAction(Action.IGNORE);
      }
    } else {
      element.setAction(Action.CREATE);
    }
  }

  private Optional<ElementInfo> getFirstElementInfo(Collection<ElementInfo> elementInfos,
                                                    Predicate<ElementInfo> elementInfoPredicate) {
    return elementInfos.stream()
        .filter(elementInfoPredicate)
        .findFirst();
  }

  @Override
  public Collection<Item> listItems(SessionContext context) {
    return connector.listItems(context);
  }

  @Override
  public Item getItem(SessionContext context, Id itemId) {
    return connector.getItem(context, itemId);
  }

  @Override
  public Id createItem(SessionContext context, Info info) {
    return connector.createItem(context, info);
  }

  @Override
  public void updateItem(SessionContext context, Id itemId, Info info) {
    connector.updateItem(context, itemId, info);
  }

  @Override
  public Optional<ItemVersion> getFirstVersion(SessionContext context, Id itemId) {
    Collection<ItemVersion> versions = connector.listPublicVersions(context, itemId);
    if (versions == null || versions.isEmpty()) {
      return Optional.empty();
    }
    List<ItemVersion> itemVersions = new ArrayList<>(versions);
    sortItemVersionListByModificationTimeDescOrder(itemVersions);
    ItemVersion itemVersion = itemVersions.iterator().next();

    return Optional.ofNullable(itemVersion);
  }

  @Override
  public Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId) {
    return connector.listPublicVersions(context, itemId);
  }

  @Override
  public ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId) {
    return connector.getPublicVersion(context, itemId, versionId);
  }

  @Override
  public ItemVersion getVersion(SessionContext context, Id itemId, Id versionId) {
    return connector.getVersion(context, itemId, versionId);
  }

  @Override
  public ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId) {
    return connector.getVersionStatus(context, itemId, versionId);
  }

  @Override
  public ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId) {
    return connector.getVersionConflict(context, itemId, versionId);
  }

  @Override
  public Id createVersion(SessionContext context, Id itemId, Id baseVersionId, ItemVersionData
      itemVersionData) {
    return connector.createVersion(context, itemId, baseVersionId, itemVersionData);
  }

  @Override
  public void updateVersion(SessionContext context, Id itemId, Id versionId,
                            ItemVersionData itemVersionData) {
    connector.updateVersion(context, itemId, versionId, itemVersionData);
  }

  @Override
  public void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag) {
    connector.tagVersion(context, itemId, versionId, tag);
  }

  @Override
  public void resetVersionHistory(SessionContext context, Id itemId, Id versionId,
                                  String changeRef) {
    connector.resetVersionHistory(context, itemId, versionId, changeRef);
  }

  @Override
  public void publishVersion(SessionContext context, Id itemId, Id versionId, String message) {
    connector.publishVersion(context, itemId, versionId, message);
  }

  @Override
  public void syncVersion(SessionContext context, Id itemId, Id versionId) {
    connector.syncVersion(context, itemId, versionId);
  }

  @Override
  public void forceSyncVersion(SessionContext context, Id itemId, Id versionId) {
    connector.forceSyncVersion(context, itemId, versionId);
  }

  @Override
  public void revert(SessionContext context, Id itemId, Id versionId, Id revisionId) {
    connector.revertVersionRevision(context, itemId, versionId, revisionId);
  }

  @Override
  public ItemVersionRevisions listRevisions(SessionContext context, Id itemId, Id versionId) {
    return connector.listVersionRevisions(context, itemId, versionId);
  }

  @Override
  public Collection<HealthInfo> checkHealth(SessionContext context) {
    return connector.checkHealth(context);
  }

  @Override
  public String getVersion(SessionContext context) {
    return connector.getVersion(context);
  }

  private static void sortItemVersionListByModificationTimeDescOrder(
      List<ItemVersion> itemVersions) {
    itemVersions.sort((o1, o2) -> Integer.compare(o2.getId().getValue().length(),
        o1.getId().getValue().length()));
  }
}
