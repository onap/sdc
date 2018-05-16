/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Objects;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

public class NestedConsolidationDataHandler implements ConsolidationDataHandler {

    private final NestedConsolidationData nestedConsolidationData;

    public NestedConsolidationDataHandler(NestedConsolidationData nestedConsolidationData) {
        this.nestedConsolidationData = nestedConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                            RequirementAssignment requirementAssignment) {
        EntityConsolidationData entityConsolidationData =
                getNestedTemplateConsolidationData(translateTo, translateTo.getHeatFileName(),
                        translateTo.getTranslatedId());

        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);
        }
    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                           String dependentNodeTemplateId, String targetResourceId,
                                           String requirementId, RequirementAssignment requirementAssignment) {

        EntityConsolidationData entityConsolidationData =
                getNestedTemplateConsolidationData(translateTo, translateTo.getHeatFileName(), dependentNodeTemplateId);

        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedIn(sourceNodeTemplateId, requirementId, requirementAssignment);
        }

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       String paramName,
                                                       String contrailSharedResourceId,
                                                       String sharedTranslatedResourceId) {

        throw new UnsupportedOperationException(
                "API removeParamNameFromAttrFuncList not supported for NestedConsolidationDataHandler");

    }

    private NestedTemplateConsolidationData getNestedTemplateConsolidationData(TranslateTo translateTo,
                                                                                      String nestedHeatFileName,
                                                                                      String nestedNodeTemplateId) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        TranslationContext context = translateTo.getContext();
        return getNestedTemplateConsolidationData(serviceTemplate, context, nestedHeatFileName, nestedNodeTemplateId);
    }

    private NestedTemplateConsolidationData getNestedTemplateConsolidationData(
                                ServiceTemplate serviceTemplate,  TranslationContext context,
                                String nestedHeatFileName, String nestedNodeTemplateId) {


        if (isNestedResourceIdOccursInDifferentNestedFiles(context, nestedHeatFileName,
                nestedNodeTemplateId)) {
            throw new CoreException(new
                                            DuplicateResourceIdsInDifferentFilesErrorBuilder(
                                                    nestedNodeTemplateId).build());
        }

        if (isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(
                nestedNodeTemplateId, nestedHeatFileName, context)) {
            return null;
        }

        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FileNestedConsolidationData fileNestedConsolidationData = nestedConsolidationData
                                               .getFileNestedConsolidationData(serviceTemplateFileName);

        if (fileNestedConsolidationData == null) {
            fileNestedConsolidationData = new FileNestedConsolidationData();
            nestedConsolidationData.setFileNestedConsolidationData(serviceTemplateFileName,
                    fileNestedConsolidationData);
        }

        NestedTemplateConsolidationData nestedTemplateConsolidationData =
                fileNestedConsolidationData.getNestedTemplateConsolidationData(nestedNodeTemplateId);
        if (nestedTemplateConsolidationData == null) {
            nestedTemplateConsolidationData = new NestedTemplateConsolidationData();
            nestedTemplateConsolidationData.setNodeTemplateId(nestedNodeTemplateId);
            fileNestedConsolidationData.setNestedTemplateConsolidationData(nestedNodeTemplateId,
                    nestedTemplateConsolidationData);
        }

        return nestedTemplateConsolidationData;
    }

    private boolean isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(String nestedNodeTemplateId,
                                                                                     String nestedHeatFileName,
                                                                                     TranslationContext context) {

        return context.isServiceTemplateWithoutNodeTemplatesSection(
                FileUtils.getFileWithoutExtention(nestedHeatFileName))
                       || context.isNodeTemplateIdPointsToStWithoutNodeTemplates(nestedNodeTemplateId);
    }

    private boolean isNestedResourceIdOccursInDifferentNestedFiles(TranslationContext context,
                                                                          String nestedHeatFileName,
                                                                          String nestedNodeTemplateId) {
        return Objects.nonNull(nestedHeatFileName) && context.getAllTranslatedResourceIdsFromDiffNestedFiles(
                nestedHeatFileName).contains(nestedNodeTemplateId);
    }
}
