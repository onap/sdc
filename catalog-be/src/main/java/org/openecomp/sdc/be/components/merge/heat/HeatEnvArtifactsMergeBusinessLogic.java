package org.openecomp.sdc.be.components.merge.heat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Created by chaya on 9/14/2017.
 */
@Component
public class HeatEnvArtifactsMergeBusinessLogic {

    public List<ArtifactDefinition> mergeInstanceHeatEnvArtifacts(List<ArtifactDefinition> origHeatEnvArtifacts, List<ArtifactDefinition> newHeatEnvArtifacts) {
        Map<String, ArtifactDefinition> origArtifactDefinitionByLabel = MapUtil.toMap(origHeatEnvArtifacts, ArtifactDefinition::getArtifactLabel);
        List<ArtifactDefinition> artifactsToUpdate = new ArrayList<>();
        newHeatEnvArtifacts.stream()
                .filter(heatEnvArtifact -> origArtifactDefinitionByLabel.containsKey(heatEnvArtifact.getArtifactLabel()))
                .forEach(heatEnvArtifact -> {
                    ArtifactDefinition origHeatEnvArtifact = origArtifactDefinitionByLabel.get(heatEnvArtifact.getArtifactLabel());
                    Boolean wasMergedHeatEnvArtifact = mergeHeatEnvArtifactsParameters(heatEnvArtifact, origHeatEnvArtifact);
                    if (wasMergedHeatEnvArtifact) {
                        artifactsToUpdate.add(heatEnvArtifact);
                    }
                });
        return artifactsToUpdate;
    }

    private Boolean mergeHeatEnvArtifactsParameters(ArtifactDefinition currArtifact, ArtifactDefinition origArtifact) {
        List<HeatParameterDefinition> currentHeatEnvParams = currArtifact.getListHeatParameters();
        List<HeatParameterDefinition> origHeatEnvParams = origArtifact.getListHeatParameters();
        boolean wasChanged = false;

        if (CollectionUtils.isEmpty(origHeatEnvParams) || CollectionUtils.isEmpty(currentHeatEnvParams)) {
            return false;
        }

        Map<String, HeatParameterDefinition> origHeatParametersByName = MapUtil.toMap(origHeatEnvParams, HeatParameterDefinition::getName);

        for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
            String paramName = currHeatParam.getName();
            HeatParameterDefinition origHeatParam = origHeatParametersByName.get(paramName);
            if (isSameHeatWithDiffValue(origHeatParam, currHeatParam)) {
                currHeatParam.setCurrentValue(origHeatParam.getCurrentValue());
                wasChanged = true;
            }
        }
        currArtifact.setListHeatParameters(currentHeatEnvParams);
        return wasChanged;
    }

    private boolean isSameHeatWithDiffValue(HeatParameterDefinition origHeatParam, HeatParameterDefinition newHeatParam) {
        return origHeatParam != null &&
               origHeatParam.getCurrentValue() != null &&
               origHeatParam.getType().equals(newHeatParam.getType()) &&
              !origHeatParam.getCurrentValue().equals(newHeatParam.getCurrentValue());
    }

}
