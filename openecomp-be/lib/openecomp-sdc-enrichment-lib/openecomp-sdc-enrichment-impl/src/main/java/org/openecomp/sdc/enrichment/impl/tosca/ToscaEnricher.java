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

package org.openecomp.sdc.enrichment.impl.tosca;

import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToscaEnricher extends Enricher {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  @Override
  public Map<String, List<ErrorMessage>> enrich() {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    errors.putAll(enrichAbstractSubstitute());
    errors.putAll(enrichPortMirroring());

    return errors;
  }

  private Map<String, List<ErrorMessage>> enrichAbstractSubstitute() {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, List<ErrorMessage>> enrichErrors = new HashMap<>();

    ToscaServiceModel toscaModel = (ToscaServiceModel) model;
    AbstractSubstituteToscaEnricher abstractSubstituteToscaEnricher =
        new AbstractSubstituteToscaEnricher();

    try {
      enrichErrors = abstractSubstituteToscaEnricher.enrich(toscaModel, data.getKey(),
          data.getVersion());
    }catch (Exception e){
      enrichErrors.put("Tosca Enrich", Arrays.asList(new ErrorMessage(ErrorLevel.ERROR, e
          .getMessage())));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return enrichErrors;
  }

  private Map<String, List<ErrorMessage>> enrichPortMirroring() {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    Map<String, List<ErrorMessage>> enrichErrors = new HashMap<>();
    ToscaServiceModel toscaModel = (ToscaServiceModel) model;
    PortMirroringEnricher portMirroringEnricher = new PortMirroringEnricher();

    try {
      enrichErrors = portMirroringEnricher.enrich(toscaModel);
    }catch (Exception e){
      enrichErrors.put("Tosca Enrich", Arrays.asList(new ErrorMessage(ErrorLevel.ERROR, e
          .getMessage())));
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
    return enrichErrors;
  }
}
