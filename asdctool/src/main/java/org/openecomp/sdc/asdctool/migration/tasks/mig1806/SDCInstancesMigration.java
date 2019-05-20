package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import fj.data.Either;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

@Component
public class SDCInstancesMigration implements Migration {

    private JanusGraphDao janusGraphDao;
    private NodeTemplateOperation nodeTemplateOperation;

    private static final Logger log = Logger.getLogger(SDCInstancesMigration.class);

    private static final String ALLOTTED_CATEGORY = "Allotted Resource";
    
    private static final List<String> UUID_PROPS_NAMES = Arrays.asList("providing_service_uuid", "providing_service_uuid");
 
 
    public SDCInstancesMigration(JanusGraphDao janusGraphDao, NodeTemplateOperation nodeTemplateOperation) {
        this.janusGraphDao = janusGraphDao;
        this.nodeTemplateOperation = nodeTemplateOperation;
    }

    @Override
    public String description() {
        return "connect instances in container to its origins";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        StorageOperationStatus status = connectAllContainers();

        return status == StorageOperationStatus.OK ? MigrationResult.success() : MigrationResult.error("failed to create connection between instances and origins. Error : " + status);
    }

    private StorageOperationStatus connectAllContainers() {
        StorageOperationStatus status;
        Map<GraphPropertyEnum, Object> hasNotProps = new EnumMap<>(GraphPropertyEnum.class);
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.CVFC);

