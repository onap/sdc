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

package org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Avrahamg
 * @since December 25, 2016
 */
public class AnalyzedZipHeatFiles {
  private Set<String> nestedFiles = new HashSet<>();
  private Set<String> otherNonModuleFiles = new HashSet<>();
  private Set<String> moduleFiles = new HashSet<>();

  public void addNestedFile(String fileName)
  {
    nestedFiles.add(fileName);
    moduleFiles.remove(fileName);
  }

  public void addNestedFiles(Collection<String> fileNames)
  {
    nestedFiles.addAll(fileNames);
    moduleFiles.removeAll(fileNames);
  }

  public void addOtherNonModuleFile(String fileName)
  {
    otherNonModuleFiles.add(fileName);
    moduleFiles.remove(fileName);
  }

  public void addOtherNonModuleFiles(Collection<String> fileNames)
  {
    otherNonModuleFiles.addAll(fileNames);
    moduleFiles.removeAll(fileNames);
  }

  public void addModuleFile(String fileName)
  {
    moduleFiles.add(fileName);
  }

  public Collection<String> getFilesNotEligbleForModules()
  {
    return CollectionUtils.union(this.getNestedFiles(), this.getOtherNonModuleFiles());
  }

  public Set<String> getNestedFiles() {
    return nestedFiles;
  }

  public Set<String> getOtherNonModuleFiles() {
    return otherNonModuleFiles;
  }

  public Set<String> getModuleFiles() {
    return moduleFiles;
  }
}
