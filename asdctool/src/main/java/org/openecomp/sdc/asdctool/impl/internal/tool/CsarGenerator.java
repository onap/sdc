/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.asdctool.impl.internal.tool;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("csarGenerator")
public class CsarGenerator extends CommonInternalTool {
    public CsarGenerator() {
        super("generate");
    }

    @Autowired
    private JanusGraphDao janusGraphDao;
    @Autowired
    private CsarUtils csarUtils;
    @Autowired
    private ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private ArtifactCassandraDao artifactCassandraDao;
    @Autowired
    private ToscaExportHandler toscaExportHandler;
    

    private static Logger log = Logger.getLogger(CsarGenerator.class.getName());

    public void generateCsar(String uuid, Scanner scanner) {
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;

        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.UUID, uuid);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());

        List<GraphVertex> byCriterria = janusGraphDao
            .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, props).either(l -> l, r -> null);
        if (byCriterria != null && !byCriterria.isEmpty()) {
            if (byCriterria.size() > 1) {
                ConsoleWriter.dataLine("Warning ! More that 1 certified service with uuid", uuid);
                // TBD
            } else {
                GraphVertex metadataV = byCriterria.get(0);

                printComponentInfo(metadataV.getMetadataProperties());
                ConsoleWriter.dataLine("\nGenerate CSAR (yes/no)?");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("yes")) {
                    
                    status = handleService(metadataV, uuid);
                }
            }
        } else {
            ConsoleWriter.dataLine("No certified service with UUID", uuid);
        }
        if (status == JanusGraphOperationStatus.OK) {
            janusGraphDao.commit();
        } else {
            janusGraphDao.rollback();
        }
    }

    private JanusGraphOperationStatus handleService(GraphVertex metadataV, String uuid) {
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
        org.openecomp.sdc.be.model.Component component = toscaOperationFacade.getToscaFullElement(metadataV.getUniqueId()).either(l -> l, r -> null);
        if (component != null) {

            Supplier<byte[]> supplier = () -> generateToscaPayload(component);
            generateArtifact(component, ArtifactTypeEnum.TOSCA_TEMPLATE, supplier);
            
            supplier = () -> generateCsarPayload(component);
            generateArtifact(component, ArtifactTypeEnum.TOSCA_CSAR, supplier);
            
            GraphVertex toscaArtifactV = janusGraphDao
                .getChildVertex(metadataV, EdgeLabelEnum.TOSCA_ARTIFACTS, JsonParseFlagEnum.ParseJson).either(l->l, r->null);
            if ( toscaArtifactV != null ){
                Map<String, ArtifactDataDefinition> copy = component.getToscaArtifacts().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArtifactDataDefinition(e.getValue())));
                toscaArtifactV.setJson(copy);
                janusGraphDao.updateVertex(toscaArtifactV);
            }
           
        } else {
            ConsoleWriter.dataLine("Failed to fetch certified service with UUID", uuid);
        }
        return status;
    }

    private JanusGraphOperationStatus generateArtifact(Component component, ArtifactTypeEnum artifactType, Supplier<byte[]> supplier){
        JanusGraphOperationStatus status = JanusGraphOperationStatus.GENERAL_ERROR;
        ArtifactDefinition csarArtifact = null;
        Optional<ArtifactDefinition> op = component.getToscaArtifacts().values().stream().filter(p -> p.getArtifactType().equals(artifactType.getType())).findAny();
        if (op.isPresent()) {
            csarArtifact = op.get();
              
            status = savePayload(component, csarArtifact, supplier);
        }
        return status;
    }
    
    private byte[] generateCsarPayload(org.openecomp.sdc.be.model.Component component) {
        return csarUtils.createCsar(component, true, true).either( l -> l, r -> null);
    }
    private byte[] generateToscaPayload(Component component){
       return toscaExportHandler.exportComponent(component).either(l -> l.getMainYaml().getBytes(), r -> null);
    }

    private JanusGraphOperationStatus savePayload(org.openecomp.sdc.be.model.Component component, ArtifactDefinition csarArtifact, Supplier<byte[]> supplier) {
        byte[] payload = supplier.get();

        if ( payload == null ) {
            ConsoleWriter.dataLine("create artifact failed ", csarArtifact.getArtifactLabel());
            return JanusGraphOperationStatus.GENERAL_ERROR;
        }
        ConsoleWriter.dataLine("createartifact  success ", csarArtifact.getArtifactLabel());
        csarArtifact.setPayload(payload);
        byte[] decodedPayload = csarArtifact.getPayloadData();

        String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(component.getUniqueId(), csarArtifact.getArtifactLabel());
        csarArtifact.setUniqueId(uniqueId);
        csarArtifact.setEsId(csarArtifact.getUniqueId());
        
        ConsoleWriter.dataLine("create artifact unique id ", uniqueId);
        
        
        csarArtifact.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(decodedPayload));
        ESArtifactData artifactData = new ESArtifactData(csarArtifact.getEsId(), decodedPayload);
        artifactCassandraDao.saveArtifact(artifactData);
        ConsoleWriter.dataLine("Artifact generated and saved into Cassandra ", csarArtifact.getArtifactLabel());
        report(component, csarArtifact);

        return JanusGraphOperationStatus.OK;
    }

    private void report(org.openecomp.sdc.be.model.Component component, ArtifactDefinition csarArtifact) {
        Map<String, Object> dataToPrint = new HashMap<>();
        dataToPrint.put("name", component.getName());
        dataToPrint.put("type", component.getComponentType());
        dataToPrint.put("version", component.getVersion());
        dataToPrint.put("UUID", component.getUUID());
        dataToPrint.put("state", component.getLifecycleState());
        dataToPrint.put("archive", component.isArchived());
        dataToPrint.put("id", component.getUniqueId());
        dataToPrint.put("artifact name", csarArtifact.getArtifactLabel());
        dataToPrint.put("artifact id", csarArtifact.getUniqueId());
        dataToPrint.put("csar es id", csarArtifact.getEsId());
        dataToPrint.put("artifact checksum", csarArtifact.getArtifactChecksum());

        try {
            getReportWriter().report(dataToPrint);
        } catch (IOException e) {
            ConsoleWriter.dataLine("\nFailed to created report file.");
        }
    }
}
