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

package org.openecomp.sdcrests.wrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a generic collection wrapper to be used by paginated results.
 *
 * @param <T> the type parameter
 */
public class GenericCollectionWrapper<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  private List<T> results;
  private int listCount;

  /**
   * Instantiates a new Generic collection wrapper.
   */
  public GenericCollectionWrapper() {
    this.results = new ArrayList<>();
  }

  /**
   * Instantiates a new Generic collection wrapper.
   *
   * @param list      the list
   * @param listCount the list count
   */
  public GenericCollectionWrapper(List<T> list, int listCount) {
    if (!list.isEmpty()) {
      this.results = list;
      this.listCount = listCount;
    }
  }

  /**
   * Gets results.
   *
   * @return the results
   */
  public List<T> getResults() {
    return results;
  }

  /**
   * Sets results.
   *
   * @param results the results
   */
  public void setResults(List<T> results) {
    this.results = results;
  }

  /**
   * Gets list count.
   *
   * @return the list count
   */
  public int getListCount() {
    return listCount;
  }

  /**
   * Sets list count.
   *
   * @param listCount the list count
   */
  public void setListCount(int listCount) {
    this.listCount = listCount;
  }

  /**
   * Add boolean.
   *
   * @param e0 the e 0
   * @return the boolean
   */
  public boolean add(T e0) {
    if (this.getResults().add(e0)) {
      this.setListCount(this.getResults().size());
      return true;
    }
    return false;
  }
}
