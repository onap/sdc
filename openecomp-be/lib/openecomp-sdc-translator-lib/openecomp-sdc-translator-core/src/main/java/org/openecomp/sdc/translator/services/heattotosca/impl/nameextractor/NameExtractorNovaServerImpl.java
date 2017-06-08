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

package org.openecomp.sdc.translator.services.heattotosca.impl.nameextractor;

import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.translator.datatypes.heattotosca.PropertyRegexMatcher;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractorUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationNovaServerImpl;

import java.util.List;
import java.util.Optional;

public class NameExtractorNovaServerImpl implements NameExtractor {

  @Override
  public String extractNodeTypeName(Resource resource, String resourceId, String translatedId) {
    ResourceTranslationNovaServerImpl novaServerTranslator =
        new ResourceTranslationNovaServerImpl();
    List<PropertyRegexMatcher> propertyRegexMatchers =
        novaServerTranslator.getPropertyRegexMatchersForNovaNodeType();

    Optional<String> extractedNodeTypeName = NameExtractorUtil
        .extractNodeTypeNameByPropertiesPriority(resource.getProperties(), propertyRegexMatchers);

    return ToscaNodeType.VFC_NODE_TYPE_PREFIX + "heat."
        + (extractedNodeTypeName.isPresent() ? extractedNodeTypeName.get()
        : translatedId.replace(".", "_"));
  }

}
