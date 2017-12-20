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

package org.openecomp.sdc.action.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.ActionManager;
import org.openecomp.sdc.action.dao.ActionArtifactDao;
import org.openecomp.sdc.action.dao.ActionArtifactDaoFactory;
import org.openecomp.sdc.action.dao.ActionDao;
import org.openecomp.sdc.action.dao.ActionDaoFactory;
import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.errors.ActionErrorConstants;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.logging.StatusCode;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.ActionArtifactProtection;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.openecomp.sdc.action.types.OpenEcompComponent;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDaoFactory;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.errors.EntityNotExistErrorBuilder;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.openecomp.sdc.action.ActionConstants.ACTION_VERSIONABLE_TYPE;
import static org.openecomp.sdc.action.ActionConstants.ARTIFACT_METADATA_ATTR_NAME;
import static org.openecomp.sdc.action.ActionConstants.ARTIFACT_METADATA_ATTR_UUID;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_CATEGORY;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_MODEL;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_NAME;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_NONE;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_OPEN_ECOMP_COMPONENT;
import static org.openecomp.sdc.action.ActionConstants.FILTER_TYPE_VENDOR;
import static org.openecomp.sdc.action.ActionConstants.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.action.ActionConstants.STATUS;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY_API;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY_DB;
import static org.openecomp.sdc.action.ActionConstants.UNIQUE_ID;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ALREADY_EXISTS;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ALREADY_EXISTS_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_DELETE_READ_ONLY_MSG;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_NAME_INVALID;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY_MSG;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKIN_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_DELETE_ON_LOCKED_ENTITY_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_NOT_EXIST;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_ERROR;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ENTITY_UNIQUE_VALUE_MSG;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_REQUESTED_VERSION_INVALID;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_INVALID_VERSION;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_CODE_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_NOT_ALLOWED_FOR_NAME;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_ON_UNLOCKED_ENTITY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_UPDATE_PARAM_INVALID;
import static org.openecomp.sdc.action.util.ActionUtil.actionLogPostProcessor;
import static org.openecomp.sdc.action.util.ActionUtil.actionLogPreProcessor;
import static org.openecomp.sdc.action.util.ActionUtil.getCurrentTimeStampUtc;
import static org.openecomp.sdc.versioning.dao.types.Version.VERSION_STRING_VIOLATION_MSG;

/**
 * Manager Implementation for {@link ActionManager Action Library Operations} <br> Handles Business
 * layer validations and acts as an interface between the REST and DAO layers.
 */
public class ActionManagerImpl implements ActionManager {

  private static final ActionDao actionDao = ActionDaoFactory.getInstance().createInterface();
  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private static final ActionArtifactDao actionArtifactDao =
      ActionArtifactDaoFactory.getInstance().createInterface();
  private static VersionInfoDao versionInfoDao =
      VersionInfoDaoFactory.getInstance().createInterface();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public ActionManagerImpl() {
    actionDao.registerVersioning(ACTION_VERSIONABLE_TYPE);
  }

  /**
   * List All Major, Last Minor and Candidate version (if any) for Given Action Invariant UUID
   *
   * @param invariantId Invariant UUID of the action for which the information is required
   * @return List of All Major, Last Minor and Candidate version if any Of {@link Action} with given
     actionInvariantUuId.
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred during the operation
   */

  @Override
  public List<Action> getActionsByActionInvariantUuId(String invariantId) throws ActionException {
    List<Action> actions;

    log.debug(" entering getActionsByActionInvariantUuId with  invariantID = " + invariantId);
    actions = actionDao
        .getActionsByActionInvariantUuId(invariantId != null ? invariantId.toUpperCase() : null);

    if (actions != null && actions.isEmpty()) {
      throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
    }

    log.debug(" exit getActionsByActionInvariantUuId with  invariantID = " + invariantId);
    return actions;
  }

  /**
   * Get list of actions based on a filter criteria. If no filter is sent all actions will be
   * returned
   *
   * @param filterType  Filter by Vendor/Category/Model/Component/None
   * @param filterValue Filter Parameter Value (Vendor ID/Category ID/Model ID/Component ID)
   * @return List of {@link Action} objects based on a filter criteria <br> Empty List if no records
     match the provided filter criteria
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public List<Action> getFilteredActions(String filterType, String filterValue)
      throws ActionException {
    List<Action> actions;
    log.debug(" entering getFilteredActions By filterType = " + filterType + " With value = "
        + filterValue);
    switch (filterType) {
      case FILTER_TYPE_NONE:
        //Business validation for OPENECOMP Component type fetch (if any)
        break;
      case FILTER_TYPE_VENDOR:
        //Business validation for vendor type fetch (if any)
        break;
      case FILTER_TYPE_CATEGORY:
        //Business validation for Category type fetch (if any)
        break;
      case FILTER_TYPE_MODEL:
        //Business validation for model type fetch (if any)
        break;
      case FILTER_TYPE_OPEN_ECOMP_COMPONENT:
        //Business validation for OPENECOMP Component type fetch (if any)
        break;
      case FILTER_TYPE_NAME:
        actions = actionDao
            .getFilteredActions(filterType, filterValue != null ? filterValue.toLowerCase() : null);
        if (actions != null && actions.isEmpty()) {
          throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
        }
        log.debug(" exit getFilteredActions By filterType = " + filterType + " With value = "
            + filterValue);
        return actions;
      default:
        break;
    }
    actions = actionDao
        .getFilteredActions(filterType, filterValue != null ? filterValue.toLowerCase() : null);
    List<Action> majorMinorVersionList = getMajorMinorVersionActions(actions);
    Collections.sort(majorMinorVersionList);
    log.debug(
        " exit getFilteredActions By filterType = " + filterType + " With value = " + filterValue);
    return majorMinorVersionList;
  }

  /**
   * Get the properties of an action version by its UUID.
   *
   * @param actionUuId UUID of the specific action version
   * @return {@link Action} object corresponding the version represented by the UUID
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action getActionsByActionUuId(String actionUuId) throws ActionException {
    log.debug(" entering getActionsByActionUuId with  actionUUID = " + actionUuId);
    Action action =
        actionDao.getActionsByActionUuId(actionUuId != null ? actionUuId.toUpperCase() : null);

    if (action == null) {
      throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
    }

    log.debug(" exit getActionsByActionUuId with  actionUUID = " + actionUuId);
    return action;
  }

  /**
   * List OPENECOMP Components supported by Action Library.
   *
   * @return List of {@link OpenEcompComponent} objects supported by Action Library <br> Empty List if
     no components are found
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public List<OpenEcompComponent> getOpenEcompComponents() throws ActionException {
    return actionDao.getOpenEcompComponents();
  }


  /**
   * Delete an action.
   *
   * @param actionInvariantUuId Invariant UUID of the action to be deleted
   * @param user                User id of the user performing the operation
   */
  @Override
  public void deleteAction(String actionInvariantUuId, String user) throws ActionException {
    try {
      log.debug("entering deleteAction with actionInvariantUuId = " + actionInvariantUuId
          + " and user = " + user);
      actionLogPreProcessor(ActionSubOperation.DELETE_ACTION, TARGET_ENTITY_API);
      versioningManager.delete(ACTION_VERSIONABLE_TYPE, actionInvariantUuId, user);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      actionDao.deleteAction(actionInvariantUuId);
    } catch (CoreException ce) {
      formAndThrowException(ce);
    }
  }

