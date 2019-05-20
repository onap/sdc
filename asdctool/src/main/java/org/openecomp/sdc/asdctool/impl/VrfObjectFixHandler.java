package org.openecomp.sdc.asdctool.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.migration.tasks.handlers.XlsOutputHandler;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@org.springframework.stereotype.Component("vrfObjectFixHandler")
public class VrfObjectFixHandler {

    private static final Logger log = Logger.getLogger(VrfObjectFixHandler.class);
    private static final String VALID_TOSCA_NAME = "org.openecomp.nodes.VRFObject";
    private static final Object[] outputTableTitle =
            new String[]{"VRF OBJECT VERSION",
                    "CONTAINER NAME",
                    "CONTAINER UNIQUE ID",
                    "INSTANCE NAME",
                    "INSTANCE UNIQUE ID"};

    private XlsOutputHandler outputHandler;
    private final String sheetName = this.getClass().getSimpleName() + "Report";

    private JanusGraphDao janusGraphDao;

    public VrfObjectFixHandler(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    public boolean handle(String mode, String outputPath) {
        outputHandler = new XlsOutputHandler(outputPath, sheetName, outputTableTitle);
        switch (mode){
            case "detect" :
                return detectCorruptedData();
            case "fix":
                return fixCorruptedData();
            default :
                log.debug("#handle - The invalid mode parameter has been received: {}", mode);
                return false;
        }
    }

    private boolean fixCorruptedData(){
        try{
            Map<GraphVertex,Map<Vertex, List<ComponentInstanceDataDefinition>>> corruptedData = fetchCorruptedData();
            corruptedData.forEach(this::fixCorruptedVfrObjectAndRelatedInstances);
            janusGraphDao.commit();
            writeOutput(corruptedData);
        } catch (Exception e){
            janusGraphDao.rollback();
            log.debug("#fixCorruptedData - Failed to detect corrupted data. The exception occurred: ", e);
            return false;
        }
        return true;
    }

    private boolean detectCorruptedData(){
        try{
            Map<GraphVertex,Map<Vertex, List<ComponentInstanceDataDefinition>>> corruptedData = fetchCorruptedData();
            writeOutput(corruptedData);
        } catch (Exception e){
            log.debug("#detectCorruptedData - Failed to detect corrupted data. The exception occurred: ", e);
            return false;
        }
        return true;
    }

    private void fixCorruptedVfrObjectAndRelatedInstances(GraphVertex vfrObjectV, Map<Vertex, List<ComponentInstanceDataDefinition>> instances) {
        fixCorruptedVfrObject(vfrObjectV);
        instances.forEach(this::fixCorruptedContainerInstances);
    }

    private void fixCorruptedVfrObject(GraphVertex vfrObjectV) {
        vfrObjectV.getMetadataProperties().put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, VALID_TOSCA_NAME);
        janusGraphDao.updateVertex(vfrObjectV).left().on(this::rightOnUpdate);
    }

    private Map<GraphVertex,Map<Vertex,List<ComponentInstanceDataDefinition>>> fetchCorruptedData(){
        Map<GraphVertex,Map<Vertex, List<ComponentInstanceDataDefinition>>> corruptedData = new HashMap<>();
        List<GraphVertex> vrfObjectsV = getCorruptedVrfObjects();
        vrfObjectsV.forEach(vrfObjectV-> fillCorruptedData(vrfObjectV, corruptedData));
        return corruptedData;
    }

