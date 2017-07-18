package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class UnifiedSubstitutionData {

  //Key - node template id, Value - related abstract node template id
  private Map<String, String> nodesRelatedAbstractNode = new HashMap<>();
  //Key - node template id, Value - related node template id in the substitution service template
  private Map<String, String> nodesRelatedSubstitutionServiceTemplateNode = new HashMap<>();
  private Map<String, NodeTemplateInformation> cleanedNodeTemplates = new HashMap<>();
  //Key - nested node template id, Value - related unified nested node template id
  private Map<String, String> nestedNodeTemplateRelatedUnifiedTranslatedId = new HashMap<>();
  //Key - nested node type id, Value - related unified nested node template id
  private Map<String, String> nestedNodeTypeRelatedUnifiedTranslatedId = new HashMap<>();
  //Key - handled compute type, Value - number of times it was handled
  private Map<String, Integer> handledComputeTypesInNestedSubstitutionTemplate =
      new HashMap<>();
  //Key - nested compute type, Value - list of nested files that the compute type is present
  private Map<String, Integer> handledNestedComputeTypesNestedFiles = new HashMap<>();
  //Key - new property id, Value - orig property value
  private Map<String, Object> newParameterIdsToPropertiesFromOrigNodeTemplate = new HashMap<>();
  //handled nested files
  private Set<String> handledNestedFiles = new HashSet<>();
  //handled nested nodes
  private Set<String> handledNestedNodes = new HashSet<>();

  public Map<String, String> getNodesRelatedAbstractNode() {
    return nodesRelatedAbstractNode;
  }

  public void setNodesRelatedAbstractNode(
      Map<String, String> nodesRelatedAbstractNode) {
    this.nodesRelatedAbstractNode = nodesRelatedAbstractNode;
  }

  public void addHandledNestedNodes(String handledNestedNodeId) {
    this.handledNestedNodes.add(handledNestedNodeId);
  }

  public Map<String, String> getNodesRelatedSubstitutionServiceTemplateNode() {
    return nodesRelatedSubstitutionServiceTemplateNode;
  }

  public void setNodesRelatedSubstitutionServiceTemplateNode(
      Map<String, String> nodesRelatedSubstitutionServiceTemplateNode) {
    this.nodesRelatedSubstitutionServiceTemplateNode = nodesRelatedSubstitutionServiceTemplateNode;
  }

  public String getNodeRelatedAbstractNode(String origNodeId) {
    return this.nodesRelatedAbstractNode.get(origNodeId);
  }

  public Collection<String> getAllRelatedAbstractNodeIds() {
    return this.nodesRelatedAbstractNode.values();
  }

  public Collection<String> getAllUnifiedNestedNodeTemplateIds() {
    return this.nestedNodeTemplateRelatedUnifiedTranslatedId.values();
  }

  /**
   * Add cleaned node template.
   *
   * @param nodeTemplateId           the node template id
   * @param unifiedCompositionEntity the unified composition entity
   * @param nodeTemplate             the node template
   */
  public void addCleanedNodeTemplate(String nodeTemplateId,
                                     UnifiedCompositionEntity unifiedCompositionEntity,
                                     NodeTemplate nodeTemplate) {
    NodeTemplateInformation nodeTemplateInformation = new NodeTemplateInformation(
        unifiedCompositionEntity, nodeTemplate);
    this.cleanedNodeTemplates.putIfAbsent(nodeTemplateId, nodeTemplateInformation);
  }

  public NodeTemplate getCleanedNodeTemplate(String nodeTemplateId) {
    return this.cleanedNodeTemplates.get(nodeTemplateId).getNodeTemplate().clone();
  }

  public UnifiedCompositionEntity getCleanedNodeTemplateCompositionEntity(String nodeTemplateId) {
    return this.cleanedNodeTemplates.get(nodeTemplateId).getUnifiedCompositionEntity();
  }

  public void addUnifiedNestedNodeTemplateId(String nestedNodeTemplateId,
                                             String unifiedNestedNodeRelatedId) {
    this.nestedNodeTemplateRelatedUnifiedTranslatedId
        .put(nestedNodeTemplateId, unifiedNestedNodeRelatedId);
  }

  public Optional<String> getUnifiedNestedNodeTemplateId(String nestedNodeTemplateId) {
    return this.nestedNodeTemplateRelatedUnifiedTranslatedId.get(nestedNodeTemplateId) == null
        ? Optional.empty()
        : Optional.of(this.nestedNodeTemplateRelatedUnifiedTranslatedId.get(nestedNodeTemplateId));
  }

  public void addUnifiedNestedNodeTypeId(String nestedNodeTypeId,
                                         String unifiedNestedNodeRelatedId) {
    this.nestedNodeTypeRelatedUnifiedTranslatedId.put(nestedNodeTypeId, unifiedNestedNodeRelatedId);
  }

  public Optional<String> getUnifiedNestedNodeTypeId(String nestedNodeTypeId) {
    return this.nestedNodeTypeRelatedUnifiedTranslatedId.get(nestedNodeTypeId) == null ? Optional
        .empty()
        : Optional.of(this.nestedNodeTypeRelatedUnifiedTranslatedId.get(nestedNodeTypeId));
  }

  public Set<String> getAllRelatedNestedNodeTypeIds(){
    if(MapUtils.isEmpty(nestedNodeTypeRelatedUnifiedTranslatedId)){
      return new HashSet<>();
    }
    return new HashSet<>(this.nestedNodeTypeRelatedUnifiedTranslatedId.values());
  }

  public void addHandledComputeType(String handledComputeType) {

    if (this.handledComputeTypesInNestedSubstitutionTemplate.containsKey(handledComputeType)) {
      Integer timesHandled =
          this.handledComputeTypesInNestedSubstitutionTemplate.get(handledComputeType);
      this.handledComputeTypesInNestedSubstitutionTemplate
          .put(handledComputeType, timesHandled + 1);
    } else {
      //this.handledNestedFiles.add(nestedServiceTemplateFileName);
      handledComputeTypesInNestedSubstitutionTemplate.put(handledComputeType, 0);
    }
  }

  public boolean isComputeTypeHandledInServiceTemplate(String computeType) {
    return this.handledComputeTypesInNestedSubstitutionTemplate.containsKey(computeType);
  }

  public int getHandledNestedComputeNodeTemplateIndex(String computeType) {
    return this.handledComputeTypesInNestedSubstitutionTemplate.containsKey(computeType) ?
        this.handledComputeTypesInNestedSubstitutionTemplate.get(computeType) : 0;
  }

  public void addHandlesNestedServiceTemplate(String nestedServiceTemplateFileName){
    this.handledNestedFiles.add(nestedServiceTemplateFileName);
  }

  public boolean isNestedServiceTemplateWasHandled(String nestedServiceTemplateFileName) {
    return this.handledNestedFiles.contains(nestedServiceTemplateFileName);
  }

  public void updateUsedTimesForNestedComputeNodeType(String computeType) {
    this.handledNestedComputeTypesNestedFiles.putIfAbsent(computeType, 0);

    Integer usedNumber = this.handledNestedComputeTypesNestedFiles.get(computeType);
    this.handledNestedComputeTypesNestedFiles.put(computeType, usedNumber + 1);

  }

  public int getGlobalNodeTypeIndex(String computeType) {
    return Objects.isNull(this.handledNestedComputeTypesNestedFiles.get(computeType))
        || this.handledNestedComputeTypesNestedFiles.get(computeType)== 0 ? 0
        : this.handledNestedComputeTypesNestedFiles.get(computeType);
  }

  public boolean isNestedNodeWasHandled(String nestedNodeId) {
    return this.handledNestedNodes.contains(nestedNodeId);
  }


  public Map<String, Object> getAllNewPropertyInputParamIds(){
    return this.newParameterIdsToPropertiesFromOrigNodeTemplate;
  }

  public void addNewPropertyIdToNodeTemplate(String newPropertyId,
                                             Object origPropertyValue){
    if(!newParameterIdsToPropertiesFromOrigNodeTemplate.containsKey(newPropertyId)) {
      newParameterIdsToPropertiesFromOrigNodeTemplate.put(newPropertyId, origPropertyValue);
    }
  }

  public Optional<Object> getNewPropertyInputParam(String newPropertyId){
    if(!newParameterIdsToPropertiesFromOrigNodeTemplate.containsKey(newPropertyId)){
      return Optional.empty();
    }

    return Optional.of(newParameterIdsToPropertiesFromOrigNodeTemplate.get(newPropertyId));
  }

}
