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

package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.enrichment.inter.ExternalArtifactEnricherInterface;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalArtifactEnricher extends Enricher {
  private MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static String EXTERNAL_ARTIFACT_ENRICH_CONF_FILE = "ExternalArtifactConfiguration.json";
  private static final String EXTERNAL_ARTIFACT_ENRICH_ERROR = "ERROR_CREATING_EXTERNAL_ARTIFACTS";
  private static final String EXTERNAL_ARTIFACT_ENRICH_ERROR_MSG =
      "An Error has occured during enrichment of external artifacts ";
  private static Collection<String> implementingClasses =
      getExternalArtifactEnrichedImplClassesList();
  private static Logger logger = LoggerFactory.getLogger(ExternalArtifactEnricher.class);

  private static Collection<String> getExternalArtifactEnrichedImplClassesList() {
    @SuppressWarnings("unchecked")
    Map<String, String> confFileAsMap = FileUtils.readViaInputStream(EXTERNAL_ARTIFACT_ENRICH_CONF_FILE,
        stream -> JsonUtil.json2Object(stream, Map.class));

    return confFileAsMap.values();
  }

  @Override
  public Map<String, List<ErrorMessage>> enrich() {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, List<ErrorMessage>> errors = new HashMap<>();

        try {
            for (String implementingClassName : implementingClasses) {
                ExternalArtifactEnricherInterface externalArtifactEnricherInstance = getExternalArtifactEnricherInstance(implementingClassName);
                externalArtifactEnricherInstance.enrich(this.data);
            }
        } catch (Exception e) {
            e.printStackTrace();
          logger.error(e.getMessage());
        }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return errors;
  }

  private ExternalArtifactEnricherInterface getExternalArtifactEnricherInstance(
      String implementingClassName) throws Exception {
    Class<?> clazz = Class.forName(implementingClassName);
    Constructor<?> constructor = clazz.getConstructor();
    return (ExternalArtifactEnricherInterface) constructor.newInstance();
  }
}
