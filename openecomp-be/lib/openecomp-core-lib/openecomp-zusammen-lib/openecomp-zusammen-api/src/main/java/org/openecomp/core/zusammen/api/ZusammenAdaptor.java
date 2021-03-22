/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.core.zusammen.api;

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
import java.util.Optional;

public interface ZusammenAdaptor {

    Collection<Item> listItems(SessionContext context);

    Item getItem(SessionContext context, Id itemId);

    void deleteItem(SessionContext context, Id itemId);

    Id createItem(SessionContext context, Info info);

    void updateItem(SessionContext context, Id itemId, Info info);

    Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId);

    ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId);

    Id createVersion(SessionContext context, Id itemId, Id baseVersionId, ItemVersionData itemVersionData);

    void updateVersion(SessionContext context, Id itemId, Id versionId, ItemVersionData itemVersionData);

    ItemVersion getVersion(SessionContext context, Id itemId, Id versionId);

    ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId);

    ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId);

    void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag);

    void resetVersionHistory(SessionContext context, Id itemId, Id versionId, String version);

    void publishVersion(SessionContext context, Id itemId, Id versionId, String message);

    void syncVersion(SessionContext context, Id itemId, Id versionId);

    void forceSyncVersion(SessionContext context, Id itemId, Id versionId);

    void cleanVersion(SessionContext context, Id itemId, Id versionId);

    Optional<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext, Id elementId);

    Optional<Element> getElement(SessionContext context, ElementContext elementContext,
                                 String elementId); // TODO: 4/3/2017 change to Id

    Optional<Element> getElementByName(SessionContext context, ElementContext elementContext, Id parentElementId, String elementName);

    Collection<ElementInfo> listElements(SessionContext context, ElementContext elementContext, Id parentElementId);

    Collection<Element> listElementData(SessionContext context, ElementContext elementContext, Id parentElementId);

    /**
     * Lists the sub elements of the element named elementName which is a sub element of parentElementId
     */
    Collection<ElementInfo> listElementsByName(SessionContext context, ElementContext elementContext, Id parentElementId, String elementName);

    Optional<ElementInfo> getElementInfoByName(SessionContext context, ElementContext elementContext, Id parentElementId, String elementName);

    Optional<ElementConflict> getElementConflict(SessionContext context, ElementContext elementContext, Id elementId);

    Element saveElement(SessionContext context, ElementContext elementContext, ZusammenElement element, String message);

    void resolveElementConflict(SessionContext context, ElementContext elementContext, ZusammenElement element, Resolution resolution);

    void revert(SessionContext context, Id itemId, Id versionId, Id revisionId);

    ItemVersionRevisions listRevisions(SessionContext context, Id itemId, Id versionId);

    Collection<HealthInfo> checkHealth(SessionContext context);

    String getVersion(SessionContext context);
}
