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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@NoArgsConstructor
public class PropertyRule extends ToscaDataDefinition {

    private static final String FORCE_ALL = "FORCE_ALL";
    private static final String ALL = "ALL";
    private static final String RULE_ANY_MATCH = ".+";
    private List<String> rule;
    private String value;

    public PropertyRule(List<String> rule, String value) {
        super();
        this.rule = rule;
        this.value = value;
    }

    public static String getForceAll() {
        return FORCE_ALL;
    }

    public static String getALL() {
        return ALL;
    }

    public static String getRuleAnyMatch() {
        return RULE_ANY_MATCH;
    }

    @JsonIgnore
    public String getFirstToken() {
        return getToken(1);
    }

    public String getToken(int tokenNumber) {
        int index = tokenNumber - 1;
        if (rule == null || index >= rule.size() || index < 0) {
            return null;
        }
        return rule.get(index);
    }

    @JsonIgnore
    public int getRuleSize() {
        if (rule == null) {
            return 0;
        }
        return rule.size();
    }

    @Override
    public String toString() {
        return "PropertyRule [rule=" + rule + ", value=" + value + "]";
    }

    public boolean compareRule(PropertyRule comparedPropertyRule) {
        if (comparedPropertyRule == null) {
            return false;
        }
        List<String> comparedRule = comparedPropertyRule.getRule();
        if (rule == null && comparedRule == null) {
            return true;
        }
        if (rule != null && comparedRule != null) {
            if (rule.size() != comparedRule.size()) {
                return false;
            } else {
                int size = rule.size();
                boolean isEqual = true;
                for (int i = 0; i < size; i++) {
                    String item = rule.get(i);
                    String comparedItem = comparedRule.get(i);
                    if (item == null || !item.equals(comparedItem)) {
                        isEqual = false;
                        break;
                    }
                }
                return isEqual;
            }
        } else {
            return false;
        }
    }

    public void replaceFirstToken(String token) {
        if (rule != null && rule.size() > 0) {
            rule.set(0, token);
        }
    }
}
