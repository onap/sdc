/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GlobalTypesGenerator {

  private GlobalTypesGenerator() {
  }

  /**
   * Gets global types service template.
   *
   * @return the global types service template
   */
  public static Map<String, ServiceTemplate> getGlobalTypesServiceTemplate(OnboardingTypesEnum
                                                                               onboardingType) {
    return GlobalTypesServiceTemplates.getGlobalTypesServiceTemplates(onboardingType);
  }

  /**
   * Gets global types import list.
   *
   * @return the global types import list
   */
  public static List<Map<String, Import>> getGlobalTypesImportList() {
    List<Map<String, Import>> globalImports = new ArrayList<>();
    Map<String, Import> globalImportMap = new HashMap<>();
    Map<String, ServiceTemplate> globalTypesServiceTemplate =
        GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.ZIP);
    globalImportMap.put("openecomp_heat_index",
        HeatToToscaUtil.createServiceTemplateImport(globalTypesServiceTemplate.get
            ("openecomp-heat/_index.yml")));
    globalImports.add(globalImportMap);
    return globalImports;
  }

  /**
   * Gets global types import list for Manual Vsp onboarding.
   *
   * @return the global types import list
   */
  public static List<Map<String, Import>> getManualVspGlobalTypesImportList() {
    List<Map<String, Import>> globalImports = new ArrayList<>();
    Map<String, Import> globalImportMap = new HashMap<>();
    Map<String, ServiceTemplate> globalTypesServiceTemplate =
        GlobalTypesGenerator.getGlobalTypesServiceTemplate(OnboardingTypesEnum.MANUAL);
    globalImportMap.put("openecomp_index",
        HeatToToscaUtil.createServiceTemplateImport(globalTypesServiceTemplate.get
            ("openecomp/_index.yml")));
    globalImports.add(globalImportMap);
    return globalImports;
  }
}
