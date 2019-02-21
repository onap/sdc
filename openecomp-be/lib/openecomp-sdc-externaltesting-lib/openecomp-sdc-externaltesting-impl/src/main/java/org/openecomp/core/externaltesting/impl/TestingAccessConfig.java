/*
 * Copyright © 2019 iconectiv
 *
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
 */

package org.openecomp.core.externaltesting.impl;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TestingAccessConfig {

  private ClientConfiguration client;
  private List<RemoteTestingEndpointDefinition> endpoints;

  public List<RemoteTestingEndpointDefinition> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<RemoteTestingEndpointDefinition> endpoints) {
    this.endpoints = endpoints;
  }

  public ClientConfiguration getClient() {
    return client;
  }

  public void setClient(ClientConfiguration client) {
    this.client = client;
  }
}
