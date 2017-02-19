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

package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.Collection;

public class NicCompositionSchemaInput implements SchemaTemplateInput {
  private boolean manual;
  private Nic nic;
  private Collection<String> networkIds;

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  public Nic getNic() {
    return nic;
  }

  public void setNic(Nic nic) {
    this.nic = nic;
  }

  public Collection<String> getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(Collection<String> networkIds) {
    this.networkIds = networkIds;
  }
}
