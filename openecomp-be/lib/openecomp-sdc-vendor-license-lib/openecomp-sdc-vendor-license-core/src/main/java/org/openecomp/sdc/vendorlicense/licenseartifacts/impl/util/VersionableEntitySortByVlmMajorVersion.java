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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util;

import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.Comparator;

/**
 * @author katyr
 * @since January 10, 2017
 */

public class VersionableEntitySortByVlmMajorVersion implements Comparator<VersionableEntity> {
  @Override
  public int compare(VersionableEntity o1, VersionableEntity o2) {
    return Integer.compare(o1.getVersion().getMajor(), o2.getVersion().getMajor());

  }
}
