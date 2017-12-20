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

package org.openecomp.sdc.translator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileComputeConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.TypeComputeConsolidationData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TestUtils {
  private static final String MANIFEST_NAME = SdcCommon.MANIFEST_NAME;
  private static String zipFilename = "VSP.zip";
  private static String validationFilename = "validationOutput.json";

  private static Logger logger = (Logger) LoggerFactory.getLogger(TestUtils.class);

  private TestUtils() {
  }


  public static void addFilesToTranslator(HeatToToscaTranslator heatToToscaTranslator, String path)
      throws IOException {
    File manifestFile = new File(path);
    File[] files = manifestFile.listFiles();
    byte[] fileContent;

    Assert.assertNotNull("manifest files is empty", files);

    for (File file : files) {

      try (FileInputStream fis = new FileInputStream(file)) {

        fileContent = FileUtils.toByteArray(fis);

        if (file.getName().equals(MANIFEST_NAME)) {
          heatToToscaTranslator.addManifest(MANIFEST_NAME, fileContent);
        } else {
          if (!file.getName().equals(zipFilename) && (!file.getName().equals(validationFilename))) {
            heatToToscaTranslator.addFile(file.getName(), fileContent);
          }
        }
      }
    }
  }

  /**
   * Get tosca service template models for the files in a directory
   * @param baseDirPath base directory for the tosca file
   * @return Map of <ServiceTemplateFilename, ServiceTemplate> for the files in this directory
   */
  public static Map<String, ServiceTemplate> getServiceTemplates(String baseDirPath){
    Map<String, ServiceTemplate> serviceTemplateMap = new HashMap<>();
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    baseDirPath = "."+baseDirPath+"/";
    try {
      String[] fileList = {};
      URL filesDirUrl = TestUtils.class.getClassLoader().getResource(baseDirPath);
      if (filesDirUrl != null && filesDirUrl.getProtocol().equals("file")) {
        fileList = new File(filesDirUrl.toURI()).list();
      } else {
        Assert.fail("Invalid expected output files directory");
      }
      for (int i = 0; i < fileList.length; i++) {

        URL resource = TestUtils.class.getClassLoader().getResource(baseDirPath + fileList[i]);
        ServiceTemplate serviceTemplate = FileUtils.readViaInputStream(resource,
                        stream -> toscaExtensionYamlUtil.yamlToObject(stream, ServiceTemplate.class));

        serviceTemplateMap.put(fileList[i], serviceTemplate);
      }
    } catch (Exception e) {
      logger.debug("",e);
      Assert.fail(e.getMessage());
    }
    return serviceTemplateMap;
  }

  /**
   * Get tosca service template models
   * @param expectedResultMap Map of filename and payload of the expected result files
   * @return Map of <ServiceTemplateFilename, ServiceTemplate> for the files in this directory
   */
  public static Map<String, ServiceTemplate> getServiceTemplates(Map<String, byte[]>
                                                                     expectedResultMap){
    Map<String, ServiceTemplate> serviceTemplateMap = new HashMap<>();
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    for(String fileName : expectedResultMap.keySet()){
      ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject
          (new String(expectedResultMap.get(fileName)), ServiceTemplate.class);
      serviceTemplateMap.put(fileName, serviceTemplate);
    }
    return serviceTemplateMap;
  }


  public static ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
                                                        String globalServiceTemplatesPath,
                                                        String entryDefinitionServiceTemplate)
      throws IOException {
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
    if (entryDefinitionServiceTemplate == null) {
      entryDefinitionServiceTemplate = "MainServiceTemplate.yaml";
    }

    serviceTemplates = getServiceTemplates(serviceTemplatesPath);
    if (globalServiceTemplatesPath != null) {
      serviceTemplates = getServiceTemplates(globalServiceTemplatesPath);
    }

    return new ToscaServiceModel(null, serviceTemplates, entryDefinitionServiceTemplate);
  }

  public static ServiceTemplate loadServiceTemplate(String serviceTemplatePath)
      throws IOException {
    ServiceTemplate serviceTemplateFromYaml = new ServiceTemplate();
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    URL urlFile = TestUtils.class.getResource(serviceTemplatePath);
    if (urlFile != null) {
      File pathFile = new File(urlFile.getFile());
      File[] files = pathFile.listFiles();
      for (File file : files) {
        try (InputStream yamlFile = new FileInputStream(file)) {
          serviceTemplateFromYaml =
              toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
          createConcreteRequirementObjectsInServiceTemplate(serviceTemplateFromYaml, toscaExtensionYamlUtil);
          try {
            yamlFile.close();
          } catch (IOException ignore) {
            logger.debug("",ignore);
          }
        } catch (FileNotFoundException e) {
          throw e;
        } catch (IOException e) {
          throw e;
        }
      }
    } else {
      throw new NotDirectoryException(serviceTemplatePath);
    }
    return serviceTemplateFromYaml;
  }


  public static void loadServiceTemplates(String serviceTemplatesPath,
                                          ToscaExtensionYamlUtil toscaExtensionYamlUtil,
                                          Map<String, ServiceTemplate> serviceTemplates)
      throws IOException {
    URL urlFile = TestUtils.class.getResource(serviceTemplatesPath);
    if (urlFile != null) {
      File pathFile = new File(urlFile.getFile());
      File[] files = pathFile.listFiles();
      if (files != null) {
        addServiceTemplateFiles(serviceTemplates, files, toscaExtensionYamlUtil);
      } else {
        throw new NotDirectoryException(serviceTemplatesPath);
      }
    } else {
      throw new NotDirectoryException(serviceTemplatesPath);
    }
  }

  private static void addServiceTemplateFiles(Map<String, ServiceTemplate> serviceTemplates,
                                              File[] files,
                                              ToscaExtensionYamlUtil toscaExtensionYamlUtil)
      throws IOException {
    for (File file : files) {
      try (InputStream yamlFile = new FileInputStream(file)) {
        ServiceTemplate serviceTemplateFromYaml =
            toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        createConcreteRequirementObjectsInServiceTemplate(serviceTemplateFromYaml, toscaExtensionYamlUtil);
        serviceTemplates.put(file.getName(), serviceTemplateFromYaml);
        try {
          yamlFile.close();
        } catch (IOException ignore) {
          logger.debug("",ignore);
        }
      } catch (FileNotFoundException e) {
        throw e;
      } catch (IOException e) {
        throw e;
      }
    }
  }

  private static void createConcreteRequirementObjectsInServiceTemplate(ServiceTemplate
                                                                          serviceTemplateFromYaml,
                                                                      ToscaExtensionYamlUtil
                                                                          toscaExtensionYamlUtil) {

    if (serviceTemplateFromYaml == null
        || serviceTemplateFromYaml.getTopology_template() == null
        || serviceTemplateFromYaml.getTopology_template().getNode_templates() == null) {
      return;
    }

    //Creating concrete objects
    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplateFromYaml.getTopology_template().getNode_templates();
    for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
      NodeTemplate nodeTemplate = entry.getValue();
      List<Map<String, RequirementAssignment>> requirements = nodeTemplate.getRequirements();
      List<Map<String, RequirementAssignment>> concreteRequirementList = new ArrayList<>();
      if (requirements != null) {
        ListIterator<Map<String, RequirementAssignment>> reqListIterator = requirements
            .listIterator();
        while (reqListIterator.hasNext()){
          Map<String, RequirementAssignment> requirement = reqListIterator.next();
          Map<String, RequirementAssignment> concreteRequirement = new HashMap<>();
          for (Map.Entry<String, RequirementAssignment> reqEntry : requirement.entrySet()) {
            RequirementAssignment requirementAssignment = (toscaExtensionYamlUtil
                .yamlToObject(toscaExtensionYamlUtil.objectToYaml(reqEntry.getValue()),
                    RequirementAssignment.class));
            concreteRequirement.put(reqEntry.getKey(), requirementAssignment);
            concreteRequirementList.add(concreteRequirement);
            reqListIterator.remove();
          }
        }
        requirements.clear();
        requirements.addAll(concreteRequirementList);
        nodeTemplate.setRequirements(requirements);
      }
      System.out.println();
      //toscaExtensionYamlUtil.yamlToObject(nodeTemplate, NodeTemplate.class);
    }
  }

  public static void updateMultiplePortConsolidationDatas(String serviceTemplateName,
                                                          List<String> portNodeTemplateIds,
                                                          List<List<String>> nodesConnectedInIds,
                                                          List<List<String>> nodesConnectedOutIds,
                                                          List<List<String>> groupIds,
                                                          List<List<String>> getAttrInIds,
                                                          List<List<Pair<String, GetAttrFuncData>>> getAttrOutFuncDataList,
                                                          ConsolidationData consolidationData){
    for(int i = 0; i < portNodeTemplateIds.size(); i++){
      updatePortConsolidationData(serviceTemplateName, portNodeTemplateIds.get(i),
          nodesConnectedInIds.get(i), nodesConnectedOutIds.get(i),
          groupIds.get(i), getAttrInIds.get(i),getAttrOutFuncDataList.get(i), consolidationData);
    }
  }

  public static void updatePortConsolidationData(String serviceTemplateFileName,
                                                 String portNodeTemplateId,
                                                 List<String> nodesConnectedInIds,
                                                 List<String> nodesConnectedOutIds,
                                                 List<String> groupIds, List<String> getAttrInIds,
                                                 List<Pair<String, GetAttrFuncData>> getAttrOutFuncDataList,
                                                 ConsolidationData consolidationData) {

    PortTemplateConsolidationData portTemplateConsolidationData =
        createPortTemplateConsolidationData(portNodeTemplateId);

    updateRelationsForEntityConsolidationData(portNodeTemplateId, nodesConnectedInIds,
        nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOutFuncDataList, portTemplateConsolidationData);

    consolidationData.getPortConsolidationData()
        .getFilePortConsolidationData(serviceTemplateFileName)
        .setPortTemplateConsolidationData(portNodeTemplateId, portTemplateConsolidationData);
  }

  public static PortTemplateConsolidationData createPortTemplateConsolidationData(
      String portNodeTemplateId) {
    PortTemplateConsolidationData portTemplateConsolidationData =
        new PortTemplateConsolidationData();
    portTemplateConsolidationData.setNodeTemplateId(portNodeTemplateId);
    return portTemplateConsolidationData;
  }

  public static void initPortConsolidationData(String serviceTemplateFileName,
                                               ConsolidationData consolidationData) {

    consolidationData.getPortConsolidationData()
        .setFilePortConsolidationData(serviceTemplateFileName, new FilePortConsolidationData());
  }

  public static void updateComputeTemplateConsolidationData(String serviceTemplateFileName,
                                                            String computeNodeTypeName,
                                                            String computeNodeTemplateId,
                                                            List<String> nodeIdsConnectedIn,
                                                            List<String> nodeIdsConnectedOut,
                                                            List<String> volumeIds,
                                                            List<String> groupIds,
                                                            List<String> getAttrInIds,
                                                            List<Pair<String, GetAttrFuncData>> getAttrOutIds,
                                                            List<Pair<String, String>> portTypeToIdList,
                                                            ConsolidationData consolidationData) {

    initComputeNodeTemplateIdInConsolidationData(serviceTemplateFileName, computeNodeTypeName,
        computeNodeTemplateId, consolidationData);

    Map<String, List<RequirementAssignmentData>> volumes =
        consolidationData.getComputeConsolidationData().getFileComputeConsolidationData
            (serviceTemplateFileName).getTypeComputeConsolidationData(computeNodeTypeName)
            .getComputeTemplateConsolidationData(computeNodeTemplateId).getVolumes();

    ComputeTemplateConsolidationData computeTemplateConsolidationData =
        createComputeTemplateConsolidationData(computeNodeTemplateId, portTypeToIdList, volumes);

    updateRelationsForEntityConsolidationData(computeNodeTemplateId, nodeIdsConnectedIn,
        nodeIdsConnectedOut, groupIds, getAttrInIds, getAttrOutIds, computeTemplateConsolidationData);

    updateVolumes(computeTemplateConsolidationData, volumeIds);

    consolidationData.getComputeConsolidationData()
        .getFileComputeConsolidationData(serviceTemplateFileName)
        .getTypeComputeConsolidationData(computeNodeTypeName)
        .setComputeTemplateConsolidationData(computeNodeTemplateId,
            computeTemplateConsolidationData);
  }

  private static void updateRelationsForEntityConsolidationData(String entityNodeTemplateId,
                                                                List<String> nodeIdsConnectedIn,
                                                                List<String> nodeIdsConnectedOut,
                                                                List<String> groupIds,
                                                                List<String> getAttrInIds,
                                                                List<Pair<String, GetAttrFuncData>> getAttrOutFuncDataList,
                                                                EntityConsolidationData entity) {
    updateRelationsIn(entity, nodeIdsConnectedIn);
    updateRelationsOut(entity, entityNodeTemplateId, nodeIdsConnectedOut);
    updateGetAttrIn(entity, getAttrInIds);
    updateGetAttrOut(entity, getAttrOutFuncDataList);
    entity.setGroupIds(groupIds);
  }

  public static void initComputeNodeTemplateIdInConsolidationData(String serviceTemplateFileName,
                                                                   String computeNodeTypeName,
                                                                   String computeNodeTemplateId,
                                                                   ConsolidationData consolidationData) {

    if(Objects.isNull(consolidationData.getComputeConsolidationData().getFileComputeConsolidationData
        (serviceTemplateFileName))) {
      consolidationData.getComputeConsolidationData().setFileComputeConsolidationData
          (serviceTemplateFileName, new FileComputeConsolidationData());
    }
    TypeComputeConsolidationData typeComputeConsolidationData =
        consolidationData.getComputeConsolidationData().getFileComputeConsolidationData
            (serviceTemplateFileName).getTypeComputeConsolidationData(computeNodeTypeName);

    if(
        typeComputeConsolidationData.getComputeTemplateConsolidationData(computeNodeTemplateId) == null) {

      consolidationData.getComputeConsolidationData()
          .getFileComputeConsolidationData(serviceTemplateFileName)
          .getTypeComputeConsolidationData(computeNodeTypeName)
          .setComputeTemplateConsolidationData(computeNodeTemplateId, new ComputeTemplateConsolidationData());

    }
  }

  public static void updateNestedConsolidationData(String serviceTemplateName,
                                                   List<String> substitutionNodeTemplateIds,
                                                   ConsolidationData consolidationData){
    if(Objects.isNull(consolidationData.getNestedConsolidationData())){
      consolidationData.setNestedConsolidationData(new NestedConsolidationData());
    }

    FileNestedConsolidationData fileNestedConsolidationData = new FileNestedConsolidationData();
    for(String substitutionNodeTemplateId : substitutionNodeTemplateIds) {
      NestedTemplateConsolidationData nestedTemplateConsolidationData = new NestedTemplateConsolidationData();
      nestedTemplateConsolidationData.setNodeTemplateId(substitutionNodeTemplateId);
      fileNestedConsolidationData.setNestedTemplateConsolidationData(substitutionNodeTemplateId, nestedTemplateConsolidationData);
    }
    consolidationData.getNestedConsolidationData().setFileNestedConsolidationData(serviceTemplateName, fileNestedConsolidationData);
  }

  public static ComputeTemplateConsolidationData createComputeTemplateConsolidationData(String computeNodeTemplateId,
                                                                                        List<Pair<String, String>> portTypeToIdList,
                                                                                        Map<String,List<RequirementAssignmentData>> volumes) {
    ComputeTemplateConsolidationData compute = new ComputeTemplateConsolidationData();
    compute.setNodeTemplateId(computeNodeTemplateId);
    if (portTypeToIdList != null) {
      for (Pair<String, String> port : portTypeToIdList) {
        compute.addPort(port.getLeft(), port.getRight());
      }
    }
    compute.setVolumes(volumes);
    return compute;
  }

  public static void updateRelationsIn(EntityConsolidationData entity,
                                       List<String> idsPontingTome){
    if(CollectionUtils.isEmpty(idsPontingTome)){
      return;
    }

    for(String pointingId : idsPontingTome){
      entity.addNodesConnectedIn(pointingId, entity.getNodeTemplateId(), new RequirementAssignment());
    }
  }

  public static void updateRelationsOut(EntityConsolidationData entity,
                                        String nodeTemplateId,
                                        List<String> idsToUpdate){
    if(CollectionUtils.isEmpty(idsToUpdate)){
      return;
    }

    for(String id : idsToUpdate){
      entity.addNodesConnectedOut(id, id, new RequirementAssignment());
    }
  }

  public static void updateGetAttrIn(EntityConsolidationData entity,
                                     List<String> idsToUpdate){
    if(CollectionUtils.isEmpty(idsToUpdate)){
      return;
    }

    for(String id : idsToUpdate){
      entity.addNodesGetAttrIn(id, new GetAttrFuncData());
    }
  }

  public static void updateGetAttrOut(EntityConsolidationData entity,
                                      List<Pair<String, GetAttrFuncData>> getAttrOutIds){
    if(CollectionUtils.isEmpty(getAttrOutIds)){
      return;
    }

    for(Pair<String, GetAttrFuncData> getAttrOutFunc : getAttrOutIds){
      entity.addNodesGetAttrOut(getAttrOutFunc.getLeft(), getAttrOutFunc.getRight());
    }
  }

  public static void updateVolumes(ComputeTemplateConsolidationData compute,
                                   List<String> volumeIds){
    if(CollectionUtils.isEmpty(volumeIds)){
      return;
    }

    for(String id : volumeIds){
      RequirementAssignment requirementAssignment = new RequirementAssignment();
      requirementAssignment.setNode(id);
      compute.addVolume(id, requirementAssignment);
    }
  }

  public static void initComputeNodeTypeInConsolidationData(String serviceTemplateFileName,
                                                            String computeNodeTypeName,
                                                            ConsolidationData consolidationData) {
    ComputeConsolidationData computeConsolidationData =
        consolidationData.getComputeConsolidationData();
    if (!computeConsolidationData.getAllServiceTemplateFileNames()
        .contains(serviceTemplateFileName)) {
      computeConsolidationData
          .setFileComputeConsolidationData(serviceTemplateFileName,
              new FileComputeConsolidationData());
    }
    computeConsolidationData
        .getFileComputeConsolidationData(serviceTemplateFileName).setTypeComputeConsolidationData(
        computeNodeTypeName, new TypeComputeConsolidationData());
  }

  public static Map<String, List<RequirementAssignmentData>> getNodeConnectedOutList(
      NodeTemplate nodeTemplate, String requirementKey) {
    Map<String, List<RequirementAssignmentData>> requirementAssignmentDataMap = new HashMap<>();
    Optional<List<RequirementAssignmentData>> requirementAssignmentDataList =
        TestUtils.createRequirementAssignmentDataList(nodeTemplate, requirementKey);
    if (requirementAssignmentDataList.isPresent()) {
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList
          .get()) {
        String connectedNodeTemplateId = requirementAssignmentData.getRequirementAssignment()
            .getNode();
        requirementAssignmentDataMap
            .computeIfAbsent(connectedNodeTemplateId, k -> new ArrayList<>());
        requirementAssignmentDataMap.get(connectedNodeTemplateId).add(requirementAssignmentData);
      }
    }
    return requirementAssignmentDataMap;
  }

  public static Map<String, List<GetAttrFuncData>> getNodesGetAttrIn(NodeTemplate nodeTemplate,
                                                                     String nodeTemplateId) {
    Map<String, List<GetAttrFuncData>> nodesGetAttrIn = new HashMap<>();
    List<GetAttrFuncData> getAttrList = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> nodeTemplateProperties = nodeTemplate.getProperties();
    for (Map.Entry<String, Object> propertyEntry : nodeTemplateProperties.entrySet()) {
      Map<String, List> propertyValue = mapper.convertValue(propertyEntry.getValue(), Map.class);
      for (Map.Entry<String, List> entry : propertyValue.entrySet()) {
        if (entry.getKey().equals("get_attribute")) {
          GetAttrFuncData data = new GetAttrFuncData();
          data.setFieldName(propertyEntry.getKey());
          data.setAttributeName(entry.getValue().get(1).toString());
          getAttrList.add(data);
        }
      }
      System.out.println();
    }
    nodesGetAttrIn.put(nodeTemplateId, getAttrList);
    return nodesGetAttrIn;
  }

  public static void updatePortsInComputeTemplateConsolidationData(
      List<Pair<String, String>> portIdToTypeList, ComputeTemplateConsolidationData
      compute){
    compute.setPorts(new HashMap<>());
    for(Pair<String, String> portIdToType : portIdToTypeList){
      compute.getPorts().putIfAbsent(portIdToType.getLeft(), new ArrayList<>());
      compute.getPorts().get(portIdToType.getLeft()).add(portIdToType.getRight());
    }
  }

  public static Map<String, List<RequirementAssignmentData>> getNodeConnectedInList(
      String sourceNodeTemplateId,
      ServiceTemplate serviceTemplate, String requirementKey) {
    Optional<List<RequirementAssignmentData>> requirementAssignmentDataList = Optional.empty();
    List<RequirementAssignmentData> assignmentDataList = new ArrayList<>();
    Map<String, List<RequirementAssignmentData>> requirementAssignmentDataMap = new HashMap<>();
    Map<String, NodeTemplate> nodeTemplates = serviceTemplate.getTopology_template()
        .getNode_templates();
    for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
      String nodeTemplateId = entry.getKey();
      List<Map<String, RequirementAssignment>> requirements = entry.getValue().getRequirements();
      if (requirements != null) {
        for (Map<String, RequirementAssignment> requirement : requirements) {
          if (requirement.get(requirementKey) != null) {
            RequirementAssignment requirementAssignment = requirement.get(requirementKey);
            if (requirementAssignment != null) {
              if (requirementAssignment.getNode().equals(sourceNodeTemplateId)) {
                RequirementAssignmentData data = new RequirementAssignmentData(requirementKey,
                    requirementAssignment);
                assignmentDataList.add(data);
              }
            }
          }
        }
        requirementAssignmentDataList = Optional.ofNullable(assignmentDataList);
        if (requirementAssignmentDataList.isPresent()) {
          for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList
              .get()) {
            requirementAssignmentDataMap.computeIfAbsent(nodeTemplateId, k -> new ArrayList<>());
            requirementAssignmentDataMap.get(nodeTemplateId).add(requirementAssignmentData);
          }
        }
        requirementAssignmentDataList = Optional.empty();
      }
    }
    return requirementAssignmentDataMap;
  }

  public static List<String> getGroupsForNode(ServiceTemplate serviceTemplate, String
      nodeTemplateId) {
    List<String> entityGroups = new ArrayList<>();
    Map<String, GroupDefinition> groups = serviceTemplate.getTopology_template().getGroups();
    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplate.getTopology_template().getNode_templates();
    for (Map.Entry<String, GroupDefinition> entry : groups.entrySet()) {
      String groupId = entry.getKey();
      GroupDefinition groupDefinition = entry.getValue();
      if (groupDefinition.getType().contains("HeatStack"))
        continue;
      List<String> groupMembers = groupDefinition.getMembers();
      for (String member : groupMembers) {
        if (groups.containsKey(member))
          continue;
        if (member.equals(nodeTemplateId)) {
          entityGroups.add(groupId);
        }
      }
    }
    return entityGroups;
  }

  private static Optional<List<RequirementAssignmentData>> createRequirementAssignmentDataList(
      NodeTemplate nodeTemplate, String requirementKey) {

    Optional<List<RequirementAssignment>> requirementAssignmentLink =
        DataModelUtil.getRequirementAssignment(nodeTemplate.getRequirements(), requirementKey);
    if (!requirementAssignmentLink.isPresent()) {
      return Optional.empty();
    }

    List<RequirementAssignmentData> requirementAssignmentDataList = new ArrayList<>();
    for (RequirementAssignment requirementAssignment : requirementAssignmentLink.get()) {
      RequirementAssignmentData requirementAssignmentData = new RequirementAssignmentData
          (requirementKey, requirementAssignment);
      requirementAssignmentDataList.add(requirementAssignmentData);
    }
    return Optional.ofNullable(requirementAssignmentDataList);
  }

  public static Optional<List<RequirementAssignmentData>> getRequirementAssignmentDataList(
      NodeTemplate nodeTemplate, String requirementKey) {
    List<RequirementAssignmentData> returnedReqAssignmentDataList = new ArrayList<>();
    Optional<List<RequirementAssignmentData>> requirementAssignmentDataList =
        TestUtils.createRequirementAssignmentDataList(nodeTemplate, requirementKey);

    if (requirementAssignmentDataList.isPresent()) {
      for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList
          .get()) {
        returnedReqAssignmentDataList.add(requirementAssignmentData);
      }
      return Optional.of(returnedReqAssignmentDataList);
    }
    return Optional.empty();
  }



}
