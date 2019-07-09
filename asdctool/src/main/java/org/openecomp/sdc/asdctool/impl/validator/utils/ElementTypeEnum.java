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

package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.openecomp.sdc.asdctool.impl.validator.executers.VfValidatorExecuter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaya on 7/4/2017.
 */
public enum ElementTypeEnum {

    VF ("vf", VfValidatorExecuter.class);
    //SERVICE("service", ServiceValidatorExecuter.class)

    private String elementType;
    private Class clazz;

    ElementTypeEnum(String elementType, Class clazz) {
       this. elementType = elementType;
       this.clazz = clazz;
    }

    public static ElementTypeEnum getByType(String elementType){
        for(ElementTypeEnum currType :ElementTypeEnum.values()){
            if(currType.getElementType().equals(elementType)){
                return currType;
            }
        }
        return null;
    }

    public static List<String> getAllTypes() {

        List<String> arrayList = new ArrayList<String>();

        for (ElementTypeEnum graphType : ElementTypeEnum.values()) {
            arrayList.add(graphType.getElementType());
        }
        return arrayList;
    }


    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
