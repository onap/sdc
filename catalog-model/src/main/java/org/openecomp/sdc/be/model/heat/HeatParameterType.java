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

package org.openecomp.sdc.be.model.heat;

import lombok.Getter;
import org.openecomp.sdc.be.model.tosca.converters.HeatBooleanConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatCommaDelimitedListConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatJsonConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatNumberConverter;
import org.openecomp.sdc.be.model.tosca.converters.HeatStringConverter;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.HeatBooleanValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatCommaDelimitedListValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatNumberValidator;
import org.openecomp.sdc.be.model.tosca.validators.HeatStringValidator;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;

@Getter
public enum HeatParameterType {

    STRING("string", HeatStringValidator.getInstance(), HeatStringConverter.getInstance()),

    BOOLEAN("boolean", HeatBooleanValidator.getInstance(), HeatBooleanConverter.getInstance()),

    NUMBER("number", HeatNumberValidator.getInstance(), HeatNumberConverter.getInstance()),

    JSON("json", HeatStringValidator.getInstance(), HeatJsonConverter.getInstance()),

    COMMA_DELIMITED_LIST("comma_delimited_list", HeatCommaDelimitedListValidator.getInstance(),
            HeatCommaDelimitedListConverter.getInstance()),

    SCALAR_UNIT_SIZE("scalar-unit.size", HeatStringValidator.getInstance(), HeatStringConverter.getInstance()),
    SCALAR_UNIT_TIME("scalar-unit.time", HeatStringValidator.getInstance(), HeatStringConverter.getInstance()),
    SCALAR_UNIT_FREQUENCY("scalar-unit.frequency", HeatStringValidator.getInstance(), HeatStringConverter.getInstance());

    private String type;
    private PropertyTypeValidator validator;
    private PropertyValueConverter converter;

    HeatParameterType(final String type, final PropertyTypeValidator validator, final PropertyValueConverter converter) {
        this.type = type;
        this.validator = validator;
        this.converter = converter;
    }

    public static HeatParameterType isValidType(final String typeName) {
        if (typeName == null) {
            return null;
        }

        for (final HeatParameterType type : HeatParameterType.values()) {
            if (type.getType().equals(typeName)) {
                return type;
            }
        }
        return null;
    }
}
