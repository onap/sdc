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

package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.Map;

public class GetInputValueDataDefinition extends ToscaDataDefinition {

    private String propName;
    private String inputName;
    private String inputId;
    private Integer indexValue;
    private GetInputValueDataDefinition getInputIndex;

    private boolean isList = false;

    public GetInputValueDataDefinition() {
        super();
    }

    public GetInputValueDataDefinition(Map<String, Object> pr) {
        super(pr);
    }

    public GetInputValueDataDefinition(GetInputValueDataDefinition p) {
        this.setPropName(p.getPropName());
        this.setInputName(p.getInputName());
        this.setInputId(p.getInputId());
        this.setIndexValue(p.getIndexValue());
        this.setGetInputIndex(p.getGetInputIndex());
        this.setList(p.isList());


    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public Integer getIndexValue() {
        return indexValue;
    }

    public void setIndexValue(Integer indexValue) {
        this.indexValue = indexValue;
    }

    public GetInputValueDataDefinition getGetInputIndex() {
        return getInputIndex;
    }

    public void setGetInputIndex(GetInputValueDataDefinition getInputIndex) {
        this.getInputIndex = getInputIndex;
    }

    public boolean isList() {
        return isList;
    }

    public void setList(boolean isList) {
        this.isList = isList;
    }


    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    @Override
    public String toString() {
        return "GetInputValueDataDefinition [propName=" + propName + ", inputName=" + inputName + ", indexValue=" + indexValue + ", getInputIndex=" + getInputIndex + ", isList=" + isList + "]";
    }

    @Override
    public int hashCode() {
        final int hashingNumerList = 1231;
        final int hashingNumerNoList = 1237;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInputIndex == null) ? 0 : getInputIndex.hashCode());
        result = prime * result + ((indexValue == null) ? 0 : indexValue.hashCode());
        result = prime * result + ((inputName == null) ? 0 : inputName.hashCode());
        result = prime * result + (isList ? hashingNumerList : hashingNumerNoList);
        result = prime * result + ((propName == null) ? 0 : propName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetInputValueDataDefinition other = (GetInputValueDataDefinition) obj;
        if (getInputIndex == null) {
            if (other.getInputIndex != null) {
                return false;
            }
        } else if (!getInputIndex.equals(other.getInputIndex)) {
            return false;
        }
        if (indexValue == null) {
            if (other.indexValue != null) {
                return false;
            }
        } else if (!indexValue.equals(other.indexValue)) {
            return false;
        }
        if (inputName == null) {
            if (other.inputName != null) {
                return false;
            }
        } else if (!inputName.equals(other.inputName)) {
            return false;
        }
        if (isList != other.isList) {
            return false;
        }
        if (propName == null) {
            return other.propName == null;
        } else {
            return propName.equals(other.propName);
        }
    }


}
