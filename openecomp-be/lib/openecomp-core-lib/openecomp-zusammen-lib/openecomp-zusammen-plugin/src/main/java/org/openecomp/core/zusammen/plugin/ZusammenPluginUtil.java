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

package org.openecomp.core.zusammen.plugin;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionChange;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementChange;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElementConflict;
import com.amdocs.zusammen.sdk.state.types.StateElement;
import com.amdocs.zusammen.sdk.types.ElementDescriptor;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import com.amdocs.zusammen.utils.fileutils.json.JsonUtil;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID;

public class ZusammenPluginUtil {

  public static String getSpaceName(SessionContext context, Space space) {
    switch (space) {
      case PUBLIC:
        return ZusammenPluginConstants.PUBLIC_SPACE;
      case PRIVATE:
        return getPrivateSpaceName(context);
      default:
        throw new IllegalArgumentException(String.format("Space %s is not supported.", space));
    }
  }

  public static String getPrivateSpaceName(SessionContext context) {
    return context.getUser().getUserName();
  }

  public static ElementContext getPrivateElementContext(ElementContext elementContext) {
    return new ElementContext(elementContext.getItemId(),elementContext.getVersionId(),Id.ZERO);
  }


  public static VersionEntity convertToVersionEntity(Id versionId, Id baseVersionId,
                                                     Date creationTime,
                                                     Date modificationTime) {

    return convertToVersionEntity(versionId, null, baseVersionId,
        creationTime, modificationTime);
  }

  public static VersionEntity convertToVersionEntity(Id versionId, Id revisionId, Id baseVersionId,
                                                     Date creationTime,
                                                     Date modificationTime) {
    VersionEntity version = new VersionEntity(versionId);
    version.setBaseId(baseVersionId);
    version.setCreationTime(creationTime);
    version.setModificationTime(modificationTime);
    return version;
  }

  public static ItemVersion convertToItemVersion(VersionEntity versionEntity,
                                                 ItemVersionData itemVersionData) {
    ItemVersion itemVersion = new ItemVersion();
    itemVersion.setId(versionEntity.getId());

    itemVersion.setBaseId(versionEntity.getBaseId());
    itemVersion.setCreationTime(versionEntity.getCreationTime());
    itemVersion.setModificationTime(versionEntity.getModificationTime());
    itemVersion.setData(itemVersionData);
    return itemVersion;
  }

