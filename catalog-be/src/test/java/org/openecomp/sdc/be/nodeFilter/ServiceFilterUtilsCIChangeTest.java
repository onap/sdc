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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.exception.ResponseFormat;

public class ServiceFilterUtilsCIChangeTest extends BaseServiceFilterUtilsTest {


    @Test
    public void checkComponentInstanceIsFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CI_NAME);
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.contains(CI_NAME));
    }

    private Set<String> getNodeFiltersToBeDeleted(String inCiName) {
        requirementNodeFilterPropertyDataDefinition
                .setConstraints(Arrays.asList("mem_size:\n" + "  equal:\n" + "    get_property: ["+CI_NAME+", some static]\n"));
        ComponentInstance ci = new ComponentInstance();
        ci.setName(inCiName);
        return ServiceFilterUtils.getNodesFiltersToBeDeleted(service, ci);
    }

    @Test
    public void checkComponentInstanceIsNotFound() {
        Set<String> nodesFiltersToBeDeleted = getNodeFiltersToBeDeleted(CI_NAME + " aaa bbb");
        assertNotNull(nodesFiltersToBeDeleted);
        assertTrue(nodesFiltersToBeDeleted.isEmpty());
        assertFalse(nodesFiltersToBeDeleted.contains(CI_NAME));
    }

    @Test
    public void testServiceConstraintPairSerialization() throws IOException {
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
