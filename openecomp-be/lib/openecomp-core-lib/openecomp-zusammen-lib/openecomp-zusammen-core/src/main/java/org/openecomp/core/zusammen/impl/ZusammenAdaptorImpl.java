package org.openecomp.core.zusammen.impl;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.*;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import com.amdocs.zusammen.datatypes.response.Response;
import com.amdocs.zusammen.datatypes.response.ReturnCode;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.db.ZusammenConnector;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;

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
  public Optional<ItemVersion> getFirstVersion(SessionContext context, Id itemId) {
    Collection<ItemVersion> versions = connector.listVersions(context, itemId);
    if(versions == null || versions.size()==0) {
      return Optional.empty();
    }
    List<ItemVersion> itemVersions = new ArrayList<>(versions);
    sortItemVersionListByModificationTimeDescOrder(itemVersions);
    ItemVersion itemVersion = itemVersions.iterator().next();

    return Optional.ofNullable(itemVersion);
  }

  @Override
  public Optional<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext,
                                              Id elementId) {
    Response<ElementInfo> response = connector.getElementInfo(context, elementContext, elementId);
    return response.isSuccessful() ? Optional.ofNullable(response.getValue()) : Optional.empty();
  }

  @Override
  public Optional<Element> getElement(SessionContext context, ElementContext elementContext,
                                      String elementId) {
    Response<Element> response = connector.getElement(context, elementContext, new Id(elementId));
    return response.isSuccessful() ? Optional.ofNullable(response.getValue()) : Optional.empty();
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
        .map(elementInfo -> getElement(context, elementContext, elementInfo.getId().getValue()))
        .orElse(Optional.empty());
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
    if (elementInfoList != null) {
      return elementInfoList.stream().map(elementInfo -> connector.getElement(context,
          elementContext, elementInfo.getId()).getValue()).collect(Collectors.toList());
    }

    return new ArrayList<>();

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
        elementInfo -> elementInfo.getInfo() != null &&
            elementName.equals(elementInfo.getInfo().getName()));
  }

  @Override
  public Optional<Element> saveElement(SessionContext context, ElementContext elementContext,
                                       ZusammenElement element, String message) {
    enrichElementHierarchyRec(context, elementContext, null, element);
    return connector.saveElement(context, elementContext, element, message);
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

  @Override
  public Id createItem(SessionContext context, Info info) {
    return connector.createItem(context, info);
  }

  @Override
  public Id createVersion(SessionContext context, Id itemId, Id baseVersionId, ItemVersionData
      itemVersionData) {
    return connector.createVersion(context, itemId, baseVersionId, itemVersionData);

  }

  @Override
  public Collection<Item> listItems(SessionContext context) {
    return connector.listItems(context);
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
  public void updateItem(SessionContext context, Id itemId, Info info) {
    connector.updateItem(context, itemId, info);
  }


  @Override
  public Collection<HealthInfo> checkHealth(SessionContext context) {
    return connector.checkHealth(context);
  }

  private static void sortItemVersionListByModificationTimeDescOrder(
      List<ItemVersion> itemVersions) {
    itemVersions.sort((o1, o2) -> ((Integer)o2.getId().getValue().length())
        .compareTo( (o1.getId().getValue().length())));
  }

  @Override
  public String getVersion(SessionContext sessionContext) {
    return connector.getVersion(sessionContext);
  }
}