  /**
   * Create a new Action.
   *
   * @param action Action object model of the user request for creating an action
   * @param user   AT&T id of the user sending the create request
   * @return {@link Action} model object for the created action
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action createAction(Action action, String user) throws ActionException {
    try {
      actionLogPreProcessor(ActionSubOperation.VALIDATE_ACTION_UNIQUE_NAME, TARGET_ENTITY_API);
      UniqueValueUtil
          .validateUniqueValue(ActionConstants.UniqueValues.ACTION_NAME, action.getName());
      actionLogPostProcessor(StatusCode.COMPLETE);
    } catch (CoreException exception) {
      String errorDesc = String
          .format(ACTION_ENTITY_UNIQUE_VALUE_MSG, ActionConstants.UniqueValues.ACTION_NAME,
              action.getName());
      log.error(errorDesc, exception);
      actionLogPostProcessor(StatusCode.ERROR, ACTION_ENTITY_UNIQUE_VALUE_ERROR, errorDesc, false);
      throw new ActionException(ACTION_ENTITY_UNIQUE_VALUE_ERROR, errorDesc);
    } finally {
      log.metrics("");
    }
    action.setUser(user);
    action.setTimestamp(getCurrentTimeStampUtc());
    action.setActionInvariantUuId(CommonMethods.nextUuId());
    action.setActionUuId(CommonMethods.nextUuId());

    actionLogPreProcessor(ActionSubOperation.CREATE_ACTION_VERSION, TARGET_ENTITY_API);
    Version version =
        versioningManager.create(ACTION_VERSIONABLE_TYPE, action.getActionInvariantUuId(), user);
    actionLogPostProcessor(StatusCode.COMPLETE);
    log.metrics("");

    action.setVersion(version.toString());
    action.setStatus(ActionStatus.Locked);
    action = updateData(action);
    action = actionDao.createAction(action);

    actionLogPreProcessor(ActionSubOperation.CREATE_ACTION_UNIQUE_VALUE, TARGET_ENTITY_API);
    UniqueValueUtil.createUniqueValue(ActionConstants.UniqueValues.ACTION_NAME, action.getName());
    actionLogPostProcessor(StatusCode.COMPLETE);
    log.metrics("");

    return action;
  }

  /**
   * Update an existing action.
   *
   * @param action Action object model of the user request for creating an action
   * @param user   AT&T id of the user sending the update request
   * @return {@link Action} model object for the update action
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action updateAction(Action action, String user) throws ActionException {
    try {
      log.debug("entering updateAction to update action with invariantUuId = "
          + action.getActionInvariantUuId() + " by user = " + user);
      String invariantUuId = action.getActionInvariantUuId();
      actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_API);
      VersionInfo versionInfo = versioningManager
          .getEntityVersionInfo(ACTION_VERSIONABLE_TYPE, invariantUuId, user,
              VersionableEntityAction.Write);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");

      Version activeVersion = versionInfo.getActiveVersion();
      validateActions(action, activeVersion);
      action.setStatus(ActionStatus.Locked); //Status will be Checkout for update
      updateData(action);
      action.setUser(user);
      action.setTimestamp(getCurrentTimeStampUtc());
      actionDao.updateAction(action);

    } catch (CoreException ce) {
      formAndThrowException(ce);
    }
    log.debug("exit updateAction");
    return action;
  }

  /**
   * Checkout an existing action.
   *
   * @param invariantUuId actionInvariantUuId of the action to be checked out
   * @param user          AT&T id of the user sending the checkout request
   * @return {@link Action} model object for the checkout action
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action checkout(String invariantUuId, String user) throws ActionException {
    Version version = null;
    ActionEntity actionEntity = null;
    try {
      log.debug(
          "entering checkout for Action with invariantUUID= " + invariantUuId + " by user = "
              + user);
      actionLogPreProcessor(ActionSubOperation.CHECKOUT_ACTION, TARGET_ENTITY_API);
      version = versioningManager.checkout(ACTION_VERSIONABLE_TYPE, invariantUuId, user);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");

      actionEntity =
          updateUniqueIdForVersion(invariantUuId, version, ActionStatus.Locked.name(), user);
    } catch (CoreException exception) {
      if (exception.code() != null && exception.code().id().equals(
          VersioningErrorCodes.CHECKOT_ON_LOCKED_ENTITY)) {
        actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
        VersionInfoEntity versionInfoEntity =
            versionInfoDao.get(new VersionInfoEntity(ACTION_VERSIONABLE_TYPE, invariantUuId));
        actionLogPostProcessor(StatusCode.COMPLETE);
        log.metrics("");
        String checkoutUser = versionInfoEntity.getCandidate().getUser();
        log.debug(
            "Actual checkout user for Action with invariantUUID= " + invariantUuId + " is = "
                + checkoutUser);
        if (!checkoutUser.equals(user)) {
          throw new ActionException(ACTION_CHECKOUT_ON_LOCKED_ENTITY_OTHER_USER,
              exception.getMessage());
        }
      }
      formAndThrowException(exception);
    }
    log.debug(
        "exit checkout for Action with invariantUUID= " + invariantUuId + " by user = " + user);
    return actionEntity != null ? actionEntity.toDto() : new Action();
  }

  /**
   * Undo an already checked out action.
   *
   * @param invariantUuId actionInvariantUuId of the checked out action
   * @param user          AT&T id of the user sending the request
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public void undoCheckout(String invariantUuId, String user) throws ActionException {
    Version version;
    try {
      log.debug(
          "entering undoCheckout for Action with invariantUUID= " + invariantUuId + " by user = "
              + user);

      actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
      //Get list of uploaded artifacts in this checked out version
      VersionInfoEntity versionInfoEntity =
          versionInfoDao.get(new VersionInfoEntity(ACTION_VERSIONABLE_TYPE, invariantUuId));
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      if (versionInfoEntity == null) {
        throw new CoreException(
            new EntityNotExistErrorBuilder(ACTION_VERSIONABLE_TYPE, invariantUuId).build());
      }
      UserCandidateVersion candidate = versionInfoEntity.getCandidate();
      Version activeVersion;
      if (candidate != null) {
        activeVersion = candidate.getVersion();
      } else {
        activeVersion = versionInfoEntity.getActiveVersion();
      }

      actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_VERSION, TARGET_ENTITY_DB);
      Action action = actionDao.get(new ActionEntity(invariantUuId, activeVersion)).toDto();
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");

      //Perform undo checkout on the action
      actionLogPreProcessor(ActionSubOperation.UNDO_CHECKOUT_ACTION, TARGET_ENTITY_API);
      version = versioningManager.undoCheckout(ACTION_VERSIONABLE_TYPE, invariantUuId, user);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");

      if (version.equals(new Version(0, 0))) {
        actionLogPreProcessor(ActionSubOperation.DELETE_UNIQUEVALUE, TARGET_ENTITY_API);
        UniqueValueUtil
            .deleteUniqueValue(ActionConstants.UniqueValues.ACTION_NAME, action.getName());
        actionLogPostProcessor(StatusCode.COMPLETE);
        log.metrics("");

        actionLogPreProcessor(ActionSubOperation.DELETE_ACTIONVERSION, TARGET_ENTITY_DB);
        //Added for the case where Create->Undo_Checkout->Checkout should not get the action
        versionInfoDao.delete(new VersionInfoEntity(ACTION_VERSIONABLE_TYPE, invariantUuId));
        actionLogPostProcessor(StatusCode.COMPLETE);
        log.metrics("");
      }

      List<ActionArtifact> currentVersionArtifacts = action.getArtifacts();

      //Delete the artifacts from action_artifact table (if any)
      if (CollectionUtils.isNotEmpty(currentVersionArtifacts) && currentVersionArtifacts.size() > 0) {
        for (ActionArtifact artifact : currentVersionArtifacts) {
          ActionArtifactEntity artifactDeleteEntity =
              new ActionArtifactEntity(artifact.getArtifactUuId(),
                  getEffectiveVersion(activeVersion.toString()));
          actionLogPreProcessor(ActionSubOperation.DELETE_ARTIFACT, TARGET_ENTITY_DB);
          actionArtifactDao.delete(artifactDeleteEntity);
          actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
        }
      }
    } catch (CoreException exception) {
      formAndThrowException(exception);
    }
    log.debug(
        "exit undoCheckout for Action with invariantUUID= " + invariantUuId + " by user = " + user);
  }

  /**
   * Checkin a checked out action.
   *
   * @param invariantUuId actionInvariantUuId of the checked out action
   * @param user          AT&T id of the user sending the request
   * @return {@link Action} model object for the updated action
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action checkin(String invariantUuId, String user) throws ActionException {
    Version version = null;
    ActionEntity actionEntity = null;
    try {
      log.debug("entering checkin for Action with invariantUUID= " + invariantUuId + " by user = "
          + user);
      actionLogPreProcessor(ActionSubOperation.CHECKIN_ACTION, TARGET_ENTITY_API);
      version = versioningManager.checkin(ACTION_VERSIONABLE_TYPE, invariantUuId, user, null);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      actionEntity =
          updateStatusForVersion(invariantUuId, version, ActionStatus.Available.name(), user);
    } catch (CoreException exception) {
      formAndThrowException(exception);
    }
    log.debug(
        "exit checkin for Action with invariantUUID= " + invariantUuId + " by user = " + user);
    return actionEntity != null ? actionEntity.toDto() : new Action();
  }

  /**
   * Submit a checked in action.
   *
   * @param invariantUuId actionInvariantUuId of the checked in action
   * @param user          AT&T id of the user sending the request
   * @return {@link Action} model object for the updated action
   * @throws ActionException Exception with an action library specific code, short description and
   *                         detailed message for the error occurred for the error occurred during
   *                         the operation
   */
  @Override
  public Action submit(String invariantUuId, String user) throws ActionException {
    Version version = null;
    ActionEntity actionEntity = null;
    try {
      log.debug(
          "entering submit for Action with invariantUUID= " + invariantUuId + " by user = " + user);
      actionLogPreProcessor(ActionSubOperation.SUBMIT_ACTION, TARGET_ENTITY_API);
      version = versioningManager.submit(ACTION_VERSIONABLE_TYPE, invariantUuId, user, null);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      actionEntity =
          updateUniqueIdForVersion(invariantUuId, version, ActionStatus.Final.name(), user);
    } catch (CoreException exception) {
      formAndThrowException(exception);
    }
    log.debug("exit submit for Action with invariantUUID= " + invariantUuId + " by user = " + user);
    return actionEntity != null ? actionEntity.toDto() : new Action();
  }

