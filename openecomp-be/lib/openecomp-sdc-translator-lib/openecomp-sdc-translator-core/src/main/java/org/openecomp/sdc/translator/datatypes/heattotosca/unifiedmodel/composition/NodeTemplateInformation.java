package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

/**
 * Created by Talio on 4/4/2017.
 */
public class NodeTemplateInformation {
  UnifiedCompositionEntity unifiedCompositionEntity;
  private NodeTemplate nodeTemplate;

  public NodeTemplateInformation(){}

  public NodeTemplateInformation(
      UnifiedCompositionEntity unifiedCompositionEntity,
      NodeTemplate nodeTemplate) {
    this.unifiedCompositionEntity = unifiedCompositionEntity;
    this.nodeTemplate = nodeTemplate;
  }

  public UnifiedCompositionEntity getUnifiedCompositionEntity() {
    return unifiedCompositionEntity;
  }

  public void setUnifiedCompositionEntity(
      UnifiedCompositionEntity unifiedCompositionEntity) {
    this.unifiedCompositionEntity = unifiedCompositionEntity;
  }

  public NodeTemplate getNodeTemplate() {
    return nodeTemplate;
  }

  public void setNodeTemplate(NodeTemplate nodeTemplate) {
    this.nodeTemplate = nodeTemplate;
  }
}
