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
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class ZusammenPluginUtil {

  public static String getSpaceName(SessionContext context, Space space) {
    switch (space) {
      case PUBLIC:
        return ZusammenPluginConstants.PUBLIC_SPACE;
      case PRIVATE:
        return ZusammenPluginUtil.getPrivateSpaceName(context);
      default:
        throw new IllegalArgumentException(String.format("Space %s is not supported.", space));
    }
  }

  public static String getPrivateSpaceName(SessionContext context) {
    return context.getUser().getUserName();
  }

  public static ElementEntity getElementEntity(CollaborationElement element) {
    ElementEntity elementEntity = new ElementEntity(element.getId());
    elementEntity.setNamespace(element.getNamespace());
    elementEntity.setParentId(element.getParentId() == null
        ? ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID
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
    return elementEntity;
  }

  public static CollaborationElement getCollaborationElement(
      ElementEntityContext elementEntityContext, ElementEntity elementEntity) {
    Id parentId =
        ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID.equals(elementEntity.getParentId())
            ? null
            : elementEntity.getParentId();
    CollaborationElement element = new CollaborationElement(elementEntityContext.getItemId(),
        elementEntityContext.getVersionId(), elementEntity.getNamespace(), elementEntity.getId());

    element.setParentId(parentId);
    element.setInfo(elementEntity.getInfo());
    element.setRelations(elementEntity.getRelations());

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
    element.setSubElements(elementEntity.getSubElementIds());
    return element;
  }
}
