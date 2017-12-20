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
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.healing.dao.HealingDao;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.healing.types.HealCode;
import org.openecomp.sdc.healing.types.HealerType;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Talio on 11/29/2016.
 */
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
  public Optional<Version> healItemVersion(String itemId, Version version, ItemType itemType,
                                           boolean force) {
    String user = getUser();
    if (force || isPrivateHealingNeededByFlag(itemId, version.getId(), user)) {
      version = versioningManager.get(itemId, version);
      Version origVersion = version;
      if (version.getStatus() == VersionStatus.Certified) {
        Optional<Version> newVersion = createNewVersion(itemId, version);
        if (!newVersion.isPresent()) {
          // do NOT turn off flag here (in thought of saving version calculate performance next
          // time) because maybe next time the next version will be available (due to deletion of
          // the taken one)
          return Optional.empty();
        }
        version = newVersion.get();
      }

      doHeal(itemId, version, origVersion, itemType, user, force);
      return Optional.of(version);
    }
    return Optional.empty();
  }

  private void doHeal(String itemId, Version version, Version origVersion,
                      ItemType itemType, String user, boolean force) {
    Optional<String> privateFailureMessages =
        healPrivate(itemId, version, origVersion, getItemHealers(itemType), user);

    Optional<String> publicFailureMessages =
        force || origVersion.getStatus() == VersionStatus.Certified ||
            isPublicHealingNeededByFlag(itemId, origVersion.getId())
            ? healPublic(itemId, version, origVersion, getItemHealers(itemType), user)
            : Optional.empty();

    if (privateFailureMessages.isPresent() || publicFailureMessages.isPresent()) {
      throw new RuntimeException(
          publicFailureMessages.orElse("") + " " + privateFailureMessages.orElse(""));
    }
  }

  private Optional<String> healPrivate(String itemId, Version version, Version origVersion,
                                       Map<String, Map<String, String>> itemHealers, String user) {
    if (origVersion.getStatus() != VersionStatus.Certified) {
      itemHealers.remove(HealerType.structure.name());
    }

    Optional<String> privateHealingFailureMessages = executeHealers(itemId, version, itemHealers);
    markAsHealed(itemId, origVersion.getId(), user);
    return privateHealingFailureMessages;
  }

  private Optional<String> healPublic(String itemId, Version version, Version origVersion,
                                      Map<String, Map<String, String>> itemHealers, String user) {
    Optional<String> healingFailureMessages = origVersion.getStatus() == VersionStatus.Certified
        ? Optional.empty()
        : healPublic(itemId, version, itemHealers, user);

    markAsHealed(itemId, origVersion.getId(), PUBLIC_USER);
    return healingFailureMessages;
  }

  private Optional<String> healPublic(String itemId, Version version,
                                      Map<String, Map<String, String>> itemHealers, String user) {
    SessionContextProviderFactory.getInstance().createInterface()
        .create(user + HEALING_USER_SUFFIX);

    versioningManager.sync(itemId, version);

    Optional<String> healingFailureMessages = executeHealers(itemId, version, itemHealers);
    Version publicVersion = versioningManager.get(itemId, version);

    if (Objects.nonNull(publicVersion.getState()) && publicVersion.getState().isDirty()) {
      versioningManager.publish(itemId, version, "Healing vsp");
    }

    SessionContextProviderFactory.getInstance().createInterface().create(user);
    return healingFailureMessages;
  }

  private Optional<Version> createNewVersion(String itemId, Version version) {
    Version newVersion = new Version();
    newVersion.setBaseId(version.getId());
    try {
      return Optional.of(versioningManager.create(itemId, newVersion, VersionCreationMethod.major));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Object heal(String itemId, Version version, HealerType healerType, HealCode code,
                     ItemType itemType) {
    String healerClassName = getItemHealers(itemType).get(healerType.name()).get(code.name());
    ArrayList<String> healingFailureMessages = new ArrayList<>();

    Object result = executeHealer(itemId, version, healerClassName, healingFailureMessages);

    if (!healingFailureMessages.isEmpty()) {
      throw new RuntimeException(CommonMethods.listToSeparatedString(healingFailureMessages, '\n'));
    }
    return result;
  }

  private Optional<String> executeHealers(String itemId, Version version,
                                          Map<String, Map<String, String>> itemHealers) {
    List<String> healers = itemHealers.values().stream()
        .map(Map::values)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    List<String> healingFailureMessages = new ArrayList<>();
    for (String implClassName : healers) {
      executeHealer(itemId, version, implClassName, healingFailureMessages);
    }

    return healingFailureMessages.isEmpty()
        ? Optional.empty()
        : Optional.of(CommonMethods.listToSeparatedString(healingFailureMessages, '\n'));
  }


  private Object executeHealer(String itemId, Version version, String healerClassName,
                               List<String> healingFailureMessages) {
    Healer healer;
    try {
      healer = getHealerImplInstance(healerClassName);
    } catch (Exception e) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.SELF_HEALING, ErrorLevel.ERROR.name(), LoggerErrorCode
              .DATA_ERROR.getErrorCode(), LoggerErrorDescription.CANT_HEAL);
      healingFailureMessages
          .add(String.format(Messages.CANT_LOAD_HEALING_CLASS.getErrorMessage(),
              healerClassName));
      return null;
    }

    try {
      return healer.heal(itemId, version);
    } catch (Exception e) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.SELF_HEALING, ErrorLevel.ERROR.name(), LoggerErrorCode
              .DATA_ERROR.getErrorCode(), LoggerErrorDescription.CANT_HEAL);
      healingFailureMessages.add(e.getMessage() + " ,healer name :" + healerClassName);
    }
    return null;
  }

  private boolean isPrivateHealingNeededByFlag(String itemId, String version, String user) {
    Optional<Boolean> userHealingFlag = getHealingFlag(itemId, version, user);
    return userHealingFlag.isPresent()
        ? userHealingFlag.get()
        : isPublicHealingNeededByFlag(itemId, version);
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

  private Map<String, Map<String, String>> getItemHealers(ItemType itemType) {
    // TODO: 11/29/2017 create objects to hold this configuration +
    // load once from the json file and use the relevant healers (by itemType, healerType) as needed.
    Map healingConfig = FileUtils
        .readViaInputStream(HEALERS_BY_ENTITY_TYPE_FILE,
            stream -> JsonUtil.json2Object(stream, Map.class));
    return (Map<String, Map<String, String>>) healingConfig.get(itemType.name());
  }

  private Healer getHealerImplInstance(String implClassName)
      throws InstantiationException, IllegalAccessException, InvocationTargetException,
      NoSuchMethodException, ClassNotFoundException {
    return (Healer) Class.forName(implClassName).getConstructor().newInstance();
  }

  private String getUser() {
    return SessionContextProviderFactory.getInstance().createInterface().get().getUser()
        .getUserId();
  }
}
