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

package org.openecomp.sdc.translator.datatypes.heattotosca.to;


import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;

public class TranslateTo {
  private String heatFileName;
  private ServiceTemplate serviceTemplate;
  private HeatOrchestrationTemplate heatOrchestrationTemplate;
  private Resource resource;
  private String resourceId;
  private String translatedId;
  private TranslationContext context;

  public TranslateTo() {
  }

  /**
   * Instantiates a new Translate to.
   *
   * @param heatFileName              the heat file name
   * @param serviceTemplate           the service template
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resource                  the resource
   * @param resourceId                the resource id
   * @param translatedId              the translated id
   * @param context                   the context
   */
  public TranslateTo(String heatFileName, ServiceTemplate serviceTemplate,
                     HeatOrchestrationTemplate heatOrchestrationTemplate, Resource resource,
                     String resourceId, String translatedId, TranslationContext context) {
    this.heatFileName = heatFileName;
    this.serviceTemplate = serviceTemplate;
    this.heatOrchestrationTemplate = heatOrchestrationTemplate;
    this.resource = resource;
    this.resourceId = resourceId;
    this.translatedId = translatedId;
    this.context = context;
  }

  public String getHeatFileName() {
    return heatFileName;
  }

  public void setHeatFileName(String heatFileName) {
    this.heatFileName = heatFileName;
  }

  public ServiceTemplate getServiceTemplate() {
    return serviceTemplate;
  }

  public void setServiceTemplate(ServiceTemplate serviceTemplate) {
    this.serviceTemplate = serviceTemplate;
  }

  public HeatOrchestrationTemplate getHeatOrchestrationTemplate() {
    return heatOrchestrationTemplate;
  }

  public void setHeatOrchestrationTemplate(HeatOrchestrationTemplate heatOrchestrationTemplate) {
    this.heatOrchestrationTemplate = heatOrchestrationTemplate;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getTranslatedId() {
    return translatedId;
  }

  public void setTranslatedId(String translatedId) {
    this.translatedId = translatedId;
  }

  public TranslationContext getContext() {
    return context;
  }

  public void setContext(TranslationContext context) {
    this.context = context;
  }

  @Override
  public String toString() {
    return "TranslateTo{"
        + "heatFileName='" + heatFileName + '\''
        + ", serviceTemplate=" + serviceTemplate
        + ", heatOrchestrationTemplate=" + heatOrchestrationTemplate
        + ", resource=" + resource
        + ", resourceId='" + resourceId + '\''
        + ", translatedId='" + translatedId + '\''
        + ", context=" + context
        + '}';
  }
}