  /**
   * Download an artifact of an action.
   *
   * @param artifactUuId {@link ActionArtifact} object representing the artifact and its metadata
   * @param actionUuId   UUID of the action for which the artifact has to be downloaded
   * @return downloaded action artifact object
   */
  @Override
  public ActionArtifact downloadArtifact(String actionUuId, String artifactUuId)
      throws ActionException {
    log.debug(" entering downloadArtifact with actionUUID= " + actionUuId + " and artifactUUID= "
        + artifactUuId);
    Action action = actionDao.getActionsByActionUuId(actionUuId);
    ActionArtifact actionArtifact;
    if (action != null) {
      MDC.put(SERVICE_INSTANCE_ID, action.getActionInvariantUuId());
      List<ActionArtifact> artifacts = action.getArtifacts();
      String actionVersion = action.getVersion();
      int effectiveVersion = getEffectiveVersion(actionVersion);
      ActionArtifact artifactMetadata =
          getArtifactMetadataFromAction(artifacts, ARTIFACT_METADATA_ATTR_UUID, artifactUuId);
      if (artifactMetadata != null) {
        String artifactName = artifactMetadata.getArtifactName();
        actionArtifact = actionArtifactDao.downloadArtifact(effectiveVersion, artifactUuId);
        actionArtifact.setArtifactName(artifactName);

      } else {
        throw new ActionException(ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE,
            ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
      }
    } else {
      throw new ActionException(ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE,
          ActionErrorConstants.ACTION_ENTITY_NOT_EXIST);
    }
    log.debug(" exit downloadArtifact with actionUUID= " + actionUuId + " and artifactUUID= "
        + artifactUuId);
    return actionArtifact;
  }

  /**
   * Upload an artifact to an action.
   *
   * @param artifact            {@link ActionArtifact} object representing the artifact and its
   *                            metadata
   * @param actionInvariantUuId Invariant UUID of the action to which the artifact has to be
   *                            uploaded
   * @param user                User ID of the user sending the request
   * @return Uploaded action artifact object
   */
  @Override
  public ActionArtifact uploadArtifact(ActionArtifact artifact, String actionInvariantUuId,
                                       String user) {
    ActionArtifact uploadArtifactResponse = new ActionArtifact();
    try {
      log.debug("entering uploadArtifact with actionInvariantUuId= " + actionInvariantUuId
          + "artifactName= " + artifact.getArtifactName());
      actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
      VersionInfo versionInfo = versioningManager
          .getEntityVersionInfo(ACTION_VERSIONABLE_TYPE, actionInvariantUuId, user,
              VersionableEntityAction.Write);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      Version activeVersion = versionInfo.getActiveVersion();
      actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID, TARGET_ENTITY_DB);
      Action action = actionDao.get(new ActionEntity(actionInvariantUuId, activeVersion)).toDto();
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
      String artifactUuId = generateActionArtifactUuId(action, artifact.getArtifactName());
      //Check for Unique document name
      List<ActionArtifact> actionArtifacts = action.getArtifacts();
      ActionArtifact artifactMetadata =
          getArtifactMetadataFromAction(actionArtifacts, ARTIFACT_METADATA_ATTR_NAME,
              artifact.getArtifactName());
      if (artifactMetadata != null) {
        throw new ActionException(ACTION_ARTIFACT_ALREADY_EXISTS_CODE,
            String.format(ACTION_ARTIFACT_ALREADY_EXISTS, actionInvariantUuId));
      }

      //Create the artifact
      artifact.setArtifactUuId(artifactUuId);
      artifact.setTimestamp(getCurrentTimeStampUtc());
      artifact.setEffectiveVersion(getEffectiveVersion(activeVersion.toString()));
      actionArtifactDao.uploadArtifact(artifact);

      //Update the action data field and timestamp
      addArtifactMetadataInActionData(action, artifact);

      //Set the response object
      uploadArtifactResponse.setArtifactUuId(artifact.getArtifactUuId());
    } catch (CoreException ce) {
      formAndThrowException(ce);
    }
    log.debug(
        "exit uploadArtifact with actionInvariantUuId= " + actionInvariantUuId + "artifactName= "
            + artifact.getArtifactName());
    return uploadArtifactResponse;
  }

