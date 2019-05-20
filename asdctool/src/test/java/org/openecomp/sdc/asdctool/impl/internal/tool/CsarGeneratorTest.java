/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.asdctool.impl.internal.tool;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsarGeneratorTest {
    @InjectMocks
    private CsarGenerator test;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private Component component;

    @Mock
    ToscaOperationFacade toscaOperationFacade;

    @Test
    public void testGenerateCsar() {
        String uuid = "yes";
        InputStream in = new ByteArrayInputStream(uuid.getBytes());
        Scanner scanner = new Scanner(in);
        String uniqueId = "123";
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setUniqueId(uniqueId);
        list.add(graphVertex);

        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.UUID, uuid);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        graphVertex.setMetadataProperties(props);

        when(janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, props)).thenReturn(Either.left(list));
        when(toscaOperationFacade.getToscaFullElement(any(String.class))).thenReturn(Either.left(component));
        when(janusGraphDao
            .getChildVertex(graphVertex, EdgeLabelEnum.TOSCA_ARTIFACTS, JsonParseFlagEnum.ParseJson)).thenReturn(Either.left(graphVertex));

        test.generateCsar(uuid,scanner);
    }
}
