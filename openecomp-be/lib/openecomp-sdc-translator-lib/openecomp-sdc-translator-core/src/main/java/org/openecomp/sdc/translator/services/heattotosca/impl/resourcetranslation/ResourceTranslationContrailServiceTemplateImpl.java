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
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailTranslationHelper;

public class ResourceTranslationContrailServiceTemplateImpl extends ResourceTranslationBase {

    private static final String IMAGE_NAME = "image_name";

    static String getContrailSubstitutedNodeTypeId(String serviceTemplateTranslatedId) {
        return ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + ToscaConstants.HEAT_NODE_TYPE_SUFFIX + serviceTemplateTranslatedId;
    }

    @Override
    public void translate(TranslateTo translateTo) {
        ServiceTemplate globalSubstitutionServiceTemplate = getGlobalSubstitutionTypesServiceTemplate(translateTo);
        addSubstitutedNodeType(translateTo, globalSubstitutionServiceTemplate);
        addComputeNodeType(translateTo, globalSubstitutionServiceTemplate, translateTo.getContext());
    }

    @Override
    protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
        Map<String, Object> properties = translateTo.getResource().getProperties();
        if (Objects.isNull(properties) || Objects.isNull(properties.get(IMAGE_NAME))) {
            throw new CoreException(new MissingMandatoryPropertyErrorBuilder(IMAGE_NAME).build());
        }
        return true;
    }

    private void addComputeNodeType(TranslateTo translateTo, ServiceTemplate globalSubstitutionServiceTemplate, TranslationContext context) {
        NodeType computeNodeType = new NodeType();
        computeNodeType.setDerived_from(ToscaNodeType.CONTRAIL_COMPUTE);
        String computeNodeTypeId = new ContrailTranslationHelper()
            .getComputeNodeTypeId(translateTo.getResource(), translateTo.getResourceId(), translateTo.getTranslatedId(), context);
        DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, computeNodeTypeId, computeNodeType);
    }

    private void addSubstitutedNodeType(TranslateTo translateTo, ServiceTemplate globalSubstitutionServiceTemplate) {
        NodeType substitutedNodeType = new NodeType();
        substitutedNodeType.setDerived_from(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE);
        DataModelUtil
            .addNodeType(globalSubstitutionServiceTemplate, getContrailSubstitutedNodeTypeId(translateTo.getTranslatedId()), substitutedNodeType);
    }

    @Override
    protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(TranslateTo translateTo) {
        return Optional.empty();
    }

    private ServiceTemplate getGlobalSubstitutionTypesServiceTemplate(TranslateTo translateTo) {
        ServiceTemplate globalSubstitutionServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        if (globalSubstitutionServiceTemplate == null) {
            globalSubstitutionServiceTemplate = new ServiceTemplate();
            Map<String, String> templateMetadata = new HashMap<>();
            templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
            globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
            globalSubstitutionServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
            globalSubstitutionServiceTemplate.setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
            translateTo.getContext().getTranslatedServiceTemplates()
                .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME, globalSubstitutionServiceTemplate);
        }
        return globalSubstitutionServiceTemplate;
    }
}
