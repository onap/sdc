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

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteComponentHandlerTest {
    @InjectMocks
    private DeleteComponentHandler test;

    @Mock
    private JanusGraphDao janusGraphDao;

    @Mock
    private TopologyTemplateOperation topologyTemplateOperation;

    @Mock
    TopologyTemplate toscaElement;

    @Test
    public void testDeleteComponent() {

        String input = "yes";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        String id = "start";
        GraphVertex loc = new GraphVertex();
        JanusGraphVertex vertex = Mockito.mock(JanusGraphVertex.class);
        loc.setVertex(vertex);

        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        metadataProperties.put(GraphPropertyEnum.USERID, loc.getUniqueId());
        metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.USER.getName());
        metadataProperties.put(GraphPropertyEnum.NAME, "user1");
        loc.setMetadataProperties(metadataProperties);

        when(janusGraphDao.getVertexById(id)).thenReturn(Either.left(loc));
        when(topologyTemplateOperation.deleteToscaElement(ArgumentMatchers.any(GraphVertex.class))).thenReturn(Either.left(toscaElement));

        test.deleteComponent(id,scanner);
    }
}
