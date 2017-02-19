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

package org.openecomp.sdc.asdctool.impl.migration.v1607;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public class CsarMigration {
	private static Logger log = LoggerFactory.getLogger(CsarMigration.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;

	public boolean removeCsarResources() {
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());

		Either<List<ResourceMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao
				.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (byCriteria.isRight()) {
			log.debug("Failed to fetch VF resources by criteria ", byCriteria.right().value());
			return false;
		}
		List<ResourceMetadataData> resources = byCriteria.left().value();

		try {
			for (ResourceMetadataData data : resources) {
				if (data.getMetadataDataDefinition().getCsarUUID() != null) {
					log.debug("VF {} with CSAR {}", data.getUniqueId(), data.getMetadataDataDefinition().getCsarUUID());
					Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao
							.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), data.getUniqueId());
					if (vertexByProperty.isRight()) {
						log.debug("Failed to fetch vertex with id {} . skip resource {} ", data.getUniqueId(),
								data.getMetadataDataDefinition().getName());
						continue;
					}
					Vertex vertex = vertexByProperty.left().value();
					Iterator<VertexProperty<Object>> properties = vertex
							.properties(GraphPropertiesDictionary.CSAR_UUID.getProperty());
					while (properties.hasNext()) {
						VertexProperty<Object> next = properties.next();
						next.remove();
					}

				}
			}
			titanGenericDao.commit();
		} catch (Exception e) {
			log.debug("Failed to clean CSAR UUID. rollback");
			titanGenericDao.rollback();
		}

		return true;
	}

}
