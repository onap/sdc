package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component  
public class ResourceLifecycleMigration implements Migration {

    private JanusGraphDao janusGraphDao;
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    private UserAdminOperation userAdminOperation;
    
    private User user = null;

    private static final Logger log = Logger.getLogger(ResourceLifecycleMigration.class);

    public ResourceLifecycleMigration(JanusGraphDao janusGraphDao, LifecycleBusinessLogic lifecycleBusinessLogic, UserAdminOperation userAdminOperation) {
        this.janusGraphDao = janusGraphDao;
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.userAdminOperation = userAdminOperation;
    }

    @Override
    public String description() {
        return "change resource lifecycle state from testing to certified";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        log.info("start change resource lifecycle states migration");
        final String userId = ConfigurationManager.getConfigurationManager().getConfiguration().getAutoHealingOwner();

        Either<User, ActionStatus> userReq = userAdminOperation.getUserData(userId, false);
        if (userReq.isRight()) {
            log.error("Upgrade migration failed. User {} resolve failed: {} ", userId, userReq.right().value());
            return MigrationResult.error("failed to change lifecycle state of resources. Failed to resolve user : " + userId + " error " + userReq.right().value());
        } else {
            user = userReq.left().value();
            log.info("User {} will perform upgrade operation with role {}", user.getUserId(), user.getRole());
        }

        StorageOperationStatus status = changeResourceLifecycleState();

        return status == StorageOperationStatus.OK ? MigrationResult.success() : MigrationResult.error("failed to change lifecycle state of resources. Error : " + status);
    }

    private StorageOperationStatus changeResourceLifecycleState() {
        StorageOperationStatus status;
        status = findResourcesAndChangeStatus(VertexTypeEnum.NODE_TYPE);
        if (StorageOperationStatus.OK == status) {
            status = findResourcesAndChangeStatus(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        }
        janusGraphDao.commit();
        return status;
    }

    private StorageOperationStatus findResourcesAndChangeStatus(VertexTypeEnum type) {
        StorageOperationStatus status;
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());       
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.READY_FOR_CERTIFICATION.name());
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        Map<GraphPropertyEnum, Object> hasNot = new EnumMap<>(GraphPropertyEnum.class);
        hasNot.put(GraphPropertyEnum.IS_DELETED, true);

        log.info("findResourcesAndChangeStatus for type {} and state {}", type ,LifecycleStateEnum.READY_FOR_CERTIFICATION);
        status = janusGraphDao.getByCriteria(type, props, hasNot, JsonParseFlagEnum.ParseAll).either(this::changeState, this::handleError);
        log.info("status {} for type {} and state {}", status, type ,LifecycleStateEnum.READY_FOR_CERTIFICATION);
        
        log.info("findResourcesAndChangeStatus for type {} and state {}", type ,LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
        status = janusGraphDao.getByCriteria(type, props, hasNot, JsonParseFlagEnum.ParseAll).either(this::changeState, this::handleError);
        log.info("status {} for type {} and state {}", status, type ,LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        
        
        return status;
    }

    private StorageOperationStatus changeState(List<GraphVertex> resourcesV) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        
        for (GraphVertex resourceV : resourcesV) {
            status = changeResourceState(resourceV);
            if (status != StorageOperationStatus.OK) {
                log.info("Failed to change state to certified of resource with id {} , continue to next, reset status", resourceV.getUniqueId() );
                status = StorageOperationStatus.OK;
            }
        }
        return status;
    }

    private StorageOperationStatus changeResourceState(GraphVertex resourceV) {
        log.debug("Change state to certified to resource with id {} ", resourceV.getUniqueId() );
        
        LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("change resource state by migration");
        final Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> changeComponentState = lifecycleBusinessLogic.changeComponentState(ComponentTypeEnum.RESOURCE, resourceV.getUniqueId(), user, LifeCycleTransitionEnum.CERTIFY, changeInfo, false, true);
        return changeComponentState.isLeft() ? StorageOperationStatus.OK : StorageOperationStatus.GENERAL_ERROR;
    }

    private StorageOperationStatus handleError(JanusGraphOperationStatus err) {
        log.debug("receive janusgraph error {}", err);
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
            JanusGraphOperationStatus.NOT_FOUND == err ? JanusGraphOperationStatus.OK : err);
    }

}
