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

package org.openecomp.sdc.enrichment.impl;

import org.openecomp.sdc.enrichment.impl.external.artifact.ExternalArtifactEnricher;
import org.openecomp.sdc.enrichment.impl.tosca.ToscaEnricher;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.enrichment.inter.EnricherHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EnricherHandlerImpl implements EnricherHandler {

    private static Logger logger = (Logger) LoggerFactory.getLogger(EnricherHandlerImpl.class);

    @Override
    public List<Enricher> getEnrichers() {
        List<Enricher> enricherList = new ArrayList<>();
        enricherList.add(new ToscaEnricher());
        enricherList.add(new ExternalArtifactEnricher());
        return enricherList;
    }

}
