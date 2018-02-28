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

package org.openecomp.sdc.healing.impl;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.healing.dao.HealingDao;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.healing.types.HealerType;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HealingManagerImpl implements HealingManager {

  private static final String HEALERS_BY_ENTITY_TYPE_FILE = "entityHealingConfiguration.json";
  private static final String HEALING_USER_SUFFIX = "_healer";
  private static final String PUBLIC_USER = "public";

  private HealingDao healingDao;
  private VersioningManager versioningManager;

  public HealingManagerImpl(VersioningManager versioningManager, HealingDao healingDao) {
    this.versioningManager = versioningManager;
    this.healingDao = healingDao;
  }

  @Override
  public Optional<Version> healItemVersion(final String itemId, final Version version,
                                           final ItemType itemType, final boolean force) {
    String user = getUser();
    if (force || isPrivateHealingNeededByFlag(itemId, version.getId(), user)) {

      Map<String, Collection<String>> healersByType = getItemHealers(itemType);
      List<String> failureMessages = new LinkedList<>();
      List<Healer> structureHealersToRun =
          getHealersToRun(healersByType.get(HealerType.structure.name()), itemId, version,
              failureMessages);
      List<Healer> dataHealersToRun =
          getHealersToRun(healersByType.get(HealerType.data.name()), itemId, version,
              failureMessages);

      if (structureHealersToRun.isEmpty() && dataHealersToRun.isEmpty()) {
        markAsHealed(itemId, version.getId(), user);
        markAsHealed(itemId, version.getId(), PUBLIC_USER);
        return Optional.empty();
      }

      Optional<Version> healVersion = getHealVersion(itemId, version);
      if (!healVersion.isPresent()) {
        // do NOT turn off flag here (in thought of saving version calculate performance next
        // time) because maybe next time the next version will be available (due to deletion of
        // the taken one)
        return Optional.empty();
      }

      failureMessages.addAll(
          doHeal(itemId, healVersion.get(), version, structureHealersToRun, dataHealersToRun, user,
              force));

      handleFailures(failureMessages);
      return healVersion;
    }
    return Optional.empty();
  }

  private Optional<Version> getHealVersion(String itemId, Version version) {
    version.setStatus(versioningManager.get(itemId, version).getStatus());
    return version.getStatus() == VersionStatus.Certified
        ? createNewVersion(itemId, version.getId())
        : Optional.of(version);
  }

  private Optional<Version> createNewVersion(String itemId, String versionId) {
    Version newVersion = new Version();
    newVersion.setBaseId(versionId);
    try {
      return Optional.of(versioningManager.create(itemId, newVersion, VersionCreationMethod.major));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private List<String> doHeal(String itemId, Version version, Version origVersion,
                              List<Healer> structureHealersToRun,
                              List<Healer> dataHealersToRun, String user,
                              boolean force) {
    List<String> failureMessages =
        force || origVersion.getStatus() == VersionStatus.Certified ||
            isPublicHealingNeededByFlag(itemId, origVersion.getId())
            ? healPublic(itemId, version, origVersion, structureHealersToRun, dataHealersToRun,
            user)
            : new LinkedList<>();

    failureMessages.addAll(
        healPrivate(itemId, version, origVersion, structureHealersToRun, dataHealersToRun, user));

    return failureMessages;
  }

  private List<String> healPrivate(String itemId, Version version, Version origVersion,
                                   List<Healer> structureHealersToRun,
                                   List<Healer> dataHealersToRun, String user) {
    List<String> failureMessages;
    if (origVersion.getStatus() == VersionStatus.Certified) {
      failureMessages = executeHealers(itemId, version,
          Stream.concat(structureHealersToRun.stream(), dataHealersToRun.stream())
              .collect(Collectors.toList()));
    } else {
      if (structureHealersToRun.isEmpty()) {
        failureMessages = executeHealers(itemId, version, dataHealersToRun);
      } else {
        versioningManager.forceSync(itemId, version);
        failureMessages = new LinkedList<>();
      }
    }
    markAsHealed(itemId, origVersion.getId(), user);
    return failureMessages;
  }

  private List<String> healPublic(String itemId, Version version, Version origVersion,
                                  List<Healer> structureHealersToRun,
                                  List<Healer> dataHealersToRun, String user) {
    List<String> failureMessages = origVersion.getStatus() == VersionStatus.Certified
        ? new LinkedList<>()
        : healPublic(itemId, version,
            Stream.concat(structureHealersToRun.stream(), dataHealersToRun.stream())
                .collect(Collectors.toList()), user);

    markAsHealed(itemId, origVersion.getId(), PUBLIC_USER);
    return failureMessages;
  }

  private List<String> healPublic(String itemId, Version version, List<Healer> healers,
                                  String user) {
    String tenant = SessionContextProviderFactory.getInstance().createInterface().get().getTenant();
    SessionContextProviderFactory.getInstance().createInterface()
        .create(user + HEALING_USER_SUFFIX, tenant);

    versioningManager.forceSync(itemId, version);

    List<String> failureMessages = executeHealers(itemId, version, healers);
    Version publicVersion = versioningManager.get(itemId, version);

    if (Objects.nonNull(publicVersion.getState()) && publicVersion.getState().isDirty()) {
      versioningManager.publish(itemId, version, "Healing vsp");
    }

    SessionContextProviderFactory.getInstance().createInterface().create(user, tenant);
    return failureMessages;
  }

  private List<String> executeHealers(String itemId, Version version, List<Healer> healers) {
    List<String> failureMessages = new LinkedList<>();
    for (Healer healer : healers) {
      try {
        healer.heal(itemId, version);
      } catch (Exception e) {
        failureMessages.add(
            String.format("Failure in healer %s: %s", healer.getClass().getName(), e.getMessage()));
      }
    }

    return failureMessages;
  }

  private boolean isPrivateHealingNeededByFlag(String itemId, String version, String user) {
    Optional<Boolean> userHealingFlag = getHealingFlag(itemId, version, user);
    return userHealingFlag.orElseGet(() -> isPublicHealingNeededByFlag(itemId, version));
  }

  private boolean isPublicHealingNeededByFlag(String itemId, String versionId) {
    Optional<Boolean> publicHealingFlag = getHealingFlag(itemId, versionId, PUBLIC_USER);
    return publicHealingFlag.isPresent() && publicHealingFlag.get();
  }

  private Optional<Boolean> getHealingFlag(String itemId, String version, String user) {
    return healingDao.getItemHealingFlag(user, itemId, version);
  }

  private void markAsHealed(String itemId, String versionId, String user) {
    healingDao.setItemHealingFlag(false, user, itemId, versionId);
  }

  private void handleFailures(List<String> failureMessages) {
    if (!failureMessages.isEmpty()) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withMessage(CommonMethods.listToSeparatedString(failureMessages, '\n')).build());
    }
  }

  private List<Healer> getHealersToRun(Collection<String> healersClassNames, String itemId,
                                       Version version, List<String> failureMessages) {
    return healersClassNames.stream()
        .map(healerClassName -> getHealerInstance(healerClassName, failureMessages))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(healer -> healer.isHealingNeeded(itemId, version))
        .collect(Collectors.toList());
  }

  private Optional<Healer> getHealerInstance(String healerClassName, List<String> failureMessages) {
    try {
      return Optional.of((Healer) Class.forName(healerClassName).getConstructor().newInstance());
    } catch (Exception e) {
      failureMessages
          .add(String.format(Messages.CANT_LOAD_HEALING_CLASS.getErrorMessage(), healerClassName));
      return Optional.empty();
    }
  }

  private Map<String, Collection<String>> getItemHealers(ItemType itemType) {
    Map healingConfig = FileUtils
        .readViaInputStream(HEALERS_BY_ENTITY_TYPE_FILE,
            stream -> JsonUtil.json2Object(stream, Map.class));
    return (Map<String, Collection<String>>) healingConfig.get(itemType.name());
  }

  private String getUser() {
    return SessionContextProviderFactory.getInstance().createInterface().get().getUser()
        .getUserId();
  }
}
