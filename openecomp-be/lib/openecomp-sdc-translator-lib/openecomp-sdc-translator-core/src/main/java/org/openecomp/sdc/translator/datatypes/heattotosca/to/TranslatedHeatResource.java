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

package org.openecomp.sdc.translator.datatypes.heattotosca.to;

import org.openecomp.sdc.heat.datatypes.model.Resource;

public class TranslatedHeatResource {
  private String translatedId;
  private Resource heatResource;

  public TranslatedHeatResource(String translatedId, Resource heatResource) {
    this.translatedId = translatedId;
    this.heatResource = heatResource;
  }

  public String getTranslatedId() {
    return translatedId;
  }

  public void setTranslatedId(String translatedId) {
    this.translatedId = translatedId;
  }

  public Resource getHeatResource() {
    return heatResource;
  }

  public void setHeatResource(Resource heatResource) {
    this.heatResource = heatResource;
  }
}
