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

import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import java.util.Optional;

public class SubInterfaceTemplateConsolidationData extends EntityConsolidationData {

  //Value of the property count in the resource group representing the sub-interface
  private Object resourceGroupCount;
  //Network role of the sub-interface
  private String networkRole;
  //Parent port node template id
  private String parentPortNodeTemplateId;

  public Object getResourceGroupCount() {
    return resourceGroupCount;
  }

  public void setResourceGroupCount(Object resourceGroupCount) {
    this.resourceGroupCount = resourceGroupCount;
  }

  public String getNetworkRole() {
    return networkRole;
  }

  public void setNetworkRole(String networkRole) {
    this.networkRole = networkRole;
  }

  public String getParentPortNodeTemplateId() {
    return parentPortNodeTemplateId;
  }

  public void setParentPortNodeTemplateId(String parentPortNodeTemplateId) {
    this.parentPortNodeTemplateId = parentPortNodeTemplateId;
  }

  public Optional<PortTemplateConsolidationData> getParentPortTemplateConsolidationData(ServiceTemplate serviceTemplate,
                                                                                        TranslationContext context) {
    FilePortConsolidationData filePortConsolidationData = context.getConsolidationData().getPortConsolidationData()
        .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    PortTemplateConsolidationData portTemplateConsolidationData = null;
    if (filePortConsolidationData != null) {
      portTemplateConsolidationData = filePortConsolidationData
          .getPortTemplateConsolidationData(parentPortNodeTemplateId);
    }
    return Optional.ofNullable(portTemplateConsolidationData);
  }

  public Optional<String> getParentPortNetworkRole(ServiceTemplate serviceTemplate,
                                                                TranslationContext context) {
    Optional<PortTemplateConsolidationData> subInterfacePortTemplateConsolidationData =
        getParentPortTemplateConsolidationData(serviceTemplate, context);
    if (!subInterfacePortTemplateConsolidationData.isPresent()) {
      return Optional.empty();
    }
    return Optional.ofNullable(subInterfacePortTemplateConsolidationData.get().getNetworkRole());
  }

}
