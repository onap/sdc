package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToscaElementOperationCatalogTest {

    private ArrayList<Vertex> vertexList = new ArrayList<>();

    @Mock
    Vertex vertex;
    @Mock
    JanusGraphDao janusGraphDao;
    @Mock
    VertexProperty<Object> property;

    @InjectMocks
    private ToscaElementOperation toscaOperation = new TopologyTemplateOperation();

    @Before
    public void setUp() {
        vertexList.add(vertex);
        when(janusGraphDao.getCatalogOrArchiveVerticies(true)).thenReturn(Either.left(vertexList.iterator()));
        when(janusGraphDao.getChildVertex(vertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.NoParse))
                .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(vertex.property(GraphPropertiesDictionary.METADATA.getProperty())).thenReturn(property);
    }

    @Test
    public void getComponentFromCatalogWhenDeleteIsTrue() {
        final String vertexJsonIsDeletedTrue = "{\"lifecycleState\":\"CERTIFIED\",\"componentType\":\"RESOURCE\",\"vendorRelease\":\"1\",\"contactId\":\"ah7840\",\"lastUpdateDate\":1496119811038,\"icon\":\"att\",\"description\":\"Cloud\",\"creationDate\":1459432094781,\"vendorName\":\"AT&T\",\"mandatory\":false,\"version\":\"1.0\",\"tags\":[\"Cloud\"],\"highestVersion\":true,\"systemName\":\"Cloud\",\"name\":\"Cloud\",\"isDeleted\":true,\"invariantUuid\":\"ab5263fd-115c-41f2-8d0c-8b9279bce2b2\",\"UUID\":\"208cf7df-68a1-4ae7-afc9-409ea8012332\",\"normalizedName\":\"cloud\",\"toscaResourceName\":\"org.openecomp.resource.Endpoint.Cloud\",\"uniqueId\":\"9674e7e1-bc1a-41fe-b503-fbe996801475\",\"resourceType\":\"VFC\"}";
        when(property.value()).thenReturn(vertexJsonIsDeletedTrue);
        List<CatalogComponent> componentList = toscaOperation.getElementCatalogData(true, null).left().value();
        assertTrue(componentList.isEmpty());
    }

    @Test
    public void getComponentFromCatalogWhenDeleteNotFound() {
        final String vertexJsonIsDeletedNotFound = "{\"lifecycleState\":\"CERTIFIED\",\"componentType\":\"RESOURCE\",\"vendorRelease\":\"1\",\"contactId\":\"ah7840\",\"lastUpdateDate\":1496119811038,\"icon\":\"att\",\"description\":\"Cloud\",\"creationDate\":1459432094781,\"vendorName\":\"AT&T\",\"mandatory\":false,\"version\":\"1.0\",\"tags\":[\"Cloud\"],\"highestVersion\":true,\"systemName\":\"Cloud\",\"name\":\"Cloud\",\"invariantUuid\":\"ab5263fd-115c-41f2-8d0c-8b9279bce2b2\",\"UUID\":\"208cf7df-68a1-4ae7-afc9-409ea8012332\",\"normalizedName\":\"cloud\",\"toscaResourceName\":\"org.openecomp.resource.Endpoint.Cloud\",\"uniqueId\":\"9674e7e1-bc1a-41fe-b503-fbe996801475\",\"resourceType\":\"VFC\"}";
        when(property.value()).thenReturn(vertexJsonIsDeletedNotFound);
        List<CatalogComponent> componentList = toscaOperation.getElementCatalogData(true, null).left().value();
        assertEquals(1, componentList.size());
    }

    @Test
    public void getComponentFromCatalogWhenDeleteIsFalse() {
        final String vertexJsonIsDeletedFalse = "{\"lifecycleState\":\"CERTIFIED\",\"componentType\":\"RESOURCE\",\"vendorRelease\":\"1\",\"contactId\":\"ah7840\",\"lastUpdateDate\":1496119811038,\"icon\":\"att\",\"description\":\"Cloud\",\"creationDate\":1459432094781,\"vendorName\":\"AT&T\",\"mandatory\":false,\"version\":\"1.0\",\"tags\":[\"Cloud\"],\"highestVersion\":true,\"systemName\":\"Cloud\",\"name\":\"Cloud\",\"isDeleted\":false,\"invariantUuid\":\"ab5263fd-115c-41f2-8d0c-8b9279bce2b2\",\"UUID\":\"208cf7df-68a1-4ae7-afc9-409ea8012332\",\"normalizedName\":\"cloud\",\"toscaResourceName\":\"org.openecomp.resource.Endpoint.Cloud\",\"uniqueId\":\"9674e7e1-bc1a-41fe-b503-fbe996801475\",\"resourceType\":\"VFC\"}";
        when(property.value()).thenReturn(vertexJsonIsDeletedFalse);
        List<CatalogComponent> componentList = toscaOperation.getElementCatalogData(true, null).left().value();
        assertEquals(1, componentList.size());
    }
}
