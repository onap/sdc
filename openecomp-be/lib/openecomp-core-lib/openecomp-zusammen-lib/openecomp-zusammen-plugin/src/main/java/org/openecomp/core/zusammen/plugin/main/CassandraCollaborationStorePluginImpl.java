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

import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.commons.health.data.HealthStatus;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionHistory;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.datatypes.response.ZusammenException;
import com.amdocs.zusammen.sdk.collaboration.CollaborationStore;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationMergeResult;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationPublishResult;
import org.openecomp.core.zusammen.plugin.collaboration.ElementCollaborationStore;
import org.openecomp.core.zusammen.plugin.collaboration.VersionCollaborationStore;

import java.util.Collection;

public class CassandraCollaborationStorePluginImpl implements CollaborationStore {

  private VersionCollaborationStore versionCollaborationStore = new VersionCollaborationStore();
  private ElementCollaborationStore elementCollaborationStore = new ElementCollaborationStore();

  @Override
  public Response<Void> createItem(SessionContext context, Id id, Info info) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItem(SessionContext context, Id id) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> createItemVersion(SessionContext context, Id itemId, Id versionId, Id id2,
                                          ItemVersionData itemVersionData) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateItemVersion(SessionContext context, Id itemId, Id versionId,
                                          ItemVersionData itemVersionData) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteItemVersion(SessionContext context, Id itemId, Id versionId) {
    // done by state store
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> tagItemVersion(SessionContext context, Id itemId, Id versionId, Id changeId,
                                       Tag tag) {
    versionCollaborationStore.tagItemVersion(context, itemId, versionId, changeId, tag);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<CollaborationPublishResult> publishItemVersion(SessionContext context,
                                                                 Id itemId, Id versionId,
                                                                 String s) {
    throw new UnsupportedOperationException("publishItemVersion");
  }

  @Override
  public Response<CollaborationMergeResult> syncItemVersion(SessionContext context, Id id,
                                                            Id id1) {
    throw new UnsupportedOperationException("syncItemVersion");
  }

  @Override
  public Response<CollaborationMergeResult> mergeItemVersion(SessionContext context, Id id,
                                                             Id id1, Id id2) {
    throw new UnsupportedOperationException("mergeItemVersion");
  }

  @Override
  public Response<ItemVersionHistory> listItemVersionHistory(SessionContext context, Id id,
                                                             Id id1) {
    throw new UnsupportedOperationException("listItemVersionHistory");
  }

  @Override
  public Response<CollaborationMergeChange> resetItemVersionHistory(SessionContext context,
                                                                    Id itemId, Id versionId,
                                                                    String changeRef) {
    return new Response<>(versionCollaborationStore.resetItemVersionHistory(context, itemId, versionId, changeRef));
  }

  @Override
  public Response<Collection<CollaborationElement>> listElements(SessionContext context,
                                                                 ElementContext elementContext,
                                                                 Namespace namespace,
                                                                 Id elementId) {
    return new Response<>(
        elementCollaborationStore.listElements(context, elementContext, elementId));
  }

  @Override
  public Response<CollaborationElement> getElement(SessionContext context,
                                                   ElementContext elementContext,
                                                   Namespace namespace, Id elementId) {
    return new Response<>(elementCollaborationStore.getElement(context, elementContext, elementId));
  }

  @Override
  public Response<Void> createElement(SessionContext context, CollaborationElement element) {
    elementCollaborationStore.createElement(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> updateElement(SessionContext context, CollaborationElement element) {
    elementCollaborationStore.updateElement(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> deleteElement(SessionContext context, CollaborationElement element) {
    elementCollaborationStore.deleteElement(context, element);
    return new Response(Void.TYPE);
  }

  @Override
  public Response<Void> commitElements(SessionContext context, Id itemId, Id versionId, String s) {
    // not needed
    return new Response(Void.TYPE);
  }

  @Override
  public Response<HealthInfo> checkHealth(SessionContext sessionContext) throws ZusammenException {

    boolean health = elementCollaborationStore.checkHealth(sessionContext);
    HealthInfo healthInfo ;
    if (health){
      healthInfo = new HealthInfo("Collaboration", HealthStatus.UP,"");
    } else {
      healthInfo = new HealthInfo("Collaboration", HealthStatus.DOWN,"No Schema Available");
    }
    return new Response<HealthInfo>(healthInfo);

  }
}