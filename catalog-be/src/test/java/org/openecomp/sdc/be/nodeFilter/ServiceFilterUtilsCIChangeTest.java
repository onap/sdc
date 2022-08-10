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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.exception.ResponseFormat;

class ServiceFilterUtilsCIChangeTest extends BaseServiceFilterUtilsTest {

    @Test
    void checkComponentInstanceIsFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CI_NAME);
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.contains(CI_NAME));
    }

    private Set<String> getNodeFiltersToBeDeleted(String inCiName) {
        final var propertyFilterConstraint = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint.setPropertyName("mem_size");
        propertyFilterConstraint.setOperator(ConstraintType.EQUAL);
        propertyFilterConstraint.setValue(createToscaGetFunction(CI_NAME, ToscaGetFunctionType.GET_PROPERTY, List.of("some static")));
        propertyFilterConstraint.setValueType(FilterValueType.GET_PROPERTY);
        propertyFilterConstraint.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition
                .setConstraints(List.of(propertyFilterConstraint));
        final var componentInstance = new ComponentInstance();
        componentInstance.setName(inCiName);
        return ServiceFilterUtils.getNodesFiltersToBeDeleted(service, componentInstance);
    }

    @Test
    void checkComponentInstanceIsNotFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CI_NAME + " aaa bbb");
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.isEmpty());
    }

    @Test
    void testServiceConstraintPairSerialization() throws IOException {
        UIConstraint uiConstraint =new UIConstraint();
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        assertTrue(mapper.canSerialize(uiConstraint.getClass()));
        String data;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()  ) {
            mapper.writeValue(outputStream, uiConstraint);
            data = outputStream.toString();

        }
        assertNotNull(data);
        final AuditingManager mock = Mockito.mock(AuditingManager.class);
        final Either<UIConstraint, ResponseFormat> serviceConstraintPairResponseFormatEither =
                new ComponentsUtils(mock)
                        .convertJsonToObjectUsingObjectMapper(data, new User(), UIConstraint.class,
                                AuditingActionEnum.ADD_GROUPING, ComponentTypeEnum.SERVICE);
        assertTrue(serviceConstraintPairResponseFormatEither.isLeft());

    }
}
