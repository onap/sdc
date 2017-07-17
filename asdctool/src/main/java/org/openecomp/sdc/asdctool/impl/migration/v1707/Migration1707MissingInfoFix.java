package org.openecomp.sdc.asdctool.impl.migration.v1707;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.NodeTemplateMissingDataResolver;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("migration1707MissingInfoFix")
public class Migration1707MissingInfoFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(Migration1707MissingInfoFix.class);

    @Resource(name = "service-operation")
    private IServiceOperation serviceOperation;

    @Resource(name = "node-template-missing-data-resolver")
    private NodeTemplateMissingDataResolver nodeTemplateMissingDataResolver;

    @Resource(name = "tosca-operation-facade")
    private ToscaOperationFacade toscaOperations;

    @Resource(name = "titan-dao")
    private TitanDao titanDao;


    public boolean migrate(){
        boolean res = updateVFs();
        if(res)
            res = updateServices();
        return res;
    }

    private ComponentParametersView getFilter() {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreArtifacts(false);
        filter.setIgnoreGroups(false);
        filter.setIgnoreComponentInstancesInputs(false);
        return filter;
    }

    // if new service has VF instances with no groups - try to fetch them from old graph
    private boolean oldServiceModelRequired(Component newService) {
        Predicate<ComponentInstance> vfInstanceWithNoGroups = p -> OriginTypeEnum.VF == p.getOriginType() && (null == p.getGroupInstances() || p.getGroupInstances().isEmpty());
        return null != newService.getComponentInstances() && newService.getComponentInstances().stream()
                .anyMatch(vfInstanceWithNoGroups);
    }



    private List<GraphVertex> fetchVertices(Map<GraphPropertyEnum, Object> hasProps){
        Either<List<GraphVertex>, TitanOperationStatus> componentsByCriteria = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, JsonParseFlagEnum.ParseAll);
        if (componentsByCriteria.isRight()) {
            LOGGER.debug("couldn't fetch assets from sdctitan");
            return null;
        }
        return componentsByCriteria.left().value();
    }

    private boolean updateVFs() {

        boolean res = true;
        Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        hasProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF.name());

        List<GraphVertex> resources = fetchVertices(hasProps);
        if(null == resources)
            return false;
        ComponentParametersView filter = getFilter();
        Map<String, ToscaElement> origCompMap = new HashMap<>();

        for (GraphVertex gv : resources) {
            boolean fixed = true;
            Either<Component, StorageOperationStatus> toscaElement = toscaOperations.getToscaElement(gv.getUniqueId(), filter);
            if (toscaElement.isRight()) {
                LOGGER.debug("Failed to fetch resource {} {}", gv.getUniqueId(), toscaElement.right().value());
                return false;
            }
            Component resource = toscaElement.left().value();
            Map<String, Boolean> updateMap = new HashMap<>();
            nodeTemplateMissingDataResolver.updateVFComposition(resource, origCompMap, updateMap);
            if(updateMap.get(JsonConstantKeysEnum.COMPOSITION.name())){
                LOGGER.info("applying instance tosca name fix on VF {}", gv.getUniqueId());
                fixed = toscaOperations.updateComponentInstanceMetadataOfTopologyTemplate(resource).isLeft();
            }
            if(updateMap.get(EdgeLabelEnum.GROUPS.name())) {
                List<GroupDataDefinition> groups = new ArrayList<>(resource.getGroups());
                LOGGER.info("applying groups vertex fix on VF {}", gv.getUniqueId());
                fixed = fixed && toscaOperations.updateGroupsOnComponent(resource, ComponentTypeEnum.RESOURCE, groups).isLeft();
            }

            res = res && fixed;
            titanDao.commit();
        }
        return res;
    }

    private Map<String, MapPropertiesDataDefinition> buildInstancesInputsMap(Component component){
        Map<String, MapPropertiesDataDefinition> instanceInputsMap = new HashMap<>();
        for (Map.Entry<String, List<ComponentInstanceInput>> entry : component.getComponentInstancesInputs().entrySet()) {
            MapPropertiesDataDefinition inputsMap = new MapPropertiesDataDefinition();
            inputsMap.setMapToscaDataDefinition(entry.getValue().stream().map(e -> new PropertyDataDefinition(e)).collect(Collectors.toMap(e -> e.getName(), e -> e)));
            instanceInputsMap.put(entry.getKey(), inputsMap);
        }
        return instanceInputsMap;
    }



    private Map<String, MapGroupsDataDefinition> buildGroupInstanceMap(Component component) {
        Map<String, MapGroupsDataDefinition> instGroupsMap = new HashMap<>();
        for (ComponentInstance instance : component.getComponentInstances()) {
            if (instance.getGroupInstances() != null) {
                MapGroupsDataDefinition groupsMap = new MapGroupsDataDefinition();
                groupsMap.setMapToscaDataDefinition(instance.getGroupInstances().stream().map(e -> new GroupInstanceDataDefinition(e)).collect(Collectors.toMap(e -> e.getName(), e -> e)));
                instGroupsMap.put(instance.getUniqueId(), groupsMap);
            }
        }
        return instGroupsMap;
    }

    private <T extends ToscaDataDefinition> boolean updateDataVertex(GraphVertex componentVertex, VertexTypeEnum vertexType, EdgeLabelEnum edgeLabel, Map<String, T> dataMap){
        Either<GraphVertex, TitanOperationStatus> dataVertexEither = titanDao.getChildVertex(componentVertex, edgeLabel, JsonParseFlagEnum.ParseJson);
        if (dataVertexEither.isRight()) {
            if(TitanOperationStatus.NOT_FOUND != dataVertexEither.right().value())
                return false;
            return (nodeTemplateMissingDataResolver.topologyTemplateOperation.assosiateElementToData(componentVertex, vertexType, edgeLabel, dataMap)).isLeft();
        }
        GraphVertex dataVertex = dataVertexEither.left().value();
        dataVertex.setJson(dataMap);
        return (titanDao.updateVertex(dataVertex)).isLeft();

    }


    private boolean updateServices(){

        boolean res = true;
        Map<GraphPropertyEnum, Object> hasProps = new HashMap<>();
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());

        List<GraphVertex> componentsByCriteria = fetchVertices(hasProps);
        if(null == componentsByCriteria)
            return false;

        ComponentParametersView filter = getFilter();
        Map<String, ToscaElement> origCompMap = new HashMap<>();

        Predicate<ComponentInstance> containsGroupInstances = p -> null != p.getGroupInstances() && !p.getGroupInstances().isEmpty();

        for (GraphVertex gv : componentsByCriteria) {

            boolean fixed = true;
            Either<org.openecomp.sdc.be.model.Service, StorageOperationStatus> toscaElement = toscaOperations.getToscaElement(gv.getUniqueId(), filter);
            if (toscaElement.isRight()) {
                LOGGER.debug("Failed to fetch service {} {}", gv.getUniqueId(), toscaElement.right().value());
                return false;
            }
            Component service = toscaElement.left().value();
            Component oldService = null;

            if(oldServiceModelRequired(service)){
                Either<Service, StorageOperationStatus> oldServiceEither = serviceOperation.getService(gv.getUniqueId(), filter, false);
                if (oldServiceEither.isRight()){
                    LOGGER.debug("couldn't fetch service {} from old titan", gv.getUniqueId());
                }else {
                    oldService = oldServiceEither.left().value();
                    oldService = oldService.getComponentInstances().stream().anyMatch(containsGroupInstances) ? oldService : null;
                }
            }

            Map<String, Boolean> updateMap = new HashMap<>();
            nodeTemplateMissingDataResolver.updateServiceComposition(service, origCompMap, oldService, updateMap);
            if(updateMap.get(JsonConstantKeysEnum.COMPOSITION.name())) {
                LOGGER.info("applying instance tosca name fix on service {}", gv.getUniqueId());
                fixed = (toscaOperations.updateComponentInstanceMetadataOfTopologyTemplate(service)).isLeft();
            }
            if(updateMap.get(EdgeLabelEnum.INST_GROUPS.name())) {
                Map<String, MapGroupsDataDefinition> groupsMap = buildGroupInstanceMap(service);
                LOGGER.info("applying groups instances vertex fix on service {}", gv.getUniqueId());
                fixed = fixed && updateDataVertex(gv, VertexTypeEnum.INST_GROUPS, EdgeLabelEnum.INST_GROUPS, groupsMap);
            }
            if(updateMap.get(EdgeLabelEnum.INST_INPUTS.name())) {
                Map<String, MapPropertiesDataDefinition> instInputs = buildInstancesInputsMap(service);
                LOGGER.info("applying instances inputs vertex fix on service {}", gv.getUniqueId());
                fixed = fixed && updateDataVertex(gv, VertexTypeEnum.INST_INPUTS, EdgeLabelEnum.INST_INPUTS, instInputs);
            }
            res = res && fixed;
            titanDao.commit();
        }
        return res;
    }
}
