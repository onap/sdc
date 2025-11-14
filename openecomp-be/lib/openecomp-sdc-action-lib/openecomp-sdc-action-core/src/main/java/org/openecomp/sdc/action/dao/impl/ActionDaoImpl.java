/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.action.dao.impl;


import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.COMPLETE;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.ERROR;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY;
import static org.openecomp.sdc.action.ActionConstants.TARGET_ENTITY_DB;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.*;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.util.*;
import java.util.stream.Collectors;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.dao.ActionDao;
import org.openecomp.sdc.action.dao.ActionDaoMapper;
import org.openecomp.sdc.action.dao.ActionMapper;
import org.openecomp.sdc.action.dao.ActionMapperBuilder;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.dao.types.OpenEcompComponentEntity;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.openecomp.sdc.action.types.OpenEcompComponent;
import org.openecomp.sdc.action.util.ActionUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.ActionVersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDaoFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDaoFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.slf4j.MDC;

public class ActionDaoImpl implements ActionDao {

    private static final String FOR_VERSIONS = " for versions ";
    private static final String VERSION = "version";
    private static final String ACTION = "Action"; // table name as used in queries

    // Old 3.x driver used MappingManager — replaced with direct session and MapperBuilder in 4.x.
    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final CqlSession session = noSqlDb.getSession(); // requires NoSqlDb to expose CqlSession
    // New Mapper initialization (4.x style): build Mapper via generated builder
    private static final ActionMapper actionMapper = new ActionMapperBuilder(session).build();
    private static final ActionDaoMapper accessor = actionMapper.actionDaoMapper();

    private static final VersionInfoDao versionInfoDao = VersionInfoDaoFactory.getInstance().createInterface();
    private static final VersionInfoDeletedDao versionInfoDeletedDao = VersionInfoDeletedDaoFactory.getInstance().createInterface();
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public ActionDaoImpl() {
        // Explicit no-op constructor; static initialization handles mapper/session setup
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
        // 3.x used mapper.getTableMetadata(); not available in 4.x — replaced with hardcoded table + PK columns
        ActionVersioningManagerFactory.getInstance().createInterface().register(versionableEntityType,
            new VersionableEntityMetadata(ACTION, "actioninvariantuuid", "version"));
    }