    private List<GraphVertex> getCorruptedVrfObjects() {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, "org.openecomp.resource.configuration.VRFObject");
        return janusGraphDao.getByCriteria(VertexTypeEnum.NODE_TYPE, props).left().on(this::rightOnGet);
    }

    private void fillCorruptedData(GraphVertex vrfObjectV, Map<GraphVertex, Map<Vertex, List<ComponentInstanceDataDefinition>>> findToUpdate) {
        Map<Vertex, List<ComponentInstanceDataDefinition>> corruptedInstances = new HashMap<>();
        findToUpdate.put(vrfObjectV, corruptedInstances);
        Iterator<Edge> instanceEdges = vrfObjectV.getVertex().edges(Direction.IN, EdgeLabelEnum.INSTANCE_OF.name());
        while(instanceEdges.hasNext()){
            Edge edge = instanceEdges.next();
            putCorruptedInstances(corruptedInstances, edge, (List<String>) janusGraphDao
                .getProperty(edge, EdgePropertyEnum.INSTANCES));
        }
    }

    private void putCorruptedInstances(Map<Vertex, List<ComponentInstanceDataDefinition>> corruptedInstances, Edge edge, List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            Vertex container = edge.outVertex();
            Map<String, ? extends ToscaDataDefinition> jsonObj = getJsonMap(container);
            CompositionDataDefinition composition = (CompositionDataDefinition)jsonObj.get(JsonConstantKeysEnum.COMPOSITION.getValue());
            corruptedInstances.put(container, composition.getComponentInstances()
                    .values()
                    .stream()
                    .filter(i->ids.contains(i.getUniqueId()))
                    .collect(toList()));
        }
    }

    private void fixCorruptedContainerInstances(Vertex container, List<ComponentInstanceDataDefinition> corruptedInstances){
        try {
            Map jsonObj = getJsonMap(container);
            fixComponentToscaName(corruptedInstances, jsonObj);
            String jsonMetadataStr = JsonParserUtils.toJson(jsonObj);
            container.property(GraphPropertyEnum.JSON.getProperty(), jsonMetadataStr);
        } catch (IOException e) {
            throw new StorageException("Failed to fix the corrupted instances of the container", e, JanusGraphOperationStatus.GENERAL_ERROR);
        }
    }

    private void fixComponentToscaName(List<ComponentInstanceDataDefinition> corruptedInstances, Map<String, ? extends ToscaDataDefinition> jsonObj) {
        List<String> ids = corruptedInstances
                .stream()
                .map(ComponentInstanceDataDefinition::getUniqueId)
                .collect(toList());

        CompositionDataDefinition composition = (CompositionDataDefinition)jsonObj.get(JsonConstantKeysEnum.COMPOSITION.getValue());
        composition.getComponentInstances()
                .values()
                .stream()
                .filter(i->ids.contains(i.getUniqueId()))
                .forEach(i->i.setToscaComponentName(VALID_TOSCA_NAME));
    }

    private Map getJsonMap(Vertex container) {
        String json = (String)container.property(GraphPropertyEnum.JSON.getProperty()).value();
        Map<GraphPropertyEnum, Object> properties = janusGraphDao.getVertexProperties(container);
        VertexTypeEnum label = VertexTypeEnum.getByName((String) (properties.get(GraphPropertyEnum.LABEL)));
        return JsonParserUtils.toMap(json, label != null ? label.getClassOfJson() : null);
    }

    private void writeOutput(Map<GraphVertex, Map<Vertex, List<ComponentInstanceDataDefinition>>> corruptedData) {
        if(outputHandler.getOutputPath() != null){
            if(MapUtils.isNotEmpty(corruptedData)){
                corruptedData.forEach(this::addVrfObjectRecord);
            } else {
                outputHandler.addRecord("CORRUPTED VRF OBJECT NOT FOUND");
            }
            outputHandler.writeOutputAndCloseFile();
        }
    }

    private List<GraphVertex> rightOnGet(JanusGraphOperationStatus status) {
        if(status == JanusGraphOperationStatus.NOT_FOUND){
            return emptyList();
        }
        throw new StorageException(status);
    }
    private GraphVertex rightOnUpdate(JanusGraphOperationStatus status) {
        throw new StorageException(status);
    }

    private void addVrfObjectRecord(GraphVertex vrfObject, Map<Vertex, List<ComponentInstanceDataDefinition>> instances) {
        outputHandler.addRecord(vrfObject.getMetadataProperties().get(GraphPropertyEnum.VERSION).toString());
        instances.forEach(this::addVrfObjectInstances);
    }

    private void addVrfObjectInstances(Vertex container, List<ComponentInstanceDataDefinition> instances) {
        outputHandler.addRecord("", container.property(GraphPropertyEnum.NAME.getProperty()).value().toString(), container.property(GraphPropertyEnum.UNIQUE_ID.getProperty()).value().toString());
        instances.forEach(i->outputHandler.addRecord("","","",i.getName(),i.getUniqueId()));
    }
}
