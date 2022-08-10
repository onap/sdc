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

package org.openecomp.sdc.be.nodeFilter;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class BaseServiceFilterUtilsTest {

    protected Service service;
    protected PropertyFilterDataDefinition propertyFilterDataDefinition;
    protected PropertyFilterDataDefinition propertyFilterDataDefinition2;
    protected static final String CI_NAME = "AAAAAA";
    protected static final String A_PROP_NAME = "A_PROP";
    protected static final String SIZE_PROP = "size";

    @BeforeEach
    void initService() {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        service = new Service();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(CI_NAME);
        componentInstance.setName(CI_NAME);
        service.setComponentInstances(List.of(componentInstance));
        componentInstance.setDirectives(ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDirectives());
        CINodeFilterDataDefinition serviceFilter = new CINodeFilterDataDefinition();
        componentInstance.setNodeFilter(serviceFilter);
        propertyFilterDataDefinition = new PropertyFilterDataDefinition();
        propertyFilterDataDefinition.setName("Name1");
        final var propertyFilterConstraint1 = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint1.setPropertyName("mem_size");
        propertyFilterConstraint1.setOperator(ConstraintType.EQUAL);
        propertyFilterConstraint1.setValue(createToscaGetFunction(CI_NAME, ToscaGetFunctionType.GET_PROPERTY, List.of("size")));
        propertyFilterConstraint1.setValueType(FilterValueType.GET_PROPERTY);
        propertyFilterConstraint1.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition.setConstraints(List.of(propertyFilterConstraint1));
        propertyFilterDataDefinition2 = new PropertyFilterDataDefinition();
        propertyFilterDataDefinition2.setName("Name2");
        final var propertyFilterConstraint2 = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint2.setPropertyName("mem_size");
        propertyFilterConstraint2.setOperator(ConstraintType.EQUAL);
        propertyFilterConstraint2.setValue(createToscaGetFunction("SELF", ToscaGetFunctionType.GET_PROPERTY, List.of(A_PROP_NAME)));
        propertyFilterConstraint2.setValueType(FilterValueType.GET_PROPERTY);
        propertyFilterConstraint2.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition2.setConstraints(List.of(propertyFilterConstraint2));

        ListDataDefinition<PropertyFilterDataDefinition> listDataDefinition =
            new ListDataDefinition<>(List.of(propertyFilterDataDefinition, propertyFilterDataDefinition2));
        serviceFilter.setProperties(listDataDefinition);
        PropertyDefinition property = new PropertyDefinition();
        property.setName(A_PROP_NAME);
        service.setProperties(List.of(property));
    }

    protected static ToscaGetFunctionDataDefinition createToscaGetFunction(final String sourceName,
                                                                           final ToscaGetFunctionType toscaGetFunctionType,
                                                                           final List<String> propertyPathFromSource) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(toscaGetFunctionType);
        toscaGetFunction.setPropertyPathFromSource(propertyPathFromSource);
        toscaGetFunction.setSourceName(sourceName);
        toscaGetFunction.setPropertyName(propertyPathFromSource.get(0));
        return toscaGetFunction;
    }

}
