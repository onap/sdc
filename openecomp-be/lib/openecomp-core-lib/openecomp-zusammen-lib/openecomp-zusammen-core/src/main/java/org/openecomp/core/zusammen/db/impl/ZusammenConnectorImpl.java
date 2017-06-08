package org.openecomp.core.zusammen.db.impl;

import com.amdocs.zusammen.adaptor.inbound.api.item.ElementAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.item.ItemAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.item.ItemVersionAdaptorFactory;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
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
import java.util.Optional;

public class ZusammenConnectorImpl implements ZusammenConnector {

  private ItemAdaptorFactory itemAdaptorFactory;
  private ItemVersionAdaptorFactory versionAdaptorFactory;
  private ElementAdaptorFactory elementAdaptorFactory;

  public ZusammenConnectorImpl(
      ItemAdaptorFactory itemAdaptorFactory,
      ItemVersionAdaptorFactory versionAdaptorFactory,
      ElementAdaptorFactory elementAdaptorFactory) {
    this.itemAdaptorFactory = itemAdaptorFactory;
    this.versionAdaptorFactory = versionAdaptorFactory;
    this.elementAdaptorFactory = elementAdaptorFactory;
    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
  }

  @Override
  public Collection<Item> listItems(SessionContext context) {
    Response<Collection<Item>> response =
        itemAdaptorFactory.createInterface(context).list(context);
    if (response.isSuccessful()) {
      return response.getValue();
    } else {
      return null;
    }
  }

  @Override
  public Id createItem(SessionContext context, Info info) {
    Response<Id> response = itemAdaptorFactory.createInterface(context).create(context, info);
    if (response.isSuccessful()) {
      return response.getValue();
    } else {
      throw new RuntimeException(
          "failed to create Item. message:" + response.getReturnCode().getMessage());
    }
  }

  @Override
  public void updateItem(SessionContext context, Id itemId, Info info) {
    Response<Void> response =
        itemAdaptorFactory.createInterface(context).update(context, itemId, info);

    if (!response.isSuccessful()) {
      throw new RuntimeException("failed to update Item . ItemId:" + itemId + "" +
          " message:" + response.getReturnCode().getMessage());
    }
  }

  @Override
  public Collection<ItemVersion> listVersions(SessionContext context, Id itemId) {
    Response<Collection<ItemVersion>> versions =
        versionAdaptorFactory.createInterface(context).list(context, Space.PRIVATE, itemId);
    if (!versions.isSuccessful()) {
      logErrorMessageToMdc(ItemElementLoggerTargetServiceName.ITEM_VERSION_RETRIEVAL, versions
          .getReturnCode());
      throw new RuntimeException(versions.getReturnCode().getMessage()); // TODO: 3/26/2017
    }
    return versions.getValue();
  }

  @Override
  public Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                          ItemVersionData itemVersionData) {
    Response<Id> response = versionAdaptorFactory.createInterface(context).create(context, itemId,
        baseVersionId, itemVersionData);
    if (response.isSuccessful()) {
      return response.getValue();
    } else {
      throw new RuntimeException("failed to create Item Version. ItemId:" + itemId + " based " +
          "on:" + baseVersionId +
          " message:" + response
          .getReturnCode().getMessage());
    }
  }

  @Override
  public void updateVersion(SessionContext context, Id itemId, Id versionId,
                            ItemVersionData itemVersionData) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .update(context, itemId, versionId, itemVersionData);
    if (!response.isSuccessful()) {
      throw new RuntimeException(
          String.format("failed to create Item Version. ItemId: %s, versionId: %s, message: %s",
              itemId.getValue(), versionId.getValue(), response.getReturnCode().getMessage()));
    }
  }

  @Override
  public void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .tag(context, itemId, versionId, null, tag);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to tag Item Version with tag %s. ItemId: %s, versionId: %s, message: %s",
          tag.getName(), itemId.getValue(), versionId.getValue(), response.getReturnCode()
              .getMessage()));
    }
  }

  @Override
  public void resetVersionHistory(SessionContext context, Id itemId, Id versionId,
                                  String changeRef) {
    Response<Void> response = versionAdaptorFactory.createInterface(context)
        .resetHistory(context, itemId, versionId, changeRef);
    if (!response.isSuccessful()) {
      throw new RuntimeException(String.format(
          "failed to reset Item Version back to %s. ItemId: %s, versionId: %s, message: %s",
          changeRef, itemId.getValue(), versionId.getValue(),
          response.getReturnCode().getMessage()));
    }
  }

  @Override
  public Collection<ElementInfo> listElements(SessionContext context,
                                              ElementContext elementContext,
                                              Id parentElementId) {
    Response<Collection<ElementInfo>> elementInfosResponse = elementAdaptorFactory
        .createInterface(context).list(context, elementContext, parentElementId);
    if (elementInfosResponse.isSuccessful()) {
      return elementInfosResponse.getValue();
    } else {
      logErrorMessageToMdc(ItemElementLoggerTargetServiceName.ELEMENT_GET_BY_PROPERTY,
          elementInfosResponse.getReturnCode());
      throw new RuntimeException(elementInfosResponse.getReturnCode().getMessage());
    }
  }

  @Override
  public Response<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext,
                                              Id elementId) {
    return elementAdaptorFactory.createInterface(context)
        .getInfo(context, elementContext, elementId);
  }


  @Override
  public Response<Element> getElement(SessionContext context, ElementContext elementContext,
                                      Id elementId) {
    return elementAdaptorFactory.createInterface(context).get(context, elementContext, elementId);
  }

  @Override
  public Optional<Element> saveElement(SessionContext context, ElementContext elementContext,
                                       ZusammenElement element, String message) {
    Response<Element> saveResponse = elementAdaptorFactory.createInterface(context)
        .save(context, elementContext, element, message);
    return saveResponse.isSuccessful()
        ? Optional.of(saveResponse.getValue())
        : Optional.empty(); // TODO: 3/21/2017 error?
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
