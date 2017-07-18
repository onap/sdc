package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import static org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil.isComputeResource;
import static org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil.isPortResource;
import static org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil.isVolumeResource;

/**
 * The enum Entity type.
 */
public enum ConsolidationEntityType {
  COMPUTE,
  PORT,
  VOLUME,
  NESTED,
  //Simple nested VFC (nested file with one compute) or a complex VFC (nested ST with more than
  //one compute)
  VFC_NESTED,
  SUB_PORT,
  OTHER;

  private ConsolidationEntityType sourceEntityType;
  private ConsolidationEntityType targetEntityType;

  public ConsolidationEntityType getSourceEntityType() {
    return sourceEntityType;
  }

  public ConsolidationEntityType getTargetEntityType() {
    return targetEntityType;
  }

  /**
   * Sets entity type.
   *
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param sourceResource          the source resource
   * @param targetResource          the target resource
   */
  public void setEntityType(HeatOrchestrationTemplate heatOrchestrationTemplate,
                            Resource sourceResource,
                            Resource targetResource,
                            TranslationContext context) {
    targetEntityType =
        getEntityType(heatOrchestrationTemplate, targetResource, context);
    sourceEntityType =
        getEntityType(heatOrchestrationTemplate, sourceResource, context);
  }

  private ConsolidationEntityType getEntityType(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                Resource resource, TranslationContext context) {
    if (isComputeResource(resource)) {
      return ConsolidationEntityType.COMPUTE;
    } else if (isPortResource(resource)) {
      return ConsolidationEntityType.PORT;
    } else if (isVolumeResource(resource)) {
      return ConsolidationEntityType.VOLUME;
    } else if (HeatToToscaUtil.isNestedResource(resource)) {
      if (HeatToToscaUtil.isNestedVfcResource(resource, context)) {
        return ConsolidationEntityType.VFC_NESTED;
      } else {
        return ConsolidationEntityType.NESTED;
      }
    } else {
      return ConsolidationEntityType.OTHER;
    }
  }
}