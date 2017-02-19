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

package org.openecomp.sdc.asdctool.impl.migration.v1610;

import com.google.gson.Gson;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.CacheMangerOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by mlando on 8/17/2016.
 */
public class TitanFixUtils {
	private static Logger log = LoggerFactory.getLogger(TitanFixUtils.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;
	@Autowired
	protected CacheMangerOperation cacheMangerOperation;

	public boolean fixIconsInNormatives() {
		log.info("starting fix");
		String vlName = "VL";
		String elineName = "VL_ELINE";
		String elineFixedName = "VL ELINE";
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
		if (graphResult.isRight()) {
			log.error("failed to get graph object.");
			return false;
		}

		boolean operationFailed = false;
		Map<String, Object> vlVerticeProperties = null;
		Map<String, Object> elineVerticeProperties = null;

		try {
			TitanGraph titanGraph = graphResult.left().value();
			log.info("look up vl :{}", vlName);

			Iterable<TitanVertex> vertices = titanGraph.query()
					.has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName())
					.has(GraphPropertiesDictionary.NAME.getProperty(), vlName)
					.has(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true).vertices();

			if (vertices == null) {
				log.error("failed to get  vernice for resource name {}", vlName);
				operationFailed = true;
				return false;
			}

			Iterator<TitanVertex> iterator = vertices.iterator();
			List<TitanVertex> vertexList = new ArrayList<>();

			if (iterator == null) {
				log.error("failed to get iterator over vertices object returned for resource id {}", vlName);
				operationFailed = true;
				return false;
			}

			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertexList.add(vertex);
			}

			if (!(vertexList.size() == 1)) {
				log.error("failed to get 1 vertex for resource {} with highest true. instead got {}", vlName,
						vertexList.size());
				operationFailed = true;
				return false;
			}

			TitanVertex vlVertex = vertexList.get(0);

			log.info("look up eline:{}", elineName);

			boolean vl_eline_found = true;

			vertices = titanGraph.query()
					.has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName())
					.has(GraphPropertiesDictionary.NAME.getProperty(), elineName)
					.has(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true).vertices();

			if (vertices == null) {
				log.error("failed to get  vertices object returned for resource {}", elineName);
				operationFailed = true;

				vl_eline_found = false;
			}

			if (vl_eline_found) {
				iterator = vertices.iterator();
				vertexList = new ArrayList<>();
				if (iterator == null) {
					log.error("failed to get iterator over vertices object returned for resource id {}", elineName);
					operationFailed = true;

					vl_eline_found = false;
				}

				if (vl_eline_found) {
					while (iterator.hasNext()) {
						TitanVertex vertex = iterator.next();
						vertexList.add(vertex);
					}

					if (!(vertexList.size() == 1)) {
						log.error("failed to get 1 vertex for resource  {} with highest true. instead got {}",
								elineName, vertexList.size());
						operationFailed = true;

						vl_eline_found = false;
					}
				}
			}

			if (!vl_eline_found) {
				log.info("look up eline:{}", elineFixedName);
				vl_eline_found = true;
				operationFailed = false;

				vertices = titanGraph.query()
						.has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName())
						.has(GraphPropertiesDictionary.NAME.getProperty(), elineFixedName)
						.has(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true).vertices();

				if (vertices == null) {
					log.error("failed to get  vertices object returned for resource {}", elineFixedName);
					operationFailed = true;

					vl_eline_found = false;
				}