     @Override
    public Action createAction(Action action) {
        try {
            ActionUtil.actionLogPreProcessor(ActionSubOperation.CREATE_ACTION_ENTITY, TARGET_ENTITY_DB);
            // Mapper-based DAO insert replaces old mapper.save()
            accessor.addAction(action.toEntity());
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            return action;
        } catch (Exception exception) {
            logGenericException(exception);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
    }

    @Override
    public Action updateAction(Action action) {
        try {
            log.debug(" entering updateAction with actionUUID= " + action.getActionUuId());
            ActionUtil.actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION, TARGET_ENTITY_DB);
            // Upsert semantics via same DAO insert method (Datastax 4.x supports upsert by default)
            accessor.addAction(action.toEntity());
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            log.debug(" exit updateAction with actionUUID= " + action.getActionUuId());
            return action;
        } catch (Exception exception) {
            logGenericException(exception);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
    }
    @Override
    public void deleteAction(String actionInvariantUuId) {
        try {
            log.debug("entering deleteAction with actionInvariantUuId = " + actionInvariantUuId);
            // Using SimpleStatement in place of legacy QueryBuilder (3.x deprecated)
            ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
            VersionInfoDeletedEntity activeVersionEntity = versionInfoDeletedDao
                .get(new VersionInfoDeletedEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            Version activeVersion = activeVersionEntity.getActiveVersion();

            //Switched from mapper query to plain CQL SELECT for performance and clarity
            SimpleStatement stmt = SimpleStatement.builder("SELECT name FROM dox.\"Action\" WHERE actioninvariantuuid = ? AND version = ?")
                .addPositionalValue(actionInvariantUuId)
                .addPositionalValue(activeVersion)
                .build();

        ResultSet results = session.execute(stmt);
           if (results.iterator().hasNext()) {
                Row row = results.one();
                if (row != null) {
                    String name = row.getString("name");
        List<Version> versions = getVersionsByName(name);
        updateActionStatusForDelete(actionInvariantUuId, versions);
    }
}
        } catch (Exception noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug("exit deleteAction");
    }

    @Override
    public List<Action> getFilteredActions(String filterType, String filterId) {
        List<Action> actions = new ArrayList<>();
        log.debug(" entering getFilteredActions By filterType = " + filterType + " With value = " + filterId);
        try {
            List<ActionEntity> entities = new ArrayList<>();
            switch (filterType) {
                case ActionConstants.FILTER_TYPE_VENDOR:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_VENDOR, TARGET_ENTITY_DB);
                    entities = accessor.getActionsByVendor(filterId);
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    break;
                case ActionConstants.FILTER_TYPE_CATEGORY:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_CATEGORY, TARGET_ENTITY_DB);
                    entities = accessor.getActionsByCategory(filterId);
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    break;
                case ActionConstants.FILTER_TYPE_MODEL:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_MODEL, TARGET_ENTITY_DB);
                    entities = accessor.getActionsByModel(filterId);
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    break;
                case ActionConstants.FILTER_TYPE_OPEN_ECOMP_COMPONENT:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_COMPONENT, TARGET_ENTITY_DB);
                    entities = accessor.getActionsByOpenEcompComponent(filterId);
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    break;
                case ActionConstants.FILTER_TYPE_NONE:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ALL_ACTIONS, TARGET_ENTITY_DB);
                    entities = accessor.getAllActions();
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    break;
                case ActionConstants.FILTER_TYPE_NAME:
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONINVID_BY_NAME, TARGET_ENTITY_DB);
                    entities = accessor.getInvIdByName(filterId);
                    ActionUtil.actionLogPostProcessor(COMPLETE);
                    log.metrics("");
                    if (entities != null && !entities.isEmpty()) {
                        String actionInvariantUuId = entities.get(0).getActionInvariantUuId();
                        if (actionInvariantUuId != null) {
                            return getActionsByActionInvariantUuId(actionInvariantUuId);
                        } else {
                            return actions;
                        }
                    }
                    break;
                default:
                    break;
            }

            if (entities != null) {
                actions.addAll(entities.stream().map(ActionEntity::toDto).collect(Collectors.toList()));
            }

            log.debug(" exit getFilteredActions By filterType = " + filterType + " With value = " + filterId);
        } catch (Exception noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        return actions;
    }

    @Override
    public Action getActionsByActionUuId(String actionUuId) {
        try {
            log.debug(" entering getActionsByActionUuId with actionUUID= " + actionUuId);
            ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONUUID, TARGET_ENTITY_DB);
            Optional<ActionEntity> result = accessor.actionInvariantUuId(actionUuId);
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            if (result != null && result.isPresent()) {
                ActionEntity entity = result.get();
                return entity != null ? entity.toDto() : null;
            }
            return null;
        } catch (Exception noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
    }

    @Override
    public List<OpenEcompComponent> getOpenEcompComponents() {
        List<OpenEcompComponent> openEcompComponents = new ArrayList<>();
        try {
            log.debug(" entering getOpenEcompComponents ");
            ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_OPEN_ECOMP_COMPONENTS_ENTITY, TARGET_ENTITY_DB);
            List<OpenEcompComponentEntity> result = accessor.getOpenEcompComponents();
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            if (result != null) {
                openEcompComponents.addAll(result.stream().map(OpenEcompComponentEntity::toDto).collect(Collectors.toList()));
            }
        } catch (Exception noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug(" exit getOpenEcompComponents ");
        return openEcompComponents;
    }
    @Override
    public List<Action> getActionsByActionInvariantUuId(String actionInvariantUuId) {
        List<Action> actions = new ArrayList<>();
        try {
            log.debug(" entering getActionsByActionInvariantUuId with actionInvariantUuId= " + actionInvariantUuId);
            Set<Version> viewableVersions = new HashSet<>();
            VersionPredicate filter = new VersionPredicate();

            ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
            VersionInfoEntity versionInfoEntity = versionInfoDao
                .get(new VersionInfoEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));

            if (versionInfoEntity == null) {
                VersionInfoDeletedEntity versionInfoDeletedEntity = versionInfoDeletedDao
                    .get(new VersionInfoDeletedEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));
                if (versionInfoDeletedEntity != null) {
                    viewableVersions = versionInfoDeletedEntity.getViewableVersions();
                    if (versionInfoDeletedEntity.getActiveVersion() != null) {
                        filter.activeVersion = versionInfoDeletedEntity.getActiveVersion();
                        filter.finalVersion = versionInfoDeletedEntity.getLatestFinalVersion();
                        viewableVersions.removeIf(filter::isIntermediateMinorVersion);
                    }
                }
            } else {
                viewableVersions = versionInfoEntity.getViewableVersions();
                if (versionInfoEntity.getActiveVersion() != null) {
                    filter.activeVersion = versionInfoEntity.getActiveVersion();
                    filter.finalVersion = versionInfoEntity.getLatestFinalVersion();
                    viewableVersions.removeIf(filter::isIntermediateMinorVersion);
                }
                if (versionInfoEntity.getCandidate() != null) {
                    viewableVersions.add(versionInfoEntity.getCandidate().getVersion());
                }
            }

            MDC.put(TARGET_ENTITY, TARGET_ENTITY_DB);
            ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
            log.metrics("");
            if (!viewableVersions.isEmpty()) {
                ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID, TARGET_ENTITY_DB);
                List<Version> vList = new ArrayList<>(viewableVersions);
                List<ActionEntity> result = accessor.getActionsByInvId(actionInvariantUuId, vList);
                ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
                log.metrics("");
                if (result != null) {
                    actions.addAll(result.stream().map(ActionEntity::toDto).collect(Collectors.toList()));
                }
            }
        } catch (Exception noHostAvailableException) {
            logGenericException(noHostAvailableException);
            throw new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
        }
        log.debug(" exit getActionsByActionInvariantUuId with actionInvariantUuId= " + actionInvariantUuId);
        return actions;
    }

   @Override
    public Action getLockedAction(String actionInvariantUuId, String user) {
        log.debug(" entering getLockedAction with actionInvariantUuId= " + actionInvariantUuId);
        ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION, TARGET_ENTITY_DB);
        Action action = null;
        VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));
        ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
        log.metrics("");
        if (versionInfoEntity != null) {
            if (versionInfoEntity.getCandidate() != null) {
                String actionUser = versionInfoEntity.getCandidate().getUser();
                if (actionUser != null && actionUser.equals(user)) {
                    Set<Version> versions = new HashSet<>();
                    versions.add(versionInfoEntity.getCandidate().getVersion());
                    ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID, TARGET_ENTITY_DB);
                    List<ActionEntity> result = accessor.getActionsByInvId(actionInvariantUuId, new ArrayList<>(versions));
                    ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
                    log.metrics("");
                    if (result != null && !result.isEmpty()) {
                        ActionEntity actionEntity = result.get(0);
                        action = actionEntity != null ? actionEntity.toDto() : null;
                    }
                } else {
                    throw new ActionException(ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE,
                        String.format(ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER, actionUser));
                }
            } else {
                throw new ActionException(ACTION_NOT_LOCKED_CODE, ACTION_NOT_LOCKED_MSG);
            }
        } else {
            throw new ActionException(ACTION_ENTITY_NOT_EXIST_CODE, ACTION_ENTITY_NOT_EXIST);
        }
        return action;
    }


    private void logGenericException(Exception exception) {
        ActionUtil.actionLogPostProcessor(ERROR, ACTION_QUERY_FAILURE_CODE, ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG, false);
        log.metrics("");
        ActionUtil.actionErrorLogProcessor(CategoryLogLevel.FATAL, ACTION_QUERY_FAILURE_CODE, ACTION_QUERY_FAILURE_MSG);
        log.error(exception.getMessage());
    }


     private void updateActionStatusForDelete(String actionInvariantUuId, List<Version> versions) {
        log.debug("entering updateActionStatusForDelete with actionInvariantUuId = " + actionInvariantUuId + FOR_VERSIONS + versions);
        ActionUtil.actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION_STATUS, TARGET_ENTITY_DB);
        // Replaced 3.x BatchStatement with 4.x SimpleStatement using positional binding.
        String cql = "UPDATE dox.\"Action\" SET status = ? WHERE actioninvariantuuid = ? AND version IN ?";
        SimpleStatement stmt = SimpleStatement.builder(cql)
            .addPositionalValue(ActionStatus.Deleted.name())
            .addPositionalValue(actionInvariantUuId)
            .addPositionalValue(versions)
            .build();
        session.execute(stmt);
        ActionUtil.actionLogPostProcessor(COMPLETE, null, "", false);
        log.metrics("");
        // update data field
        updateStatusInActionData(actionInvariantUuId, versions, ActionStatus.Deleted);
        log.debug("exit updateActionStatusForDelete with actionInvariantUuId = " + actionInvariantUuId + FOR_VERSIONS + versions);
    }
    /**
     * Update status for a list of versions for a given action.
     *
     * @param actionInvariantUuId Invariant UUID of the action.
     * @param versions            List of {@link Version} for which the status has to be updated.
     * @param status              The status value.
     */
    private void updateStatusInActionData(String actionInvariantUuId, List<Version> versions, ActionStatus status) {
        log.debug("entering updateStatusInActionData for actionInvariantUuId = " + actionInvariantUuId + " and status = " + status + FOR_VERSIONS
            + versions);
        for (Version v : versions) {
            // use get by primary key
           ActionEntity entity = this.get(new ActionEntity(actionInvariantUuId, v));
           String currentData = entity.getData();
           Map<String, Object> currentDataMap = JsonUtil.json2Object(currentData, LinkedHashMap.class);
           currentDataMap.put(ActionConstants.STATUS, status);

           String updatedActionData = JsonUtil.object2Json(currentDataMap);
           entity.setData(updatedActionData);

        // just update using the existing entity
           this.updateAction(entity.toDto());
           }
            log.debug("exit updateStatusInActionData");
    }

    /**
     * Get list of all major and minor version values for a given action by action name.
     *
     * @param name Name of the action.
     * @return List of {@link Version} objects for the action.
     */
     private List<Version> getVersionsByName(String name) {
        log.debug("entering getVersionsByName for Action Name = " + name);
      // Converted legacy mapper.select() to SimpleStatement with positional parameters.
        SimpleStatement stmt = SimpleStatement.builder("SELECT version FROM dox.\"Action\" WHERE name = ?")
            .addPositionalValue(name)
            .build();
        ResultSet results = session.execute(stmt);
        List<Version> versionList = new ArrayList<>();
        for (Row row : results) {
            // Using Row.get(String, Class<T>) — new Datastax 4.x idiom.
            Version version = row.get(VERSION, Version.class);
            versionList.add(version);
        }
        log.debug("exit getVersionsByName for Action Name = " + name);
        return versionList;
    }

    
    class VersionPredicate {

        Version activeVersion;
        Version finalVersion;

        public boolean isIntermediateMinorVersion(Version version) {
            int activeMajorVersion = activeVersion.getMajor();
            int activeMinorVersion = activeVersion.getMinor();
            int currentMinorVersion = version.getMinor();
            int currentMajorVersion = version.getMajor();
            if (finalVersion != null) {
                if (finalVersion.getMajor() == activeMajorVersion && currentMajorVersion == finalVersion.getMajor()) {
                    if (currentMinorVersion < activeMinorVersion && currentMinorVersion != 0) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                if (!version.equals(activeVersion)) {
                    return true;
                }
            }
            return false;
        }
    }
    @Override
    public Collection<ActionEntity> list(ActionEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'list'");
    }

    @Override
    public void create(ActionEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public void update(ActionEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ActionEntity get(ActionEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void delete(ActionEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
