package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("migration1707vnfFix")
public class Migration1707VnfFix{

	private static final String VF_MODULES_METADATA = "vfModulesMetadata";

	@Autowired
	private TitanDao titanDao;

	@Autowired
	private TopologyTemplateOperation topologyTemplateOperation;

	private static Logger LOGGER = LoggerFactory.getLogger(Migration1707RelationsFix.class);

	public boolean migrate() {
		boolean result = true;

		Map<GraphPropertyEnum, Object> propsHasNot = new EnumMap<>(GraphPropertyEnum.class);
		propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);

		Map<GraphPropertyEnum, Object> propsHas = new EnumMap<>(GraphPropertyEnum.class);
		propsHas.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
		propsHas.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.VF.name());
		propsHas.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED);

		Either<List<GraphVertex>, TitanOperationStatus> getAllTopologyTemplatesRes = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, propsHasNot, JsonParseFlagEnum.ParseMetadata);
		if (getAllTopologyTemplatesRes.isRight() && getAllTopologyTemplatesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
			LOGGER.debug("Failed to fetch all non marked as deleted topology templates , error {}", getAllTopologyTemplatesRes.right().value());
			result = false;
		}
		List<GraphVertex> metadataVertices = getAllTopologyTemplatesRes.left().value();
		for (GraphVertex metadataV : metadataVertices) {
			Either<Map<String, ArtifactDataDefinition>, TitanOperationStatus> dataFromGraph = topologyTemplateOperation.getDataFromGraph(metadataV.getUniqueId(), EdgeLabelEnum.DEPLOYMENT_ARTIFACTS);
			if (dataFromGraph.isLeft()) {
				Map<String, ArtifactDataDefinition> artifacts = dataFromGraph.left().value();
				if (artifacts.containsKey(VF_MODULES_METADATA)) {
					artifacts.remove(VF_MODULES_METADATA);
					Either<GraphVertex, TitanOperationStatus> vertexById = titanDao.getVertexById(metadataV.getUniqueId());
					TitanVertex vertex = vertexById.left().value().getVertex();
					Iterator<Edge> edges = vertex.edges(Direction.OUT, EdgeLabelEnum.DEPLOYMENT_ARTIFACTS.name());
					if (edges.hasNext()) {
						Edge edge = edges.next();
						Vertex dataV = edge.inVertex();

						String jsonStr;
						try {
							jsonStr = JsonParserUtils.jsonToString(artifacts);
							dataV.property(GraphPropertyEnum.JSON.getProperty(), jsonStr);
						} catch (Exception e) {
							LOGGER.debug("Failed to update deployment artifacts for VF {}", metadataV.getUniqueId());
						}
					}
				}
			}
			TitanOperationStatus commit = titanDao.commit();
			if ( commit != TitanOperationStatus.OK){
				LOGGER.debug("Failed to commit changes for deployment artifacts for VF {} {}", metadataV.getUniqueId(), metadataV.getMetadataProperty(GraphPropertyEnum.NAME));
			}
		}

		return result;
	}

	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

}
