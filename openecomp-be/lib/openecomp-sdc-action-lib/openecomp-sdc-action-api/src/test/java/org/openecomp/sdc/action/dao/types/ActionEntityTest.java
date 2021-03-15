/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;


public class ActionEntityTest {

  @Test
  public void TestSetVendorList() {
    ActionEntity entity = new ActionEntity();

    entity.setVendorList(null);
    assertEquals(null, entity.getVendorList());

    Set<String> stringSet = new HashSet<>();
    entity.setVendorList(stringSet);
    assertEquals(stringSet, entity.getVendorList());

    stringSet.add("TesT");
    entity.setVendorList(stringSet);
    assertEquals("test", entity.getVendorList().toArray()[0]);
  }

  @Test
  public void TestSetCategoryList() {
    ActionEntity entity = new ActionEntity();

    entity.setCategoryList(null);
    assertEquals(null, entity.getCategoryList());

    Set<String> stringSet = new HashSet<>();
    entity.setCategoryList(stringSet);
    assertEquals(stringSet, entity.getCategoryList());

    stringSet.add("TesT");
    entity.setCategoryList(stringSet);
    assertEquals("test", entity.getCategoryList().toArray()[0]);
  }
}