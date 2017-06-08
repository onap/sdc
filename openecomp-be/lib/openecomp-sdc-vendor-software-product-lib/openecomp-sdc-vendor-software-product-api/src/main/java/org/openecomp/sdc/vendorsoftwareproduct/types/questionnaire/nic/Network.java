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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic;

/**
 * Created by TALIO on 11/22/2016
 */
public class Network {
    private String networkDescription;

    public String getNetworkDescription() {
        return networkDescription;
    }

    public void setNetworkDescription(String networkDescription) {
        this.networkDescription = networkDescription;
    }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Network{");
    sb.append("networkDescription='").append(networkDescription).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
