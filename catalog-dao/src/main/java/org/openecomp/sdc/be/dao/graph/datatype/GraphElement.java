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
package org.openecomp.sdc.be.dao.graph.datatype;

import java.util.Map;

public abstract class GraphElement {

    private GraphElementTypeEnum elementType;
    private ActionEnum action;

    protected GraphElement(GraphElementTypeEnum elementType) {
        this.elementType = elementType;
        this.action = ActionEnum.NoAction;
    }

    public GraphElementTypeEnum getElementType() {
        return elementType;
    }

    public void setElementType(GraphElementTypeEnum elementType) {
        this.elementType = elementType;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public abstract Map<String, Object> toGraphMap();

    @Override
    public String toString() {
        return "GraphElement [elementType=" + elementType + ", action=" + action + "]";
    }
}
