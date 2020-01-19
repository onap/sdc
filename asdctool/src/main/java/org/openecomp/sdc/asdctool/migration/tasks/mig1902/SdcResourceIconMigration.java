package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import com.google.common.annotations.VisibleForTesting;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.tasks.InstanceMigrationBase;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SdcResourceIconMigration extends InstanceMigrationBase implements Migration {

    private static final Logger log = Logger.getLogger(SdcResourceIconMigration.class);

    private Map <String, String> resourceTypeToIconMap = new HashMap<>();

    @VisibleForTesting
    SdcResourceIconMigration(JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
    }


    @Override
    public String description() {
        return "update iconPath for VL and CP nodes";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1902), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        StorageOperationStatus status;
        try {
            updateNodeTypeIconAndStoreInMap(ResourceTypeEnum.VL);
            updateNodeTypeIconAndStoreInMap(ResourceTypeEnum.CP);

            if (!resourceTypeToIconMap.isEmpty()) {
                status = upgradeTopologyTemplates();
            } else {
                log.error("No VL and CP node definitions found");
                status = StorageOperationStatus.NOT_FOUND;
            }
        }
        catch(Exception e) {
            log.error("Exception thrown: {}", e);
            status = StorageOperationStatus.GENERAL_ERROR;
        }
        return status == StorageOperationStatus.OK ?
                    MigrationResult.success() : MigrationResult.error("failed to update iconPath for VL and CP nodes. Error : " + status);
    }

    @Override
    protected StorageOperationStatus handleOneContainer(GraphVertex containerVorig) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        GraphVertex containerV = getVertexById(containerVorig.getUniqueId());

        Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>)containerV.getJson();
        if (jsonComposition != null && !jsonComposition.isEmpty()) {
            CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
            Map<String, ComponentInstanceDataDefinition> componentInstances = compositionDataDefinition.getComponentInstances();

            long updateCount = componentInstances.values()
                    .stream()
                    .filter(this::updateIconInsideInstance).count();
            if (updateCount > 0) {
                status = updateVertexAndCommit(containerV);
            }
        }
        else {
            log.warn("No json found for template <{}> uniqueId <{}>",
                    containerV.getMetadataProperties().get(GraphPropertyEnum.NAME),
                    containerV.getMetadataProperties().get(GraphPropertyEnum.UNIQUE_ID));
        }
        if (log.isInfoEnabled()) {
            log.info("Upgrade status is <{}> for topology template <{}> uniqueId <{}>",
                    status.name(), containerV.getMetadataProperties().get(GraphPropertyEnum.NAME),
                    containerV.getMetadataProperties().get(GraphPropertyEnum.UNIQUE_ID));
        }
        return status;
    }


    @VisibleForTesting
    boolean updateIconInsideInstance(ComponentInstanceDataDefinition componentInstanceDataDefinition) {
        String iconPath = resourceTypeToIconMap.get(componentInstanceDataDefinition.getComponentName());
        if (iconPath != null) {
            componentInstanceDataDefinition.setIcon(iconPath);
            if (log.isDebugEnabled()) {
                log.debug("Icon of component {} is set to {}", componentInstanceDataDefinition.getComponentName(), iconPath);
            }
            return true;
        }
        return false;
    }

    @VisibleForTesting
    void updateNodeTypeIconAndStoreInMap(ResourceTypeEnum resourceType) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(GraphPropertyEnum.RESOURCE_TYPE, resourceType.name());
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        String iconPath = String.valueOf(resourceType.getValue()).toLowerCase();

        Map<String, String> resourceNameToIconMap = janusGraphDao.getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll)
                .either(vl-> updateIconResource(vl, iconPath), status->null);

        if (resourceNameToIconMap != null) {
            resourceTypeToIconMap.putAll(resourceNameToIconMap);
        }
        else {
            log.warn("Failed to get resources of type <{}>", resourceType.name());
        }
    }

    private Map <String, String> updateIconResource(List<GraphVertex> vertexList, String iconPath) {
        if (vertexList.isEmpty()) {
            return null;
        }
        Map <String, String> nameToIconMap = new HashMap<>();
        vertexList.forEach(v->{
            StorageOperationStatus status = updateIconOnVertex(v, iconPath);
            if (status == StorageOperationStatus.OK) {
                if (log.isDebugEnabled()) {
                    log.debug("Node type's {} icon is updated to {}", v.getMetadataProperty(GraphPropertyEnum.NAME), iconPath);
                }
                nameToIconMap.put(String.valueOf(v.getMetadataProperty(GraphPropertyEnum.NAME)), iconPath);
            }
            else {
                log.error("Failed to update node type {} icon due to a reason: {}",
                                v.getMetadataProperty(GraphPropertyEnum.NAME), status);
                throw new RuntimeException("Node update failure");
            }
        });
        return nameToIconMap;
    }

    private StorageOperationStatus updateIconOnVertex(GraphVertex vertex, String iconPath) {
        vertex.setJsonMetadataField(JsonPresentationFields.ICON, iconPath);
        return updateVertexAndCommit(vertex);
    }

}
