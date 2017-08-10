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

public class GenericCollectionWrapper<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  private transient List<T> results;
  private int listCount;

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

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public int getListCount() {
    return listCount;
  }

  public void setListCount(int listCount) {
    this.listCount = listCount;
  }

  /**
   * Add boolean.
   *
   * @param item the list item
   * @return the boolean
   */
  public boolean add(T item) {
    if (this.getResults().add(item)) {
      this.setListCount(this.getResults().size());
      return true;
    }
    return false;
  }
}
