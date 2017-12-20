/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.zusammen.plugin.main;


import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.plugin.statestore.cassandra.StateStoreImpl;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.state.types.StateElement;
import org.openecomp.core.zusammen.plugin.ZusammenPluginUtil;
import org.openecomp.core.zusammen.plugin.collaboration.ElementPrivateStore;
import org.openecomp.core.zusammen.plugin.collaboration.impl.ElementPrivateStoreImpl;
import org.openecomp.core.zusammen.plugin.dao.ElementRepository;
import org.openecomp.core.zusammen.plugin.dao.ElementRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.VersionDao;
import org.openecomp.core.zusammen.plugin.dao.VersionDaoFactory;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionDataElement;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.convertToItemVersion;
import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class CassandraStateStorePluginImpl extends StateStoreImpl {

  private ElementPrivateStore elementPrivateStore = new ElementPrivateStoreImpl();

  @Override
  public Response<Collection<ItemVersion>> listItemVersions(SessionContext context, Space space,
                                                            Id itemId) {
    String spaceName = getSpaceName(context, space);
    return new Response<>(getVersionDao(context).list(context, spaceName, itemId).stream()
        .map(versionEntity -> getItemVersion(context, spaceName, itemId, versionEntity))
        .collect(Collectors.toList()));
  }

  @Override
  public Response<Boolean> isItemVersionExist(SessionContext context, Space space, Id itemId,
                                              Id versionId) {
    return new Response<>(
        getVersionDao(context).get(context, getSpaceName(context, space), itemId, versionId)
            .isPresent());
  }

  @Override
  public Response<ItemVersion> getItemVersion(SessionContext context, Space space, Id itemId,
                                              Id versionId) {
    String spaceName = getSpaceName(context, space);
    return new Response<>(getVersionDao(context).get(context, spaceName, itemId, versionId)
        .map(versionEntity -> getItemVersion(context, spaceName, itemId, versionEntity))
        .orElse(null));
  }

  @Override
  public Response<Void> createItemVersion(SessionContext context, Space space, Id itemId,
                                          Id baseVersionId, Id versionId, ItemVersionData data,
                                          Date creationTime) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateItemVersion(SessionContext context, Space space, Id itemId,
                                          Id versionId, ItemVersionData data,
                                          Date modificationTime) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Space space, Id itemId,
                                          Id versionId) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> createElement(SessionContext context, StateElement element) {
    ElementEntity elementEntity = new ElementEntity(element.getId());
    elementEntity.setNamespace(element.getNamespace());

    ElementRepositoryFactory.getInstance().createInterface(context)
        .createNamespace(context,
            new ElementEntityContext(getSpaceName(context, element.getSpace()),
                element.getItemId(), element.getVersionId()), elementEntity);
    // create element is done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateElement(SessionContext context, StateElement element) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteElement(SessionContext context, StateElement element) {
    // done by collaboration store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Collection<StateElement>> listElements(SessionContext context,
                                                         ElementContext elementContext,
                                                         Id elementId) {
    return new Response(elementPrivateStore.listSubs(context, elementContext, elementId).stream()
        .map(elementEntity -> ZusammenPluginUtil.getStateElement(elementContext, elementEntity))
        .collect(Collectors.toList()));

  }

  @Override
  public Response<StateElement> getElement(SessionContext context, ElementContext elementContext,
                                           Id elementId) {

    return new Response(elementPrivateStore.get(context, elementContext, elementId)
        .map(elementEntity -> ZusammenPluginUtil
            .getStateElement(elementContext, elementEntity))
        .orElse
            (null));


  }

  private ItemVersion getItemVersion(SessionContext context, String spaceName, Id itemId,
                                     VersionEntity versionEntity) {

    ItemVersionData itemVersionData = getElementRepository(context)
        .get(context, new ElementEntityContext(spaceName, itemId, versionEntity.getId(), null),
            new VersionDataElement())
        .map(ZusammenPluginUtil::convertToVersionData)
        .orElseThrow(() -> new IllegalStateException("Version must have data"));

    return convertToItemVersion(versionEntity, itemVersionData);
  }

  protected VersionDao getVersionDao(SessionContext context) {
    return VersionDaoFactory.getInstance().createInterface(context);
  }

  protected ElementRepository getElementRepository(SessionContext context) {
    return ElementRepositoryFactory.getInstance().createInterface(context);
  }
}
