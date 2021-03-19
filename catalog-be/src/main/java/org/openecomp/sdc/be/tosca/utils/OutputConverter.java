/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.tosca.AttributeConverter;
import org.openecomp.sdc.be.tosca.exception.ToscaConversionException;
import org.openecomp.sdc.be.tosca.model.ToscaAttribute;
import org.openecomp.sdc.be.tosca.model.ToscaOutput;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutputConverter {

    private final ObjectProvider<AttributeConverter> attributeConverterProvider;

    @Autowired
    public OutputConverter(final ObjectProvider<AttributeConverter> attributeConverterProvider) {
        this.attributeConverterProvider = attributeConverterProvider;
    }

    public Map<String, ToscaProperty> convert(final List<OutputDefinition> outputDefinitionList, final Map<String, DataTypeDefinition> dataTypes)
        throws ToscaConversionException {
        final AttributeConverter attributeConverter = this.attributeConverterProvider.getObject(dataTypes);
        final Map<String, ToscaProperty> outputMap = new HashMap<>();
        if (CollectionUtils.isEmpty(outputDefinitionList)) {
            return Collections.emptyMap();
        }
        for (final OutputDefinition outputDefinition : outputDefinitionList) {
            final ToscaAttribute toscaAttribute = attributeConverter.convert(outputDefinition);
            final ToscaProperty toscaProperty = new ToscaOutput(toscaAttribute);
            outputMap.put(outputDefinition.getName(), toscaProperty);
        }
        return outputMap;
    }
}
