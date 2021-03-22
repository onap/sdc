/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.item.rest.mapping;

import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdcrests.item.types.RevisionDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapRevisionToDto extends MappingBase<Revision, RevisionDto> {

    @Override
    public void doMapping(Revision source, RevisionDto target) {
        target.setId(source.getId());
        target.setMessage(source.getMessage());
        target.setUser(source.getUser());
        target.setTime(source.getTime());
    }
}
