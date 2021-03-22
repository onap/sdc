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
package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

public class FunctionTranslator {

    private static final String UNSUPPORTED_RESOURCE_PREFIX = "UNSUPPORTED_RESOURCE_";
    private static final String UNSUPPORTED_ATTRIBUTE_PREFIX = "UNSUPPORTED_ATTRIBUTE_";
    private ServiceTemplate serviceTemplate;
    private String resourceId;
    private String propertyName;
    private Object functionValue;
    private String heatFileName;
    private HeatOrchestrationTemplate heatOrchestrationTemplate;
    private Template toscaTemplate;
    private TranslationContext context;

    public FunctionTranslator() {
        //default constructor
    }

    public FunctionTranslator(TranslateTo functionTranslateTo, String propertyName, Object functionValue, Template toscaTemplate) {
        this.serviceTemplate = functionTranslateTo.getServiceTemplate();
        this.resourceId = functionTranslateTo.getResourceId();
        this.propertyName = propertyName;
        this.functionValue = functionValue;
        this.heatFileName = functionTranslateTo.getHeatFileName();
        this.heatOrchestrationTemplate = functionTranslateTo.getHeatOrchestrationTemplate();
        this.toscaTemplate = toscaTemplate;
        this.context = functionTranslateTo.getContext();
    }

    public static TranslateTo getFunctionTranslateTo(ServiceTemplate serviceTemplate, String resourceId, String heatFileName,
                                                     HeatOrchestrationTemplate heatOrchestrationTemplate, TranslationContext context) {
        return new TranslateTo(heatFileName, serviceTemplate, heatOrchestrationTemplate, null, resourceId, null, context);
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    Object getFunctionValue() {
        return functionValue;
    }

    void setFunctionValue(Object functionValue) {
        this.functionValue = functionValue;
    }

    public String getHeatFileName() {
        return heatFileName;
    }

    public HeatOrchestrationTemplate getHeatOrchestrationTemplate() {
        return heatOrchestrationTemplate;
    }

    Template getToscaTemplate() {
        return toscaTemplate;
    }

    public TranslationContext getContext() {
        return context;
    }

    String getUnsupportedResourcePrefix() {
        return UNSUPPORTED_RESOURCE_PREFIX;
    }

    String getUnsupportedAttributePrefix() {
        return UNSUPPORTED_ATTRIBUTE_PREFIX;
    }

    public boolean isResourceSupported(String translatedResourceId) {
        return !translatedResourceId.startsWith(UNSUPPORTED_RESOURCE_PREFIX);
    }

    boolean isAttributeSupported(String translatedAttName) {
        return !translatedAttName.startsWith(UNSUPPORTED_ATTRIBUTE_PREFIX);
    }
}
