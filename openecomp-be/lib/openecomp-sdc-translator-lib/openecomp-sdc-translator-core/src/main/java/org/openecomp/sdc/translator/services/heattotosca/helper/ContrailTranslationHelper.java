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

package org.openecomp.sdc.translator.services.heattotosca.helper;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.translator.datatypes.heattotosca.PropertyRegexMatcher;
import org.openecomp.sdc.translator.services.heattotosca.ConfigConstants;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContrailTranslationHelper {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  /**
   * Gets compute node type id.
   *
   * @param contrailServiceTemplateResource     contrail service teamplte resource
   * @param contrailServiceTemplateResourceId   contrailservice template resource id
   * @param contrailServiceTemplateTranslatedId contrail service tempalte resource translated id
   * @return the compute node type id
   */
  public String getComputeNodeTypeId(Resource contrailServiceTemplateResource,
                                     String contrailServiceTemplateResourceId,
                                     String contrailServiceTemplateTranslatedId,
                                     TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    NameExtractor nodeTypeNameExtractor =
        context.getNameExtractorImpl(ConfigConstants.CONTRAIL_COMPUTE_NODE_TYPE_IMPL_KEY);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return nodeTypeNameExtractor
        .extractNodeTypeName(contrailServiceTemplateResource, contrailServiceTemplateResourceId,
            contrailServiceTemplateTranslatedId);
  }

  /**
   * Get property Regx matcher list.
   *
   * @return Regex exprission per contrail service template resource property, while contail compute
   *         type name is consider when setting the name value
   */
  public List<PropertyRegexMatcher> getPropertyRegexMatchersForComputeNodeType() {
    List<PropertyRegexMatcher> propertyRegexMatchers = new ArrayList<>();
    propertyRegexMatchers
        .add(new PropertyRegexMatcher("image_name", Collections.singletonList(".+_image_name$"),
            "_image_name"));
    propertyRegexMatchers
        .add(new PropertyRegexMatcher("flavor", Collections.singletonList(".+_flavor_name$"),
            "_flavor_name"));
    return propertyRegexMatchers;
  }

  public String getSubstitutionContrailServiceTemplateMetadata(String heatFileName,
                                                               String serviceInstanceTranslatedId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return FileUtils.getFileWithoutExtention(heatFileName) + "_" + serviceInstanceTranslatedId;
  }

  /**
   * Translate fn split function optional.
   *
   * @param propertyValue       the property value
   * @param listSize            the list size
   * @param includeBooleanValue the include boolean value
   * @return the optional
   */
  public Optional<List<Map<String, List>>> translateFnSplitFunction(Object propertyValue,
                                                                    int listSize,
                                                                    boolean
                                                                        includeBooleanValue) {
    List<Map<String, List>> tokenPropertyValueList = new ArrayList<>();

    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map<String, Object> propMap = (Map) propertyValue;
      Map.Entry<String, Object> entry = propMap.entrySet().iterator().next();
      Object entity = entry.getValue();
      String key = entry.getKey();
      String tokenChar;

      if (key.equals("Fn::Split") && entity instanceof List) {
        tokenChar = (String) ((List) entity).get(0);
        Object refParameter = ((List) entity).get(1);

        for (int substringIndex = 0; substringIndex < listSize; substringIndex++) {
          Map<String, List> tokenPropertyValue = new HashMap<>();
          tokenPropertyValue.put("token", new ArrayList<>());

          if (refParameter instanceof Map && ((Map) refParameter).get("Ref") != null) {
            Map<String, String> stringWithToken = new HashMap<>();
            ((Map) stringWithToken)
                .put(ToscaFunctions.GET_INPUT.getDisplayName(), ((Map) refParameter).get("Ref"));
            tokenPropertyValue.get("token").add(stringWithToken);
          } else if (refParameter instanceof String) {
            if (includeBooleanValue) {
              StringBuffer booleanBuffer = new StringBuffer();
              String[] booleanValueList = ((String) refParameter).split(tokenChar);
              for (int i = 0; i < booleanValueList.length; i++) {
                if (i == 0) {
                  booleanBuffer.append(HeatBoolean.eval(booleanValueList[i]));
                } else {
                  booleanBuffer.append(tokenChar);
                  booleanBuffer.append(HeatBoolean.eval(booleanValueList[i]));
                }
              }
              tokenPropertyValue.get("token").add(booleanBuffer.toString());
            } else {
              tokenPropertyValue.get("token").add(refParameter);
            }
          }
          tokenPropertyValue.get("token").add(tokenChar);
          tokenPropertyValue.get("token").add(substringIndex);
          tokenPropertyValueList.add(tokenPropertyValue);
        }

        return Optional.of(tokenPropertyValueList);

      }
    }

    return Optional.empty();
  }
}
