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

package org.openecomp.sdc.tosca.datatypes;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;

public class PropertyTypeTest {
  @Test
  public void shouldReturnNullWhenDisplayNameDoesNotExistForAnyProperty() {
    String s = "blabla";
    Assert.assertEquals(PropertyType.getPropertyTypeByDisplayName(s), null);
  }

  @Test
  public void shouldReturnApproppriatePropertyTypeWhenDisplayNameExist() {
    String s = "scalar-unit.size";
    Assert
        .assertEquals(PropertyType.getPropertyTypeByDisplayName(s), PropertyType.SCALAR_UNIT_SIZE);
  }
}