				if (vl_eline_found) {
					iterator = vertices.iterator();
					vertexList = new ArrayList<>();
					if (iterator == null) {
						log.error("failed to get iterator over vertices object returned for resource id {}",
								elineFixedName);
						operationFailed = true;

						vl_eline_found = false;
					}

					if (vl_eline_found) {
						while (iterator.hasNext()) {
							TitanVertex vertex = iterator.next();
							vertexList.add(vertex);
						}

						if (!(vertexList.size() == 1)) {
							log.error("failed to get 1 vertex for resource  {} with highest true. instead got {}",
									elineFixedName, vertexList.size());
							operationFailed = true;

							vl_eline_found = false;
						}
					}
				}
			}

			if (!vl_eline_found) {
				return false;
			} else {
				TitanVertex elineVertex = vertexList.get(0);

				vlVerticeProperties = titanGenericDao.getProperties(vlVertex);

				log.info("VL Vertice  Properties {}", vlVerticeProperties);
				if ("network".equals(vlVerticeProperties.get(GraphPropertiesDictionary.ICON.getProperty()))) {
					log.info("nothing to update in vl");
				} else {
					log.info("updating property icon of vl");
					vlVertex.property(GraphPropertiesDictionary.ICON.getProperty(), "network");
				}

				elineVerticeProperties = titanGenericDao.getProperties(elineVertex);

				log.info("eline vertice  Properties {}", elineVerticeProperties);
				if ("network".equals(elineVerticeProperties.get(GraphPropertiesDictionary.ICON.getProperty()))) {
					log.info("nothing to update in eline");
				} else {
					log.info("updating property icon of eline");
					elineVertex.property(GraphPropertiesDictionary.ICON.getProperty(), "network");
				}

				if ("VL ELINE".equals(elineVerticeProperties.get(GraphPropertiesDictionary.NAME.getProperty()))) {
					log.info("nothing to update in eline");
				} else {
					log.info("updating property name and tag of eline");
					elineVertex.property(GraphPropertiesDictionary.NAME.getProperty(), elineFixedName);
					List<String> tags = new ArrayList<>();
					tags.add("VL ELINE");
					elineVertex.property(GraphPropertiesDictionary.TAGS.getProperty(), new Gson().toJson(tags));
				}

				log.info("print current properties state");

				vlVerticeProperties = titanGenericDao.getProperties(vlVertex);

				log.info("vertice vl Properties {}", vlVerticeProperties);
				elineVerticeProperties = titanGenericDao.getProperties(elineVertex);

				log.info("vertice eline Properties {}", elineVerticeProperties);
			}

			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				log.error("exception", e);
			}
			return true;
		} finally {
			if (operationFailed) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
				long time = System.currentTimeMillis();
				if (vlVerticeProperties != null) {
					cacheMangerOperation.updateComponentInCache(
							(String) vlVerticeProperties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()), time,
							NodeTypeEnum.Resource);
				}
				if (elineVerticeProperties != null) {
					cacheMangerOperation.updateComponentInCache(
							(String) elineVerticeProperties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()),
							time, NodeTypeEnum.Resource);
				}
			}
		}
	}

	/**
	 * in 1610 we encounter an issue that if a capability property overrides a
	 * property of a derived capability then it was created with out a property
	 * type when it was first imported as part of the capability types. this
	 * will add property type to the properties missing it.
	 */
	public boolean fixCapabiltyPropertyTypes() {

		String propertyIdSecure = "tosca.capabilities.Endpoint.Admin.secure";
		String propertyIdNetworkName = "tosca.capabilities.Endpoint.Public.network_name";
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();

		if (graphResult.isRight()) {
			log.error("failed to get graph object.");
			return false;
		}

		boolean operationFailed = false;
		try {
			TitanGraph titanGraph = graphResult.left().value();
			log.info("look up propertyIdSecure:{}", propertyIdSecure);
			Iterable<TitanVertex> vertices = titanGraph.query()
					.has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyIdSecure).vertices();
			if (vertices == null) {
				log.error("failed to get  vertices object returned for resource id {}", propertyIdSecure);
				operationFailed = true;
				return false;
			}
			Iterator<TitanVertex> iterator = vertices.iterator();
			List<TitanVertex> vertexList = new ArrayList<>();

			if (iterator == null) {
				log.error("failed to get iterator over vertices object returned for resource id " + propertyIdSecure);
				operationFailed = true;
				return false;
			}

			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertexList.add(vertex);
			}

			if (!(vertexList.size() == 1)) {
				log.error("failed to get 1 vertex for resource id {} instead got {}", propertyIdSecure,
						vertexList.size());
				operationFailed = true;
				return false;
			}

			TitanVertex propertyVerticeSecure = vertexList.get(0);

			log.info("look up propertyIdNetworkName:{}", propertyIdNetworkName);
			vertices = titanGraph.query()
					.has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyIdNetworkName).vertices();
			if (vertices == null) {
				log.error("failed to get  vertices object returned for resource id {}", propertyIdNetworkName);
				operationFailed = true;
				return false;
			}

			iterator = vertices.iterator();
			vertexList = new ArrayList<>();

			if (iterator == null) {
				log.error("failed to get iterator over vertices object returned for resource id {}",
						propertyIdNetworkName);
				operationFailed = true;
				return false;
			}

			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertexList.add(vertex);
			}

			if (!(vertexList.size() == 1)) {
				log.error("failed to get 1 vertex for resource id {} instead got {}", propertyIdNetworkName,
						vertexList.size());
				operationFailed = true;
				return false;
			}

			TitanVertex propertyVerticeNetworkName = vertexList.get(0);

			Map<String, Object> verticeNetworkNameProperties = titanGenericDao
					.getProperties(propertyVerticeNetworkName);

			log.info("vertice NetworkName Properties {}", verticeNetworkNameProperties);
			Object type = verticeNetworkNameProperties.get(GraphPropertiesDictionary.TYPE.getProperty());
			if (type == null || "".equals(type)) {
				log.info("updating property Vertice Network Name");
				propertyVerticeNetworkName.property(GraphPropertiesDictionary.TYPE.getProperty(), "string");
			}

			Map<String, Object> verticeSecureProperties = titanGenericDao.getProperties(propertyVerticeSecure);

			log.info("vertice Secure Properties {}", verticeSecureProperties);

			type = verticeSecureProperties.get(GraphPropertiesDictionary.TYPE.getProperty());

			if (type == null || "".equals(type)) {
				log.info("updating property Vertice Secure");
				propertyVerticeSecure.property(GraphPropertiesDictionary.TYPE.getProperty(), "boolean");
			}

			log.info("print current properties state");

			verticeNetworkNameProperties = titanGenericDao.getProperties(propertyVerticeNetworkName);

			log.info("vertice NetworkName Properties {}", verticeNetworkNameProperties);

			verticeSecureProperties = titanGenericDao.getProperties(propertyVerticeSecure);

			log.info("vertice Secure Properties {}", verticeSecureProperties);

			return true;
		} finally {
			if (operationFailed) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
		}
	}

}