  @Override
  public void deleteArtifact(String actionInvariantUuId, String artifactUuId, String user)
      throws ActionException {
    log.debug(
        "enter deleteArtifact with actionInvariantUuId= " + actionInvariantUuId + "artifactUUID= "
            + artifactUuId + " and user = " + user);
    Action action = actionDao.getLockedAction(actionInvariantUuId, user);
    List<ActionArtifact> actionArtifacts = action.getArtifacts();
    ActionArtifact artifactMetadata =
        getArtifactMetadataFromAction(actionArtifacts, ARTIFACT_METADATA_ATTR_UUID, artifactUuId);
    if (artifactMetadata == null) {
      throw new ActionException(ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE,
          ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
    }
    if (artifactMetadata.getArtifactProtection().equals(ActionArtifactProtection.readOnly.name())) {
      throw new ActionException(ACTION_ARTIFACT_DELETE_READ_ONLY,
          ACTION_ARTIFACT_DELETE_READ_ONLY_MSG);
    } else {

      //Update action by removing artifact metadata
      String jsonData = action.getData();
      List<ActionArtifact> artifacts = action.getArtifacts();//action.getArtifacts();
      ActionArtifact artifact = null;
      Iterator<ActionArtifact> it = artifacts.iterator();
      while (it.hasNext()) {
        artifact = it.next();
        String artifactId = artifact.getArtifactUuId();
        if (artifactId.equals(artifactUuId)) {
          it.remove();
        }
      }

      Map dataMap = JsonUtil.json2Object(jsonData, LinkedHashMap.class);
      dataMap.put("artifacts", artifacts);
      String data = JsonUtil.object2Json(dataMap);
      ActionEntity actionEntity = action.toEntity();
      actionEntity.setData(data);
      actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION, TARGET_ENTITY_DB);
      actionDao.update(actionEntity);
      actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      // delete Artifact if it's upload and delete action on same checkout version
      String artifactName = artifactMetadata.getArtifactName();
      String generatedArtifactUuId = generateActionArtifactUuId(action, artifactName);
      if (generatedArtifactUuId.equals(artifactUuId)) {
        if (artifact != null) {
          ActionArtifactEntity artifactDeleteEntity =
              new ActionArtifactEntity(artifact.getArtifactUuId(),
                  getEffectiveVersion(action.getVersion()));
          actionLogPreProcessor(ActionSubOperation.DELETE_ACTION_ARTIFACT, TARGET_ENTITY_DB);
          actionArtifactDao.delete(artifactDeleteEntity);
        }
        actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
        log.metrics("");
      }

    }
    log.debug(
        "exit deleteArtifact with actionInvariantUuId= " + actionInvariantUuId + "artifactUUID= "
            + artifactUuId + " and user = " + user);
  }