  public static ElementEntity convertToElementEntity(CollaborationElement element) {

    ElementEntity elementEntity = new ElementEntity(element.getId());
    elementEntity.setNamespace(element.getNamespace());
    elementEntity.setParentId(element.getParentId() == null
        ? ROOT_ELEMENTS_PARENT_ID
        : element.getParentId());


    elementEntity.setInfo(element.getInfo());

    elementEntity.setRelations(element.getRelations());

    if (element.getData() != null) {
      elementEntity.setData(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
    }
    if (element.getSearchableData() != null) {
      elementEntity.setSearchableData(
          ByteBuffer.wrap(FileUtils.toByteArray(element.getSearchableData())));
    }
    if (element.getVisualization() != null) {
      elementEntity.setVisualization(
          ByteBuffer.wrap(FileUtils.toByteArray(element.getVisualization())));
    }
    elementEntity.setElementHash(new Id(calculateElementHash(elementEntity)));

    return elementEntity;
  }

  public static ElementDescriptor convertToElementDescriptor(
      ElementContext elementContext, ElementEntity elementEntity) {
    if (elementEntity == null) {
      return null;
    }
    ElementDescriptor element = new ElementDescriptor(elementContext.getItemId(),
        elementContext.getVersionId(), elementEntity.getNamespace(), elementEntity.getId());

    mapElementEntityToDescriptor(elementEntity, element);
    return element;
  }

  public static CollaborationElement convertToCollaborationElement(
      ElementContext elementContext, ElementEntity elementEntity) {
    CollaborationElement element = new CollaborationElement(elementContext.getItemId(),
        elementContext.getVersionId(), elementEntity.getNamespace(), elementEntity.getId());

    mapElementEntityToDescriptor(elementEntity, element);

    if (elementEntity.getData() != null) {
      element.setData(new ByteArrayInputStream(elementEntity.getData().array()));
    }
    if (elementEntity.getSearchableData() != null) {
      element.setSearchableData(
          new ByteArrayInputStream(elementEntity.getSearchableData().array()));
    }
    if (elementEntity.getVisualization() != null) {
      element.setVisualization(new ByteArrayInputStream(elementEntity.getVisualization().array()));
    }
    return element;
  }

  public static CollaborationElementChange convertToElementChange(
      ElementContext changedElementContext, ElementEntity changedElement, Action action) {
    CollaborationElementChange elementChange = new CollaborationElementChange();
    elementChange.setElement(convertToCollaborationElement(changedElementContext, changedElement));
    elementChange.setAction(action);
    return elementChange;
  }

  public static ItemVersionChange convertToVersionChange(ElementContext elementContext,
                                                         ElementEntity versionDataElement,
                                                         Action action) {
    ItemVersionChange versionChange = new ItemVersionChange();

    ItemVersion itemVersion = new ItemVersion();
    itemVersion.setId(elementContext.getVersionId());

    itemVersion.setData(convertToVersionData(versionDataElement));

    versionChange.setItemVersion(itemVersion);
    versionChange.setAction(action);
    return versionChange;
  }

  public static ItemVersionDataConflict getVersionConflict(ElementEntity localVesionData,
                                                           ElementEntity remoteVersionData) {
    ItemVersionDataConflict versionConflict = new ItemVersionDataConflict();
    versionConflict.setLocalData(convertToVersionData(localVesionData));
    versionConflict.setRemoteData(convertToVersionData(remoteVersionData));
    return versionConflict;
  }

  public static CollaborationElementConflict getElementConflict(ElementContext elementContext,
                                                                ElementEntity localElement,
                                                                ElementEntity remoteElement) {
    CollaborationElementConflict elementConflict = new CollaborationElementConflict();
    elementConflict
        .setLocalElement(convertToCollaborationElement(elementContext, localElement));
    elementConflict.setRemoteElement(
        convertToCollaborationElement(elementContext, remoteElement));
    return elementConflict;
  }

  public static ItemVersionData convertToVersionData(ElementEntity versionDataElement) {
    ItemVersionData versionData = new ItemVersionData();
    versionData.setInfo(versionDataElement.getInfo());
    versionData.setRelations(versionDataElement.getRelations());
    return versionData;
  }

  private static void mapElementEntityToDescriptor(ElementEntity elementEntity,
                                                   ElementDescriptor elementDescriptor) {
    Id parentId = ROOT_ELEMENTS_PARENT_ID.equals(elementEntity.getParentId())
        ? null
        : elementEntity.getParentId();

    elementDescriptor.setParentId(parentId);
    elementDescriptor.setInfo(elementEntity.getInfo());
    elementDescriptor.setRelations(elementEntity.getRelations());
    elementDescriptor.setSubElements(elementEntity.getSubElementIds());
  }

  public static String calculateElementHash(ElementEntity elementEntity) {
    StringBuffer elementHash = new StringBuffer();
    if (elementEntity.getData() != null) {
      elementHash.append(calculateSHA1(elementEntity.getData().array()));
    } else {
      elementHash.append("0");
    }
    elementHash.append("_");
    if (elementEntity.getVisualization() != null) {
      elementHash.append(calculateSHA1(elementEntity.getVisualization().array()));
    } else {
      elementHash.append("0");
    }
    elementHash.append("_");

    if (elementEntity.getSearchableData() != null) {
      elementHash.append(calculateSHA1(elementEntity.getSearchableData().array()));
    } else {
      elementHash.append("0");
    }
    elementHash.append("_");

    if (elementEntity.getInfo() != null) {
      elementHash.append(calculateSHA1(JsonUtil.object2Json(elementEntity.getInfo()).getBytes()));
    } else {
      elementHash.append("0");
    }
    elementHash.append("_");

    if (elementEntity.getRelations() != null) {
      elementHash
          .append(calculateSHA1(JsonUtil.object2Json(elementEntity.getRelations()).getBytes()));
    } else {
      elementHash.append("0");
    }

    return elementHash.toString();
  }

  private static String calculateSHA1(byte[] content2Convert) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return Base64.getEncoder().encodeToString(md.digest(content2Convert));
  }


  public static StateElement getStateElement(ElementContext elementContext, ElementEntity
      elementEntity) {
    Id parentId = ROOT_ELEMENTS_PARENT_ID.equals(elementEntity.getParentId())
        ? null
        : elementEntity.getParentId();
    StateElement element = new StateElement(elementContext.getItemId(),
        elementContext.getVersionId(), elementEntity.getNamespace(), elementEntity.getId());

    element.setParentId(parentId);
    element.setInfo(elementEntity.getInfo());
    element.setRelations(elementEntity.getRelations());
    element.setSubElements(elementEntity.getSubElementIds());
    return element;
  }


}
