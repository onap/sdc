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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

/**
 * Created by mlando on 2/23/2016.
 */
public class ProductLogic {

	private static Logger log = LoggerFactory.getLogger(ProductLogic.class.getName());

	public boolean deleteAllProducts(String titanFile, String beHost, String bePort, String adminUser) {
		log.debug("retrieving all products from graph");
		RestUtils restUtils = null;
		try {
			List<String> productList = getAllProducts(titanFile);
			restUtils = new RestUtils();
			if (productList != null) {
				for (String productUid : productList) {
					Integer status = restUtils.deleteProduct(productUid, beHost, bePort, adminUser);
				}
				return true;
			} else {
				log.error("failed to get products from graph");
				return false;
			}
		} finally {
			if (restUtils != null) {
				restUtils.closeClient();
			}
		}
	}

	private List<String> getAllProducts(String titanFile) {
		TitanGraph graph = null;
		try {
			graph = openGraph(titanFile);
			List<String> productsToDelete = new ArrayList<String>();
			Iterable vertices = graph.query()
					.has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Product.getName()).vertices();
			if (vertices != null) {
				Iterator<TitanVertex> iter = vertices.iterator();
				while (iter.hasNext()) {
					Vertex vertex = iter.next();
					String id = vertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					productsToDelete.add(id);
				}
			}

			graph.tx().commit();
			return productsToDelete;
		} catch (Exception e) {
			e.printStackTrace();
			graph.tx().rollback();
			return null;

		} finally {
			if (graph != null) {
				graph.close();
			}
		}
	}

	private TitanGraph openGraph(String titanFileLocation) {

		TitanGraph graph = TitanFactory.open(titanFileLocation);

		return graph;

	}

}
