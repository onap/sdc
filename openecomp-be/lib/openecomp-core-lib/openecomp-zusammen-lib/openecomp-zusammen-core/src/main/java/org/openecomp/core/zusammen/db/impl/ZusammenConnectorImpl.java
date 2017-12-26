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

package org.openecomp.core.zusammen.db.impl;

import com.amdocs.zusammen.adaptor.inbound.api.health.HealthAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.item.ElementAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.item.ItemAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.item.ItemVersionAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.MergeResult;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.datatypes.response.ReturnCode;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.core.zusammen.impl.ItemElementLoggerTargetServiceName;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;

import java.util.Collection;

public class ZusammenConnectorImpl implements ZusammenConnector {

  private static final String GET_ELEMENT_ERR_MSG =
      "Failed to get element. Item Id: %s, version Id: %s, element Id: %s message: %s";
  private static final String GET_ELEMENT_IN_REV_ERR_MSG =
      "Failed to get element. Item Id: %s, version Id: %s, revision Id: %s, element Id: %s message: %s";
  private ItemAdaptorFactory itemAdaptorFactory;
  private ItemVersionAdaptorFactory versionAdaptorFactory;
  private ElementAdaptorFactory elementAdaptorFactory;
  private HealthAdaptorFactory healthAdaptorFactory;

  public ZusammenConnectorImpl(
      ItemAdaptorFactory itemAdaptorFactory,
      ItemVersionAdaptorFactory versionAdaptorFactory,
      ElementAdaptorFactory elementAdaptorFactory,
      HealthAdaptorFactory healthAdaptorFactory) {
    this.itemAdaptorFactory = itemAdaptorFactory;
    this.versionAdaptorFactory = versionAdaptorFactory;
    this.elementAdaptorFactory = elementAdaptorFactory;
    this.healthAdaptorFactory = healthAdaptorFactory;
    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
  }

  @Override
  public Collection<HealthInfo> checkHealth(SessionContext sessionContext) {
    return healthAdaptorFactory.createInterface(sessionContext).getHealthStatus(sessionContext);
  }

  @Override
  public String getVersion(SessionContext sessionContext) {
    return healthAdaptorFactory.createInterface(sessionContext).getVersion();
  }

