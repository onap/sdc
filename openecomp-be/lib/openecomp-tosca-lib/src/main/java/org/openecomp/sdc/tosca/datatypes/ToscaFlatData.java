/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.tosca.datatypes;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class ToscaFlatData {

    private Object flatEntity;
    private ToscaElementTypes elementType;
    private List<String> inheritanceHierarchyType;

    public ToscaElementTypes getElementType() {
        return elementType;
    }

    public void setElementType(ToscaElementTypes elementType) {
        this.elementType = elementType;
    }

    public Object getFlatEntity() {
        return flatEntity;
    }

    public void setFlatEntity(Object flatEntity) {
        this.flatEntity = flatEntity;
    }

    public List<String> getInheritanceHierarchyType() {
        return inheritanceHierarchyType;
    }

    public void addInheritanceHierarchyType(String inheritedType) {
        if (CollectionUtils.isEmpty(inheritanceHierarchyType)) {
            inheritanceHierarchyType = new ArrayList<>();
        }
        this.inheritanceHierarchyType.add(inheritedType);
    }
}
