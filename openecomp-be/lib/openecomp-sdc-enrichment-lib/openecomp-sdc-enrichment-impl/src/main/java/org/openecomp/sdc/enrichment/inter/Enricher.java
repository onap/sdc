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

package org.openecomp.sdc.enrichment.inter;


import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.datatypes.model.AsdcModel;
import org.openecomp.sdc.enrichment.EnrichmentInfo;

import java.util.List;
import java.util.Map;

public abstract class Enricher {


    protected EnrichmentInfo data;
    protected AsdcModel model;

    public void setData(EnrichmentInfo data) {
        this.data = data;
    }

    public void setModel(AsdcModel model) {
        this.model = model;
    }

    public abstract Map<String, List<ErrorMessage>> enrich();
}
