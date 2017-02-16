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

import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.helper.impl.NameExtractorServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ContrailTranslationHelper {
  /**
   * Gets compute node type id.
   *
   * @param serviceTemplateTranslatedId the service template translated id
   * @param serviceTemplateResource     the service template resource
   * @return the compute node type id
   */
  public String getComputeNodeTypeId(String serviceTemplateTranslatedId,
                                     Resource serviceTemplateResource) {
    NameExtractorService nodeTypeNameExtractor = new NameExtractorServiceImpl();
    List<PropertyRegexMatcher> propertyRegexMatchers =
        getPropertiesAndRegexMatchers(nodeTypeNameExtractor);
    Optional<String> extractedNodeTypeName = nodeTypeNameExtractor
        .extractNodeTypeNameByPropertiesPriority(serviceTemplateResource.getProperties(),
            propertyRegexMatchers);

    return ToscaConstants.NODES_PREFIX
        + (extractedNodeTypeName.isPresent() ? extractedNodeTypeName.get()
            : "compute_" + serviceTemplateTranslatedId);
  }

  private List<PropertyRegexMatcher> getPropertiesAndRegexMatchers(
      NameExtractorService nodeTypeNameExtractor) {
    List<PropertyRegexMatcher> propertyRegexMatchers = new ArrayList<>();
    propertyRegexMatchers.add(nodeTypeNameExtractor
        .getPropertyRegexMatcher("image_name", Collections.singletonList(".+_image_name$"),
            "_image_name"));
    propertyRegexMatchers.add(nodeTypeNameExtractor
        .getPropertyRegexMatcher("flavor", Collections.singletonList(".+_flavor_name$"),
            "_flavor_name"));
    return propertyRegexMatchers;
  }
}