  /**
   * Update an existing artifact.
   *
   * @param artifact            {@link ActionArtifact} object representing the artifact and its
   *                            metadata
   * @param actionInvariantUuId Invariant UUID of the action to which the artifact has to be
   *                            uploaded
   * @param user                User ID of the user sending the request
   */
  public void updateArtifact(ActionArtifact artifact, String actionInvariantUuId, String user) {
    try {
      log.debug("Enter updateArtifact with actionInvariantUuId= " + actionInvariantUuId
          + "artifactUUID= " + artifact.getArtifactUuId() + " and user = " + user);
      actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_API);
      VersionInfo versionInfo = versioningManager
          .getEntityVersionInfo(ACTION_VERSIONABLE_TYPE, actionInvariantUuId, user,
              VersionableEntityAction.Write);
      actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      Version activeVersion = versionInfo.getActiveVersion();
      actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID, TARGET_ENTITY_DB);
      Action action = actionDao.get(new ActionEntity(actionInvariantUuId, activeVersion)).toDto();
      actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      List<ActionArtifact> actionArtifacts = action.getArtifacts();
      ActionArtifact artifactMetadataByUuId =
          getArtifactMetadataFromAction(actionArtifacts, ARTIFACT_METADATA_ATTR_UUID,
              artifact.getArtifactUuId());
      //Check if artifact is already in action or not
      if (artifactMetadataByUuId == null) {
        throw new ActionException(ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE,
            ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST);
      }
      //If user tries to change artifact name
      if (artifact.getArtifactName() != null
          && !artifactMetadataByUuId.getArtifactName().equalsIgnoreCase(
          artifact.getArtifactName())) {
        throw new ActionException(ACTION_UPDATE_NOT_ALLOWED_CODE,
            ACTION_ARTIFACT_UPDATE_NAME_INVALID);
      }

