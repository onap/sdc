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

package org.openecomp.sdc.action.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.action.types.OpenEcompComponent;

@Table(keyspace = "dox", name = "EcompComponent")
public class OpenEcompComponentEntity {

  @PartitionKey
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  private String name;

  public OpenEcompComponentEntity() {
  }

  public OpenEcompComponentEntity(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * To dto OPENECOMP component.
   *
   * @return the OPENECOMP component
   */
  public OpenEcompComponent toDto() {
    OpenEcompComponent destination = new OpenEcompComponent();
    destination.setId(this.getId());
    destination.setName(this.getName());
    return destination;
  }
}