        status = janusGraphDao
            .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, hasNotProps, JsonParseFlagEnum.ParseAll)
                .either(this::connectAll, this::handleError);
        return status;
    }

    private StorageOperationStatus handleError(JanusGraphOperationStatus err) {
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
            JanusGraphOperationStatus.NOT_FOUND == err ? JanusGraphOperationStatus.OK : err);
    }

    private StorageOperationStatus connectAll(List<GraphVertex> containersV) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        for (GraphVertex container : containersV) {
            status = handleOneContainer(container);
            if (status != StorageOperationStatus.OK) {
                break;
            }
        }
        return status;
    }

    private StorageOperationStatus handleOneContainer(GraphVertex containerV) {
        StorageOperationStatus status = StorageOperationStatus.OK;

        boolean needConnectAllotted = false;
        ComponentTypeEnum componentType = containerV.getType();
        Map<String, MapPropertiesDataDefinition> instanceProperties = null;
        if (componentType == ComponentTypeEnum.RESOURCE) {
            Either<GraphVertex, JanusGraphOperationStatus> subcategoryV = janusGraphDao
                .getChildVertex(containerV, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse);
            if (subcategoryV.isRight()) {
                log.debug("Failed to fetch category vertex for resource {} error {}  ", containerV.getUniqueId(), subcategoryV.right().value());
                return StorageOperationStatus.GENERAL_ERROR;
            }
            GraphVertex catV = subcategoryV.left().value();
            Map<GraphPropertyEnum, Object> metadataProperties = catV.getMetadataProperties();

            String name = (String) metadataProperties.get(GraphPropertyEnum.NAME);
            if (name.equals(ALLOTTED_CATEGORY)) {
                log.debug("Find allotted  resource {}.", containerV.getUniqueId());
                needConnectAllotted = true;
                Either<Map<String, MapPropertiesDataDefinition>, StorageOperationStatus> instProperties = getInstProperties(containerV);
                if ( instProperties.isRight() ){
                    return instProperties.right().value();
                }
                instanceProperties = instProperties.left().value();
            }
        }
        Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) containerV.getJson();
        if (jsonComposition != null && !jsonComposition.isEmpty()) {
            try {
                status = connectInstances(containerV, needConnectAllotted, instanceProperties, jsonComposition);

            } finally {
                if (status == StorageOperationStatus.OK) {
                    janusGraphDao.commit();
                } else {
                    janusGraphDao.rollback();
                }
            }
        }
        return status;
    }

    private Either<Map<String, MapPropertiesDataDefinition>, StorageOperationStatus> getInstProperties(GraphVertex containerV) {
        Map<String, MapPropertiesDataDefinition> instanceProperties;
       Either<GraphVertex, JanusGraphOperationStatus> instProps = janusGraphDao
           .getChildVertex(containerV, EdgeLabelEnum.INST_PROPERTIES, JsonParseFlagEnum.ParseAll);
      
        if (instProps.isRight()) {
            if (instProps.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
                instanceProperties = new HashMap<>();
            } else {
                log.debug("Failed to fetch instance properties vertex for resource {} error {}  ", containerV.getUniqueId(), instProps.right().value());
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        } else {
            instanceProperties = (Map<String, MapPropertiesDataDefinition>) instProps.left().value().getJson();
        }
        return Either.left(instanceProperties);
    }

    private StorageOperationStatus connectInstances(GraphVertex containerV, boolean needConnectAllotted, Map<String, MapPropertiesDataDefinition> instanceProperties,
            Map<String, CompositionDataDefinition> jsonComposition) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
        Map<String, ComponentInstanceDataDefinition> componentInstances = compositionDataDefinition.getComponentInstances();
        for (Map.Entry<String, ComponentInstanceDataDefinition> entry : componentInstances.entrySet()) {
            status = handleInstance(containerV, needConnectAllotted, instanceProperties, entry);
            if ( status != StorageOperationStatus.OK){
                if ( status == StorageOperationStatus.NOT_FOUND ){
                    log.debug("reset status and continue");
                    status = StorageOperationStatus.OK;
                }else{
                    log.debug("Failed handle instance. exit");
                    break;
                }
            }
        }
        return status;
    }

    private StorageOperationStatus handleInstance(GraphVertex containerV, boolean needConnectAllotted, Map<String, MapPropertiesDataDefinition> instanceProperties, Map.Entry<String, ComponentInstanceDataDefinition> entry) {
        ComponentInstanceDataDefinition instance = entry.getValue();
        StorageOperationStatus status = nodeTemplateOperation.createInstanceEdge(containerV, instance);
        if (status != StorageOperationStatus.OK) {
            if ( status == StorageOperationStatus.NOT_FOUND ){
                Boolean highest = (Boolean) containerV.getMetadataProperties().get(GraphPropertyEnum.IS_HIGHEST_VERSION);
                log.debug("No origin for instance {} with ID {}. The component is highest ={},  Reset status and continue.. ", instance.getUniqueId(), instance.getComponentUid(), highest);
                status = StorageOperationStatus.OK;
            }else{
                log.debug("Failed to connect in container {} instance {} to origin {} error {}  ", containerV.getUniqueId(), instance.getUniqueId(), instance.getComponentUid(), status);
                return status;
            }
        }
        if (needConnectAllotted) {
            status = connectAllotedInstances(containerV, instanceProperties, instance);
        }
        return status;
    }

    private StorageOperationStatus connectAllotedInstances(GraphVertex containerV, Map<String, MapPropertiesDataDefinition> instanceProperties, ComponentInstanceDataDefinition instance) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        if ( instanceProperties != null ){
            MapPropertiesDataDefinition mapPropertiesDataDefinition = instanceProperties.get(instance.getUniqueId());
            if ( mapPropertiesDataDefinition != null ){
                status = checkAllottedPropertyAndConnect(containerV, instance, mapPropertiesDataDefinition);
            }else{
                log.debug("No isntances properties for instance {}", instance.getUniqueId());
            }
        }
        return status;
    }

    private StorageOperationStatus checkAllottedPropertyAndConnect(GraphVertex containerV, ComponentInstanceDataDefinition instance, MapPropertiesDataDefinition mapPropertiesDataDefinition) {
        Map<String, PropertyDataDefinition> mapToscaDataDefinition = mapPropertiesDataDefinition.getMapToscaDataDefinition();
        StorageOperationStatus status = StorageOperationStatus.OK;
        Optional<Entry<String, PropertyDataDefinition>> findFirst = mapToscaDataDefinition
                .entrySet()
                .stream()
                .filter(e -> UUID_PROPS_NAMES.contains(e.getKey()))
                .findFirst();
        
        if ( findFirst.isPresent() ){
            PropertyDataDefinition property = findFirst.get().getValue();
            String serviceUUID = property.getValue(); 
            if ( serviceUUID != null ){
                log.debug("Defined reference service on property {} value {} on instance {}", property.getName(), property.getValue(), instance.getUniqueId() );
                status = nodeTemplateOperation.createAllottedOfEdge(containerV.getUniqueId(), instance.getUniqueId(), serviceUUID);
                if ( status != StorageOperationStatus.OK ){
                    if ( status == StorageOperationStatus.NOT_FOUND ){
                        Boolean highest = (Boolean) containerV.getMetadataProperties().get(GraphPropertyEnum.IS_HIGHEST_VERSION);
                        log.debug("No origin for allotted reference {} with UUID {}. the component highest = {}, Reset status and continue.. ", instance.getUniqueId(), serviceUUID, highest);
                        status = StorageOperationStatus.OK;
                    }else{
                        log.debug("Failed to connect in container {} instance {} to allotted service {} error {}  ", containerV.getUniqueId(), instance.getUniqueId(), instance.getComponentUid(), status);
                        return status;
                    }
                }
            }else{
                log.debug("No value for property {} on instance {}", property.getName(),instance.getUniqueId() );
            }
        }else{
            log.debug("No sercific properties of dependencies for instance {}", instance.getUniqueId());
        }
        return status;
    }

}
