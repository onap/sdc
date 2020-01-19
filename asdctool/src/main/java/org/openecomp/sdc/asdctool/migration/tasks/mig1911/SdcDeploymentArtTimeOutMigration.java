package org.openecomp.sdc.asdctool.migration.tasks.mig1911;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.tasks.InstanceMigrationBase;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class SdcDeploymentArtTimeOutMigration extends InstanceMigrationBase implements Migration {

    private static final Logger log = LoggerFactory.getLogger(SdcDeploymentArtTimeOutMigration.class);
    private static Integer defaultTimeOut = 120;

    public SdcDeploymentArtTimeOutMigration(JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
    }

    @Override
    public String description() {
        return "update instance deployment artifact timeOut to default value 120 minutes";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1911), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        StorageOperationStatus status = updateDeploymentArtifactTimeOut();
        return status == StorageOperationStatus.OK ?
                MigrationResult.success() : MigrationResult.error("failed to update instance deployment artifact timeOut. Error : " + status);
    }

    protected StorageOperationStatus updateDeploymentArtifactTimeOut() {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> byCriteria = janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        return byCriteria.either(this::proceed, this::handleError);
    }

    @Override
    protected StorageOperationStatus handleOneContainer(GraphVertex containerVorig) {
        StorageOperationStatus status = StorageOperationStatus.NOT_FOUND;
        GraphVertex containerV = getVertexById(containerVorig.getUniqueId());
        try {
            Either<GraphVertex, JanusGraphOperationStatus> childVertex = janusGraphDao.getChildVertex(containerV, EdgeLabelEnum.INST_DEPLOYMENT_ARTIFACTS, JsonParseFlagEnum.ParseAll);
            GraphVertex instDeployArt = childVertex.left().value();
            Collection<MapArtifactDataDefinition> values = (Collection<MapArtifactDataDefinition>) instDeployArt.getJson().values();
                List<ArtifactDataDefinition> artifactDataDefinitionsList = values.stream().map(f -> f.getMapToscaDataDefinition().values()).flatMap(f -> f.stream().filter(isRelevantArtifact())).collect(Collectors.toList());
                artifactDataDefinitionsList.forEach(t -> t.setTimeout(defaultTimeOut));
                status =  updateVertexAndCommit(instDeployArt);

        } catch (NullPointerException e) {
            log.error("Null Pointer Exception occurred - this mean we have zombie vertex, migration task will continue anyway", e);
            status = StorageOperationStatus.OK;
        }
        catch (Exception e) {
            //it is happy flow as well
            log.error("Exception occurred:", e);
            log.error("Migration task will continue anyway, please find below vertex details related to this exception", e);
            if (containerV != null){
                log.error("containerV.getUniqueId() {} ---> ", containerV.getUniqueId());
            }

            status = StorageOperationStatus.OK;
        } finally {
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                log.info("failed to update vertex ID {} ",  containerV.getUniqueId());
                if (status == StorageOperationStatus.NOT_FOUND) {
                    //it is happy flow as well
                    status = StorageOperationStatus.OK;
                }
            }
            else{
                log.info("vertex ID {} successfully updated",  containerV.getUniqueId());
            }

        }
        return status;
    }

    private static Predicate<ArtifactDataDefinition> isRelevantArtifact() {

        return p -> ((p.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || p.getArtifactType().equals(ArtifactTypeEnum.HEAT_VOL.getType()) || p.getArtifactType().equals(ArtifactTypeEnum.HEAT_NET.getType()))
                && p.getTimeout() != defaultTimeOut);

    }

}