      byte[] payload = artifact.getArtifact();
      String artifactLabel = artifact.getArtifactLabel();
      String artifactCategory = artifact.getArtifactCategory();
      String artifactDescription = artifact.getArtifactDescription();
      String artifactProtection = artifact.getArtifactProtection();
      String artifactName = artifact.getArtifactName();
      //If artifact read only
      if (artifactMetadataByUuId.getArtifactProtection()
          .equals(ActionArtifactProtection.readOnly.name())) {
        if (artifactName != null || artifactLabel != null || artifactCategory != null
            || artifactDescription != null || payload != null) {
          throw new ActionException(ACTION_ARTIFACT_UPDATE_READ_ONLY,
              ACTION_ARTIFACT_UPDATE_READ_ONLY_MSG);
        }
        //Changing value from readOnly to readWrite
        if (artifactProtection != null
            && artifactProtection.equals(ActionArtifactProtection.readWrite.name())) {
          artifactMetadataByUuId.setArtifactProtection(ActionArtifactProtection.readWrite.name());
          artifactMetadataByUuId.setTimestamp(getCurrentTimeStampUtc());
          updateArtifactMetadataInActionData(action, artifactMetadataByUuId);
        }
      } else {
        int effectiveVersion = getEffectiveVersion(activeVersion.toString());
        if (artifactLabel != null) {
          artifactMetadataByUuId.setArtifactLabel(artifactLabel);
        }
        if (artifactCategory != null) {
          artifactMetadataByUuId.setArtifactCategory(artifactCategory);
        }
        if (artifactDescription != null) {
          artifactMetadataByUuId.setArtifactDescription(artifactDescription);
        }
        if (artifactProtection != null) {
          artifactMetadataByUuId.setArtifactProtection(artifactProtection);
        }
        if (payload != null) {
          //get artifact data from action_artifact table for updating the content
          ActionArtifact artifactContent = new ActionArtifact();
          artifactContent.setArtifactUuId(artifact.getArtifactUuId());
          artifactContent.setArtifact(payload);
          artifactContent.setEffectiveVersion(effectiveVersion);
          actionArtifactDao.updateArtifact(artifactContent);
        }
        //Update the action data field and timestamp
        artifactMetadataByUuId.setTimestamp(getCurrentTimeStampUtc());
        updateArtifactMetadataInActionData(action, artifactMetadataByUuId);
      }
      log.debug("exit updateArtifact with actionInvariantUuId= " + actionInvariantUuId
          + "artifactUUID= " + artifact.getArtifactUuId() + " and user = " + user);
    } catch (CoreException coreException) {
      formAndThrowException(coreException);
    }
  }

  /**
   * Generate artifact UUID at runtime using action name and effective version.
   *
   * @param action       {@link Action} for which the artifact is being uploaded/updated/downloaded
   * @param artifactName Artifact name
   * @return Generated UUID string
   */
  private String generateActionArtifactUuId(Action action, String artifactName) {
    int effectiveVersion = getEffectiveVersion(action.getVersion());
    //Upper case for maintaining case-insensitive behavior for the artifact names
    String artifactUuIdString =
        action.getName().toUpperCase() + effectiveVersion + artifactName.toUpperCase();
    String generateArtifactUuId =
        UUID.nameUUIDFromBytes((artifactUuIdString).getBytes()).toString();
    String artifactUuId = generateArtifactUuId.replace("-", "");
    return artifactUuId.toUpperCase();
  }

  /**
   * Generate the effective action version for artifact operations.
   *
   * @param actionVersion Version of the action as a string
   * @return Effective version to be used for artifact operations
   */
  private int getEffectiveVersion(String actionVersion) {
    Version version = Version.valueOf(actionVersion);
    return version.getMajor() * 10000 + version.getMinor();
  }

  /**
   * Update the data field of the Action object with the modified/generated fields after an
   * operation.
   *
   * @param action Action object whose data field has to be updated
   * @return Updated {@link Action} object
   */
  private Action updateData(Action action) {
    log.debug("entering updateData to update data json for action with actionuuid=  "
        + action.getActionUuId());
    Map<String, String> dataMap = new LinkedHashMap<>();
    dataMap.put(ActionConstants.UNIQUE_ID, action.getActionUuId());
    dataMap.put(ActionConstants.VERSION, action.getVersion());
    dataMap.put(ActionConstants.INVARIANTUUID, action.getActionInvariantUuId());
    dataMap.put(ActionConstants.STATUS, action.getStatus().name());

    String data = action.getData();
    Map<String, String> currentDataMap = JsonUtil.json2Object(data, LinkedHashMap.class);
    dataMap.putAll(currentDataMap);
    data = JsonUtil.object2Json(dataMap);
    action.setData(data);
    log.debug("exit updateData");
    return action;
  }

  /**
   * Method to add the artifact metadata in the data attribute of action table.
   *
   * @param action   Action to which artifact is uploaded
   * @param artifact Uploaded artifact object
   */
  private void addArtifactMetadataInActionData(Action action, ActionArtifact artifact) {

    ActionArtifact artifactMetadata = new ActionArtifact();
    artifactMetadata.setArtifactUuId(artifact.getArtifactUuId());
    artifactMetadata.setArtifactName(artifact.getArtifactName());
    artifactMetadata.setArtifactProtection(artifact.getArtifactProtection());
    artifactMetadata.setArtifactLabel(artifact.getArtifactLabel());
    artifactMetadata.setArtifactDescription(artifact.getArtifactDescription());
    artifactMetadata.setArtifactCategory(artifact.getArtifactCategory());
    artifactMetadata.setTimestamp(artifact.getTimestamp());

    List<ActionArtifact> actionArtifacts = action.getArtifacts();
    if (actionArtifacts == null) {
      actionArtifacts = new ArrayList<>();
    }
    actionArtifacts.add(artifactMetadata);
    action.setArtifacts(actionArtifacts);
    String currentData = action.getData();
    Map<String, Object> currentDataMap = JsonUtil.json2Object(currentData, LinkedHashMap.class);
    currentDataMap.put(ActionConstants.ARTIFACTS, actionArtifacts);
    String updatedActionData = JsonUtil.object2Json(currentDataMap);
    action.setData(updatedActionData);
    action.setTimestamp(artifact.getTimestamp());
    actionDao.updateAction(action);
  }

  /**
   * Get a list of last major and last minor version (no candidate) of action from a list of
   * actions.
   *
   * @param actions Exhaustive list of the action versions
   * @return List {@link Action} of last major and last minor version (no candidate) of action from
     a list of actions
   */
  private List<Action> getMajorMinorVersionActions(List<Action> actions) {
    log.debug(" entering getMajorMinorVersionActions for actions ");
    List<Action> list = new LinkedList<>();
    actionLogPreProcessor(ActionSubOperation.GET_VERSIONINFO_FOR_ALL_ACTIONS, TARGET_ENTITY_API);
    Map<String, VersionInfo> actionVersionMap = versioningManager
        .listEntitiesVersionInfo(ACTION_VERSIONABLE_TYPE, "", VersionableEntityAction.Read);
    actionLogPostProcessor(StatusCode.COMPLETE);
    log.metrics("");
    for (Action action : actions) {
      if (action.getStatus() == ActionStatus.Deleted) {
        continue;
      }
      VersionInfo actionVersionInfo = actionVersionMap.get(action.getActionInvariantUuId());
      if (actionVersionInfo.getActiveVersion() != null
          && actionVersionInfo.getActiveVersion().equals(Version.valueOf(action.getVersion()))) {
        list.add(action);
      } else if (actionVersionInfo.getLatestFinalVersion() != null
          && actionVersionInfo.getLatestFinalVersion().equals(Version.valueOf(action.getVersion()))
          &&
          !actionVersionInfo.getLatestFinalVersion().equals(actionVersionInfo.getActiveVersion())) {
        list.add(action);
      }
    }
    log.debug(" exit getMajorMinorVersionActions for actions ");
    return list;
  }

  /**
   * CoreException object wrapper from Version library to Action Library Exception.
   *
   * @param exception CoreException object from version library
   */
  private void formAndThrowException(CoreException exception) {
    log.debug("entering formAndThrowException with input CoreException =" + exception.code().id()
        + " " + exception.getMessage());
    String errorDescription = exception.getMessage();
    String errorCode = exception.code().id();
    ActionException actionException = new ActionException();
    switch (errorCode) {
      case VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST:
        actionException.setErrorCode(ACTION_ENTITY_NOT_EXIST_CODE);
        actionException.setDescription(ACTION_ENTITY_NOT_EXIST);
        break;
      case VersioningErrorCodes.CHECKOT_ON_LOCKED_ENTITY:
        actionException.setErrorCode(ACTION_CHECKOUT_ON_LOCKED_ENTITY);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.CHECKIN_ON_UNLOCKED_ENTITY:
        actionException.setErrorCode(ACTION_CHECKIN_ON_UNLOCKED_ENTITY);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED:
        actionException.setErrorCode(ACTION_SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.SUBMIT_LOCKED_ENTITY_NOT_ALLOWED:
        actionException.setErrorCode(ACTION_SUBMIT_LOCKED_ENTITY_NOT_ALLOWED);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.UNDO_CHECKOUT_ON_UNLOCKED_ENTITY:
        actionException.setErrorCode(ACTION_UNDO_CHECKOUT_ON_UNLOCKED_ENTITY);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER:
        actionException.setErrorCode(ACTION_EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
        actionException.setDescription(errorDescription.replace("edit", "updat"));
        break;
      case VersioningErrorCodes.CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER:
        actionException.setErrorCode(ACTION_CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER:
        actionException.setErrorCode(ACTION_UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER);
        actionException.setDescription(errorDescription);
        break;
      case VersioningErrorCodes.EDIT_ON_UNLOCKED_ENTITY:
        actionException.setErrorCode(ACTION_UPDATE_ON_UNLOCKED_ENTITY);
        actionException.setDescription(errorDescription.replace("edit", "update"));
        break;
      case VersioningErrorCodes.DELETE_ON_LOCKED_ENTITY:
        actionException.setErrorCode(ACTION_DELETE_ON_LOCKED_ENTITY_CODE);
        actionException.setDescription(errorDescription);
        break;
      default:
        actionException.setErrorCode(ACTION_INTERNAL_SERVER_ERR_CODE);
        actionException.setDescription(exception.getMessage());

    }
    //Todo - Uncomment only if class to be added in ERROR Log
    /*actionErrorLogProcessor(CategoryLogLevel.ERROR, actionException.getErrorCode(),
    actionException.getDescription());
    log.error("");*/
    log.debug(
        "exit formAndThrowException with ActionException =" + actionException.getErrorCode()
            + " " + actionException.getDescription());
    throw actionException;
  }

  /**
   * Validates an action object for business layer validations before an update operation.
   *
   * @param action        Action object to be validated
   * @param activeVersion Active version of the actoin object
   */
  private void validateActions(Action action, Version activeVersion) {
    try {
      //Set version if not already available in input request
      //If version set in input compare it with version from DB
      if (StringUtils.isEmpty(action.getVersion())) {
        action.setVersion(activeVersion.toString());
      } else {
        if (!activeVersion.equals(Version.valueOf(action.getVersion()))) {
          throw new ActionException(ACTION_UPDATE_INVALID_VERSION,
              String.format(ACTION_REQUESTED_VERSION_INVALID, action.getVersion()));
        }
      }
      String invariantUuId = action.getActionInvariantUuId();
      Version version = Version.valueOf(action.getVersion());
      Action existingAction = getActions(invariantUuId, version);
      if (existingAction == null || existingAction.getActionInvariantUuId() == null) {
        throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
      }
      List<String> invalidParameters = new LinkedList<>();
      //Prevent update of name, version and id fields
      if (!existingAction.getName().equals(action.getName())) {
        throw new ActionException(ACTION_UPDATE_NOT_ALLOWED_CODE_NAME,
            ACTION_UPDATE_NOT_ALLOWED_FOR_NAME);
      }
      if (!StringUtils.isEmpty(action.getActionUuId())
          && !existingAction.getActionUuId().equals(action.getActionUuId())) {
        invalidParameters.add(UNIQUE_ID);
      }
      if (action.getStatus() != null && (existingAction.getStatus() != action.getStatus())) {
        invalidParameters.add(STATUS);
      }

      if (!invalidParameters.isEmpty()) {
        throw new ActionException(ACTION_UPDATE_NOT_ALLOWED_CODE,
            String.format(ACTION_UPDATE_PARAM_INVALID, StringUtils.join(invalidParameters, ", ")));
      }
      action.setActionUuId(existingAction.getActionUuId());
    } catch (IllegalArgumentException iae) {
      String message = iae.getMessage();
      if(message == VERSION_STRING_VIOLATION_MSG) {
        throw new ActionException(ACTION_UPDATE_NOT_ALLOWED_CODE, message);
      }
      else {
        throw iae;
      }
    }
  }

  /**
   * Get an action version entity object.
   *
   * @param invariantUuId Invariant UUID of the action
   * @param version       Version of the action
   * @return {@link ActionEntity} object of the action version
   */
  private ActionEntity getActionsEntityByVersion(String invariantUuId, Version version) {
    log.debug(
        "entering getActionsEntityByVersion with invariantUUID= " + invariantUuId + " and version"
            + version);
    ActionEntity entity = null;
    if (version != null) {
      actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_VERSION, TARGET_ENTITY_DB);
      entity = actionDao.get(
          new ActionEntity(invariantUuId != null ? invariantUuId.toUpperCase() : null, version));
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
    }
    log.debug(
        "exit getActionsEntityByVersion with invariantUuId= " + invariantUuId + " and version"
            + version);
    return entity;
  }

  /**
   * Get an action version object.
   *
   * @param invariantUuId Invariant UUID of the action
   * @param version       Version of the action
   * @return {@link Action} object of the action version
   */
  private Action getActions(String invariantUuId, Version version) {
    ActionEntity actionEntity =
        getActionsEntityByVersion(invariantUuId != null ? invariantUuId.toUpperCase() : null,
            version);
    return actionEntity != null ? actionEntity.toDto() : new Action();
  }

  /**
   * Create and set the Unique ID in for an action version row.
   *
   * @param invariantUuId Invariant UUID of the action
   * @param version       Version of the action
   * @param status        Status of the action
   * @param user          AT&T id of the user sending the request
   * @return {@link ActionEntity} object of the action version
   */
  private ActionEntity updateUniqueIdForVersion(String invariantUuId, Version version,
                                                String status, String user) {
    log.debug(
        "entering updateUniqueIdForVersion to update action with invariantUuId= " + invariantUuId
            + " with version,status and user as ::" + version + " " + status + " " + user);
    //generate UUID AND update for newly created entity row
    ActionEntity actionEntity = getActionsEntityByVersion(invariantUuId, version);
    if (actionEntity != null) {
      log.debug("Found action to be updated");
      String data = actionEntity.getData();
      String uniqueId = CommonMethods.nextUuId();
      Map<String, String> dataMap = JsonUtil.json2Object(data, LinkedHashMap.class);
      dataMap.put(ActionConstants.UNIQUE_ID, uniqueId);
      dataMap.put(ActionConstants.VERSION, version.toString());
      dataMap.put(ActionConstants.STATUS, status);
      data = JsonUtil.object2Json(dataMap);

      actionEntity.setData(data);
      actionEntity.setActionUuId(uniqueId);
      actionEntity.setStatus(status);
      actionEntity.setUser(user);
      actionEntity.setTimestamp(getCurrentTimeStampUtc());
      actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION, TARGET_ENTITY_DB);
      actionDao.update(actionEntity);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
    }

    log.debug(
        "exit updateUniqueIdForVersion to update action with invariantUUID= " + invariantUuId);
    return actionEntity;
  }

  /**
   * Set the status for an action version row.
   *
   * @param invariantUuId Invariant UUID of the action
   * @param version       Version of the action
   * @param status        Status of the action
   * @param user          AT&T id of the user sending the request
   * @return {@link ActionEntity} object of the action version
   */
  private ActionEntity updateStatusForVersion(String invariantUuId, Version version, String status,
                                              String user) {
    log.debug(
        "entering updateStatusForVersion with invariantUuId= " + invariantUuId + " and version"
            + version + " for updating status " + status + " by user " + user);
    ActionEntity actionEntity = getActionsEntityByVersion(invariantUuId, version);
    if (actionEntity != null) {
      String data = actionEntity.getData();
      Map<String, String> dataMap = JsonUtil.json2Object(data, LinkedHashMap.class);
      dataMap.put(ActionConstants.STATUS, status);
      data = JsonUtil.object2Json(dataMap);
      actionEntity.setData(data);
      actionEntity.setStatus(status);
      actionEntity.setUser(user);
      actionEntity.setTimestamp(getCurrentTimeStampUtc());
      actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION, TARGET_ENTITY_DB);
      actionDao.update(actionEntity);
      actionLogPostProcessor(StatusCode.COMPLETE);
      log.metrics("");
    }
    log.debug("exit updateStatusForVersion with invariantUuId= " + invariantUuId + " and version"
        + version + " for updating status " + status + " by user " + user);
    return actionEntity;

  }

  /**
   * Gets an artifact from the action artifact metadata by artifact name.
   *
   * @param actionArtifactList  Action's existing artifact list
   * @param artifactFilterType  Search criteria for artifact in action artifact metadata
   * @param artifactFilterValue Value of Search parameter
   * @return Artifact metadata object if artifact is present in action and null otherwise
   */
  private ActionArtifact getArtifactMetadataFromAction(List<ActionArtifact> actionArtifactList,
                                                       String artifactFilterType,
                                                       String artifactFilterValue) {
    ActionArtifact artifact = null;
    if (actionArtifactList != null && !actionArtifactList.isEmpty()) {
      for (ActionArtifact entry : actionArtifactList) {
        switch (artifactFilterType) {
          case ARTIFACT_METADATA_ATTR_UUID:
            String artifactUuId = entry.getArtifactUuId();
            if (artifactUuId != null && artifactUuId.equals(artifactFilterValue)) {
              artifact = entry;
              break;
            }
            break;
          case ARTIFACT_METADATA_ATTR_NAME:
            String existingArtifactName = entry.getArtifactName().toLowerCase();
            if (existingArtifactName.equals(artifactFilterValue.toLowerCase())) {
              artifact = entry;
              break;
            }
            break;
          default:
        }
      }
    }
    return artifact;
  }

  /**
   * Method to update the artifact metadata in the data attribute of action table.
   *
   * @param action          Action to which artifact is uploaded
   * @param updatedArtifact updated artifact object
   */
  private void updateArtifactMetadataInActionData(Action action, ActionArtifact updatedArtifact) {
    for (ActionArtifact entry : action.getArtifacts()) {
      if (entry.getArtifactUuId().equals(updatedArtifact.getArtifactUuId())) {
        entry.setArtifactLabel(updatedArtifact.getArtifactLabel());
        entry.setArtifactCategory(updatedArtifact.getArtifactCategory());
        entry.setArtifactDescription(updatedArtifact.getArtifactDescription());
        entry.setArtifactProtection(updatedArtifact.getArtifactProtection());
        entry.setTimestamp(updatedArtifact.getTimestamp());
        break;
      }
    }
    String data = action.getData();
    Map<String, Object> map = JsonUtil.json2Object(data, LinkedHashMap.class);
    map.put(ActionConstants.ARTIFACTS, action.getArtifacts());
    String updatedActionData = JsonUtil.object2Json(map);
    action.setData(updatedActionData);
    action.setTimestamp(updatedArtifact.getTimestamp());
    actionDao.updateAction(action);
  }
}
