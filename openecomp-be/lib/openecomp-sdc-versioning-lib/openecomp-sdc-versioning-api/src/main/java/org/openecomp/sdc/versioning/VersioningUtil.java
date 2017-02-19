/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.versioning;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;
import org.openecomp.sdc.versioning.errors.RequestedVersionInvalidErrorBuilder;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Versioning util.
 */
public class VersioningUtil {

  /**
   * Validate entity existence.
   *
   * @param <T>                   the type parameter
   * @param retrievedEntity       the retrieved entity
   * @param inputEntity           the input entity
   * @param firstClassCitizenType the first class citizen type
   */
  public static <T extends VersionableEntity> void validateEntityExistence(Object retrievedEntity,
                                                                 T inputEntity,
                                                                 String firstClassCitizenType) {
    if (retrievedEntity == null) {
      throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
          inputEntity.getEntityType(),
          inputEntity.getId(),
          firstClassCitizenType,
          inputEntity.getFirstClassCitizenId(),
          inputEntity.getVersion()).build());
    }
  }

  /**
   * Validate entities existence.
   *
   * @param <T>                   the type parameter
   * @param <D>                   the type parameter
   * @param entityIds             the entity ids
   * @param entity                the entity
   * @param entityDao             the entity dao
   * @param firstClassCitizenType the first class citizen type
   */
  public static <T extends VersionableEntity, D extends BaseDao<T>> void validateEntitiesExistence(
      Set<String> entityIds, T entity, D entityDao, String firstClassCitizenType) {
    if (entityIds == null) {
      return;
    }

    List<String> nonExistingIds = new ArrayList<>();
    for (String entityId : entityIds) {
      entity.setId(entityId);
      if (entityDao.get(entity) == null) {
        nonExistingIds.add(entityId);
      }
    }

    if (nonExistingIds.size() > 0) {
      if (nonExistingIds.size() == 1) {
        throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
            entity.getEntityType(),
            nonExistingIds.get(0),
            firstClassCitizenType,
            entity.getFirstClassCitizenId(),
            entity.getVersion()).build());
      }
      throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
          entity.getEntityType(),
          nonExistingIds,
          firstClassCitizenType,
          entity.getFirstClassCitizenId(),
          entity.getVersion()).build());
    }
  }

  /**
   * Validate contained entities existence.
   *
   * @param <T>                         the type parameter
   * @param containedEntityType         the contained entity type
   * @param inputContainedEntityIds     the input contained entity ids
   * @param containingEntity            the containing entity
   * @param retrievedContainedEntityIds the retrieved contained entity ids
   */
  public static <T extends VersionableEntity> void validateContainedEntitiesExistence(
      String containedEntityType, Set<String> inputContainedEntityIds, T containingEntity,
      Set<String> retrievedContainedEntityIds) {
    if (inputContainedEntityIds == null) {
      return;
    }

    List<String> nonExistingIds = inputContainedEntityIds.stream()
        .filter(entityId -> !retrievedContainedEntityIds.contains(entityId))
        .collect(Collectors.toList());

    if (nonExistingIds.size() > 0) {
      if (nonExistingIds.size() == 1) {
        throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
            containedEntityType,
            nonExistingIds.get(0),
            containingEntity.getEntityType(),
            containingEntity.getId(),
            containingEntity.getVersion()).build());
      }
      throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
          containedEntityType,
          nonExistingIds,
          containingEntity.getEntityType(),
          containingEntity.getId(),
          containingEntity.getVersion()).build());
    }
  }

  /**
   * Resolve version version.
   *
   * @param requestedVersion the requested version
   * @param versionInfo      the version info
   * @param finalOnly        the final only
   * @return the version
   */
  public static Version resolveVersion(Version requestedVersion, VersionInfo versionInfo,
                                       boolean finalOnly) {
    if (requestedVersion == null) {
      if (finalOnly) {
        if (versionInfo.getLatestFinalVersion() == null) {
          throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
        }
        requestedVersion = versionInfo.getLatestFinalVersion();
      } else {
        requestedVersion = versionInfo.getActiveVersion();
      }
    } else {
      if ((finalOnly && !requestedVersion.isFinal())
          || !versionInfo.getViewableVersions().contains(requestedVersion)) {
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }
    return requestedVersion;
  }

  /**
   * Resolve version version.
   *
   * @param requestedVersion the requested version
   * @param versionInfo      the version info
   * @return the version
   */
  public static Version resolveVersion(Version requestedVersion, VersionInfo versionInfo) {
    if (requestedVersion == null) {
      requestedVersion = versionInfo.getActiveVersion();
    } else {
      if (!versionInfo.getViewableVersions().contains(requestedVersion)) {
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }
    return requestedVersion;
  }
}
