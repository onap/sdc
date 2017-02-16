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

package org.openecomp.sdc.tosca.datatypes.model;

public class Import {

  private String file;
  private String repository;
  private String namespace_uri;
  private String namespace_prefix;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getNamespace_uri() {
    return namespace_uri;
  }

  public void setNamespace_uri(String namespaceUri) {
    this.namespace_uri = namespaceUri;
  }

  public String getNamespace_prefix() {
    return namespace_prefix;
  }

  public void setNamespace_prefix(String namespacePrefix) {
    this.namespace_prefix = namespacePrefix;
  }
}
