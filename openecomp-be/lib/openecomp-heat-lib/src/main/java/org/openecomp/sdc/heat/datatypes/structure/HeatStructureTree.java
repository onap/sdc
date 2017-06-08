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

package org.openecomp.sdc.heat.datatypes.structure;


import org.codehaus.jackson.annotate.JsonProperty;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class HeatStructureTree implements Comparable<HeatStructureTree> {

  private String fileName;
  private FileData.Type type;
  private Boolean isBase;
  private HeatStructureTree env;
  private List<ErrorMessage> errors;
  private Set<HeatStructureTree> heat;
  private Set<HeatStructureTree> volume;
  private Set<HeatStructureTree> network;
  private Set<HeatStructureTree> nested;
  private Set<HeatStructureTree> other;
  private Set<Artifact> artifacts;

  public HeatStructureTree() {
  }

  ;

  public HeatStructureTree(String fileName, boolean isBase) {
    setBase(isBase);
    setFileName(fileName);
  }

  /**
   * Gets heat structure tree by name.
   *
   * @param filesSet the files set
   * @param filename the filename
   * @return the heat structure tree by name
   */
  public static HeatStructureTree getHeatStructureTreeByName(Set<HeatStructureTree> filesSet,
                                                             String filename) {
    for (HeatStructureTree heatStructureTree : filesSet) {
      if (heatStructureTree.getFileName().equals(filename)) {
        return heatStructureTree;
      }
    }

    return null;
  }

  public void setType(FileData.Type type) {
    this.type = type;
  }

  public Boolean getBase() {
    return isBase;
  }

  public void setBase(Boolean base) {
    isBase = base;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String file) {
    this.fileName = file;
  }

  @JsonProperty(value = "heat")
  public Set<HeatStructureTree> getHeat() {
    return heat;
  }

  public void setHeat(Set<HeatStructureTree> heat) {
    this.heat = heat;
  }

  public Set<HeatStructureTree> getNested() {
    return nested;
  }

  public void setNested(Set<HeatStructureTree> nested) {
    this.nested = nested;
  }

  public Set<Artifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(Set<Artifact> artifacts) {
    this.artifacts = artifacts;
  }

  /**
   * Add heat structure tree to nested heat list.
   *
   * @param heatStructureTree the heat structure tree
   */
  public void addHeatStructureTreeToNestedHeatList(HeatStructureTree heatStructureTree) {
    if (this.nested == null) {
      this.nested = new TreeSet<>();
    }
    if (!findItemInSetByName(this.nested, heatStructureTree)) {
      this.nested.add(heatStructureTree);
    }
  }

  /**
   * Add artifact to artifact list.
   *
   * @param artifact the artifact
   */
  public void addArtifactToArtifactList(Artifact artifact) {
    if (this.artifacts == null || this.artifacts.isEmpty()) {
      this.artifacts = new TreeSet<>();
    }
    this.artifacts.add(artifact);
  }

  public HeatStructureTree getEnv() {
    return env;
  }

  public void setEnv(HeatStructureTree env) {
    this.env = env;
  }

  public Set<HeatStructureTree> getVolume() {
    return volume;
  }

  public void setVolume(Set<HeatStructureTree> volume) {
    this.volume = volume;
  }

  public Set<HeatStructureTree> getNetwork() {
    return network;
  }

  public void setNetwork(Set<HeatStructureTree> network) {
    this.network = network;
  }

  /**
   * Add network to network list.
   *
   * @param heatStructureTree the heat structure tree
   */
  public void addNetworkToNetworkList(HeatStructureTree heatStructureTree) {
    if (this.network == null) {
      this.network = new TreeSet<>();
    }
    if (!findItemInSetByName(this.network, heatStructureTree)) {
      this.network.add(heatStructureTree);
    }
  }

  /**
   * Add volume file to volume list.
   *
   * @param heatStructureTree the heat structure tree
   */
  public void addVolumeFileToVolumeList(HeatStructureTree heatStructureTree) {
    if (this.volume == null) {
      this.volume = new TreeSet<>();
    }
    if (!findItemInSetByName(this.volume, heatStructureTree)) {
      this.volume.add(heatStructureTree);
    }
  }

  /**
   * Add heat to heat list.
   *
   * @param heat the heat
   */
  public void addHeatToHeatList(HeatStructureTree heat) {
    if (this.heat == null) {
      this.heat = new TreeSet<>();
    }

    this.heat.add(heat);
  }

  /**
   * Add other to other list.
   *
   * @param other the other
   */
  public void addOtherToOtherList(HeatStructureTree other) {
    if (this.other == null) {
      this.other = new TreeSet<>();
    }

    this.other.add(other);
  }

  /**
   * Find item in set by name boolean.
   *
   * @param searchSet the search set
   * @param toFind    the to find
   * @return the boolean
   */
  public boolean findItemInSetByName(Set<HeatStructureTree> searchSet, HeatStructureTree toFind) {
    for (HeatStructureTree heatStructureTree : searchSet) {
      if (heatStructureTree.getFileName().equals(toFind.getFileName())) {
        return true;
      }

    }

    return false;
  }

  /**
   * Remove from volume or network.
   *
   * @param fileNameToRemove the file name to remove
   * @param type             the type
   */
  public void removeFromVolumeOrNetwork(String fileNameToRemove, FileData.Type type) {
    Set<HeatStructureTree> volumeOrNetworkSet =
        type.equals(FileData.Type.HEAT_VOL) ? this.volume : this.network;
    HeatStructureTree toRemove = getHeatStructureTreeByName(volumeOrNetworkSet, fileNameToRemove);

    volumeOrNetworkSet.remove(toRemove);
  }

  @Override
  public int hashCode() {
    int result1 = fileName != null ? fileName.hashCode() : 0;
    result1 = 31 * result1 + (env != null ? env.hashCode() : 0);
    result1 = 31 * result1 + (heat != null ? heat.hashCode() : 0);
    result1 = 31 * result1 + (volume != null ? volume.hashCode() : 0);
    result1 = 31 * result1 + (network != null ? network.hashCode() : 0);
    result1 = 31 * result1 + (artifacts != null ? artifacts.hashCode() : 0);
    result1 = 31 * result1 + (nested != null ? nested.hashCode() : 0);
    result1 = 31 * result1 + (errors != null ? errors.hashCode() : 0);


    return result1;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    HeatStructureTree heatStructureTree = (HeatStructureTree) other;

    if (fileName != null ? !fileName.equals(heatStructureTree.fileName)
        : heatStructureTree.fileName != null) {
      return false;
    }
    if (env != null ? !env.equals(heatStructureTree.env) : heatStructureTree.env != null) {
      return false;
    }
    if (heat != null ? !heat.equals(heatStructureTree.heat) : heatStructureTree.heat != null) {
      return false;
    }
    if (volume != null ? !volume.equals(heatStructureTree.volume)
        : heatStructureTree.volume != null) {
      return false;
    }
    if (network != null ? !network.equals(heatStructureTree.network)
        : heatStructureTree.network != null) {
      return false;
    }
    if (artifacts != null ? !artifacts.equals(heatStructureTree.artifacts)
        : heatStructureTree.artifacts != null) {
      return false;
    }
    if (nested != null ? !nested.equals(heatStructureTree.nested)
        : heatStructureTree.nested != null) {
      return false;
    }
    if (errors != null ? !errors.equals(heatStructureTree.errors)
        : heatStructureTree.errors != null) {
      return false;
    }

    return true;
  }

  public List<ErrorMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  /**
   * Add error to errors list.
   *
   * @param error the error
   */
  public void addErrorToErrorsList(ErrorMessage error) {
    if (this.errors == null || this.errors.isEmpty()) {
      this.errors = new ArrayList<>();
    }
    if (!this.errors.contains(error)) {
      this.errors.add(error);
    }
  }

  public Set<HeatStructureTree> getOther() {
    return other;
  }

  public void setOther(Set<HeatStructureTree> other) {
    this.other = other;
  }

  @Override
  public int compareTo(HeatStructureTree obj) {
    return obj.getFileName().compareTo(this.getFileName());
  }
}
