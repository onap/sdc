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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

import java.util.Optional;

public class ResourceTranslationDefaultImpl extends ResourceTranslationBase {

  @Override
  public void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    //no translation is needed, this default is used for unsupported resources
    logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
        + translateTo.getResource().getType()
        + "' is not supported, will be ignored in TOSCA translation");

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {
    return null;
  }

  @Override
  protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(
      TranslateTo translateTo) {
    return Optional.empty();
  }

}
