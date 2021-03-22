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
package org.openecomp.sdcrests.action.types;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.action.types.OpenEcompComponent;

/**
 * Defines DTO used for creating Response with list of {@link ActionResponseDto } or list of {@link openEcompComponent }.
 */
@Getter
@Setter
public class ListResponseWrapper {

    List<ActionResponseDto> actionList;
    List<OpenEcompComponent> componentList;
    List<ActionResponseDto> versions;

    public ListResponseWrapper() {
        this.actionList = new ArrayList<>();
    }

    public void add(ActionResponseDto exception) {
        this.actionList.add(exception);
    }
}