  @Override
  public Collection<Item> listItems(SessionContext context) {
    Response<Collection<Item>> response = itemAdaptorFactory.createInterface(context).list(context);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          "Failed to list Items. message:" + response.getReturnCode().toString());
    }
    return response.getValue();
  }

  @Override
  public Item getItem(SessionContext context, Id itemId) {
    Response<Item> response = itemAdaptorFactory.createInterface(context).get(context, itemId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          "Failed to get Item. message:" + response.getReturnCode().toString());
    }
    return response.getValue();
  }

  @Override
  public Id createItem(SessionContext context, Info info) {
    Response<Id> response = itemAdaptorFactory.createInterface(context).create(context, info);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          "Failed to create Item. message:" + response.getReturnCode().toString());
    }
    return response.getValue();
  }

  @Override
  public void updateItem(SessionContext context, Id itemId, Info info) {
    Response<Void> response =
        itemAdaptorFactory.createInterface(context).update(context, itemId, info);

    if (!response.isSuccessful()) {
      throw new RuntimeException("failed to update Item . ItemId:" + itemId + "" +
          " message:" + response.getReturnCode().toString());
    }
  }

  @Override
  public Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId) {
    Response<Collection<ItemVersion>> versions =
        versionAdaptorFactory.createInterface(context).list(context, Space.PUBLIC, itemId);
    if (!versions.isSuccessful()) {
      logErrorMessageToMdc(ItemElementLoggerTargetServiceName.ITEM_VERSION_RETRIEVAL, versions
          .getReturnCode());
      throw new RuntimeException(versions.getReturnCode().toString());
    }
    return versions.getValue();
  }

  @Override
  public ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId) {
    Response<ItemVersion> response = versionAdaptorFactory.createInterface(context)
        .get(context, Space.PUBLIC, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          String.format("failed to get public Item Version. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                          ItemVersionData itemVersionData) {
    Response<Id> response = versionAdaptorFactory.createInterface(context).create(context, itemId,
        baseVersionId, itemVersionData);
    if (response.isSuccessful()) {
      return response.getValue();
    } else {
      throw new RuntimeException(String.format(
          "failed to create Item Version. ItemId: %s, base versionId: %s, message: %s",
          itemId.getValue(), baseVersionId.getValue(), response.getReturnCode().toString()));
    }
  }

  @Override
  public void updateVersion(SessionContext context, Id itemId, Id versionId,
                            ItemVersionData itemVersionData) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .update(context, itemId, versionId, itemVersionData);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          String.format("failed to update Item Version. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
  }

  @Override
  public ItemVersion getVersion(SessionContext context, Id itemId, Id versionId) {
    Response<ItemVersion> response = versionAdaptorFactory.createInterface(context)
        .get(context, Space.PRIVATE, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          String.format("failed to get Item Version. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId) {
    Response<ItemVersionStatus> response =
        versionAdaptorFactory.createInterface(context).getStatus(context, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          String.format("failed to get Item Version status. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .tag(context, itemId, versionId, null, tag);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to tag Item Version with tag %s. ItemId: %s, versionId: %s, message: %s",
          tag.getName(), itemId.getValue(), versionId.getValue(),
          response.getReturnCode().toString()));
    }
  }

  @Override
  public void resetVersionRevision(SessionContext context, Id itemId, Id versionId,
                                   Id revisionId) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .resetRevision(context, itemId, versionId, revisionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to reset Item Version back to revision: %s. ItemId: %s, versionId: %s, message:" +
              " %s",
          revisionId.getValue(), itemId.getValue(), versionId.getValue(),
          response.getReturnCode().toString()));
    }
  }

  @Override
  public void revertVersionRevision(SessionContext context, Id itemId, Id versionId,
                                    Id revisionId) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .revertRevision(context, itemId, versionId, revisionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to revert Item Version back to revision: %s. ItemId: %s, versionId: %s, " +
              "message: %s",
          revisionId.getValue(), itemId.getValue(), versionId.getValue(),
          response.getReturnCode().toString()));
    }
  }

  @Override
  public ItemVersionRevisions listVersionRevisions(SessionContext context, Id itemId,
                                                   Id versionId) {
    Response<ItemVersionRevisions> response =
        versionAdaptorFactory.createInterface(context)
            .listRevisions(context, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to list revisions of Item Version. ItemId: %s, versionId: %s, message: %s",
          itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }


  @Override
  public void publishVersion(SessionContext context, Id itemId, Id versionId, String message) {
    Response<Void> response =
        versionAdaptorFactory.createInterface(context).publish(context, itemId, versionId, message);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to publish item Version. ItemId: %s, versionId: %s, message: %s",
          itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
  }

  @Override
  public void syncVersion(SessionContext context, Id itemId, Id versionId) {
    Response<MergeResult> response =
        versionAdaptorFactory.createInterface(context).sync(context, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to sync item Version. ItemId: %s, versionId: %s, message: %s",
          itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
  }

  @Override
  public void forceSyncVersion(SessionContext context, Id itemId, Id versionId) {
    Response<MergeResult> response =
        versionAdaptorFactory.createInterface(context).forceSync(context, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to force sync item Version. ItemId: %s, versionId: %s, message: %s",
          itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
  }

  @Override
  public ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId) {
    Response<ItemVersionConflict> response =
        versionAdaptorFactory.createInterface(context).getConflict(context, itemId, versionId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String
          .format("failed to get Item Version conflict. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public Collection<ElementInfo> listElements(SessionContext context,
                                              ElementContext elementContext,
                                              Id parentElementId) {
    Response<Collection<ElementInfo>> response = elementAdaptorFactory
        .createInterface(context).list(context, elementContext, parentElementId);
    if (response.isSuccessful()) {
      return response.getValue();
    } else {
      logErrorMessageToMdc(ItemElementLoggerTargetServiceName.ELEMENT_GET_BY_PROPERTY,
          response.getReturnCode());
      throw new RuntimeException(response.getReturnCode().toString());
    }
  }


  @Override
  public ElementInfo getElementInfo(SessionContext context, ElementContext elementContext,
                                    Id elementId) {
    Response<ElementInfo> response =
        elementAdaptorFactory.createInterface(context).getInfo(context, elementContext, elementId);
    if (!response.isSuccessful()) {
      throw buildGetElementException(elementContext, elementId,
          response.getReturnCode().toString());

    }
    return response.getValue();
  }

  @Override
  public Element getElement(SessionContext context, ElementContext elementContext,
                            Id elementId) {
    Response<Element> response =
        elementAdaptorFactory.createInterface(context).get(context, elementContext, elementId);
    if (!response.isSuccessful()) {
      throw buildGetElementException(elementContext, elementId,
          response.getReturnCode().toString());
    }
    return response.getValue();
  }

  @Override
  public ElementConflict getElementConflict(SessionContext context, ElementContext elementContext,
                                            Id elementId) {
    Response<ElementConflict> response = elementAdaptorFactory.createInterface(context)
        .getConflict(context, elementContext, elementId);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "Failed to get element conflict. Item Id: %s, version Id: %s, element Id: %s message: %s",
          elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
          elementId.getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public Element saveElement(SessionContext context, ElementContext elementContext,
                             ZusammenElement element, String message) {
    Response<Element> response = elementAdaptorFactory.createInterface(context)
        .save(context, elementContext, element, message);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String
          .format("Failed to create element %s. ItemId: %s, versionId: %s, message: %s",
              element.getElementId().getValue(), elementContext.getItemId().getValue(),
              elementContext.getVersionId().getValue(), response.getReturnCode().toString()));
    }
    return response.getValue();
  }

  @Override
  public void resolveElementConflict(SessionContext context, ElementContext elementContext,
                                     ZusammenElement element,
                                     Resolution resolution) {
    Response<Void> response = elementAdaptorFactory.createInterface(context)
        .resolveConflict(context, elementContext, element, resolution);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          "Failed to resolve conflict. message:" + response.getReturnCode().toString());
    }
  }

  @Override
  public void resetVersionHistory(SessionContext context, Id itemId, Id versionId,
                                  String revisionId) {
    // no-op, required by the interface
  }

  private RuntimeException buildGetElementException(ElementContext elementContext, Id elementId,
                                                    String zusammenErrorMessage) {
    if (elementContext.getRevisionId() == null) {
      return new RuntimeException(String.format(GET_ELEMENT_ERR_MSG,
          elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
          elementId.getValue(), zusammenErrorMessage));
    }
    return new RuntimeException(String.format(GET_ELEMENT_IN_REV_ERR_MSG,
        elementContext.getItemId().getValue(), elementContext.getVersionId().getValue(),
        elementContext.getRevisionId().getValue(),
        elementId.getValue(), zusammenErrorMessage));
  }

  private void logErrorMessageToMdc(ItemElementLoggerTargetServiceName
                                        itemElementLoggerTargetServiceName,
                                    ReturnCode returnCode) {
    logErrorMessageToMdc(itemElementLoggerTargetServiceName, returnCode.toString());
  }

  private void logErrorMessageToMdc(ItemElementLoggerTargetServiceName
                                        itemElementLoggerTargetServiceName,
                                    String message) {
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        itemElementLoggerTargetServiceName.getDescription(),
        ErrorLevel.ERROR.name(),
        LoggerErrorCode.BUSINESS_PROCESS_ERROR.getErrorCode(),
        message);
  }


}
