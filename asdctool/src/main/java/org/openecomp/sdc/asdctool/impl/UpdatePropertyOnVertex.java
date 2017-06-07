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

package org.openecomp.sdc.asdctool.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.Utils;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanGraphQuery;

public class UpdatePropertyOnVertex {

	private static Logger log = LoggerFactory.getLogger(UpdatePropertyOnVertex.class.getName());

	public Integer updatePropertyOnServiceAtLeastCertified(String titanFile, Map<String, Object> keyValueToSet,
			List<Map<String, Object>> orCriteria) {

		TitanGraph graph = null;

		Integer numberOfUpdatedVertexes = 0;

		try {
			graph = openGraph(titanFile);

			if (orCriteria != null && false == orCriteria.isEmpty()) {

				for (Map<String, Object> criteria : orCriteria) {

					TitanGraphQuery<? extends TitanGraphQuery> query = graph.query();

					if (criteria != null && !criteria.isEmpty()) {
						for (Map.Entry<String, Object> entry : criteria.entrySet()) {
							query = query.has(entry.getKey(), entry.getValue());
						}
					}

					Iterator iterator = query
							.has(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name())
							.vertices().iterator();

					boolean isFoundAtLeastOneCertifiedService = false;
					while (iterator.hasNext()) {
						Vertex vertex = (Vertex) iterator.next();

						Map<String, Object> leftProps = Utils.getProperties(vertex);
						boolean vertexLeftContainsRightProps = Utils.vertexLeftContainsRightProps(leftProps, criteria);
						if (false == vertexLeftContainsRightProps) {
							log.debug("Ignore vertex since properties it does not contains properties {}. Vertex properties are {}",criteria,leftProps);
							continue;
						}

						isFoundAtLeastOneCertifiedService = true;
						break;
					}

					if (true == isFoundAtLeastOneCertifiedService) {

						Integer currentNumberOfUpdates = updateVertexes(keyValueToSet, graph, criteria);

						if (currentNumberOfUpdates != null) {
							numberOfUpdatedVertexes += currentNumberOfUpdates;
						}

					} else {
						log.debug("No certified service was found for criteria {}",criteria);
					}
				}

			}

			// graph.commit();
			graph.tx().commit();

			return numberOfUpdatedVertexes;

		} catch (Exception e) {
			e.printStackTrace();
			// graph.rollback();
			graph.tx().rollback();

			return null;

		} finally {
			if (graph != null) {
				// graph.shutdown();
				graph.close();
			}
		}

	}

	private Integer updateVertexes(Map<String, Object> keyValueToSet, TitanGraph graph, Map<String, Object> criteria) {
		Integer numberOfUpdatedVertexesPerService = 0;

		TitanGraphQuery<? extends TitanGraphQuery> updateQuery = graph.query();

		if (criteria != null && !criteria.isEmpty()) {
			for (Map.Entry<String, Object> entry : criteria.entrySet()) {
				updateQuery = updateQuery.has(entry.getKey(), entry.getValue());
			}
		}
		Iterator updateIterator = updateQuery.vertices().iterator();

		while (updateIterator.hasNext()) {

			Vertex vertex = (Vertex) updateIterator.next();

			Map<String, Object> leftProps = Utils.getProperties(vertex);

			boolean vertexLeftContainsRightProps = Utils.vertexLeftContainsRightProps(leftProps, criteria);
			if (false == vertexLeftContainsRightProps) {
				log.debug("Ignore vertex since properties it does not contains properties {}. Vertex properties are {}",criteria,leftProps);
				continue;
			}

			if (keyValueToSet != null) {
				for (Entry<String, Object> entry : keyValueToSet.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();

					// vertex.setProperty(key, value);
					vertex.property(key, value);
					
					if (log.isDebugEnabled()){
						log.debug("After setting vertex {} {} with key value {},{}",  
							vertex.property(GraphPropertiesDictionary.NAME.getProperty()),
							vertex.property(GraphPropertiesDictionary.VERSION.getProperty()),key,value);
					}
					numberOfUpdatedVertexesPerService++;
				}
			}

		}

		log.info(
				"The number of updated services for criteria " + criteria + " is " + numberOfUpdatedVertexesPerService);
		return numberOfUpdatedVertexesPerService;
	}

	public TitanGraph openGraph(String titanFileLocation) {

		TitanGraph graph = TitanFactory.open(titanFileLocation);

		return graph;

	}

}
