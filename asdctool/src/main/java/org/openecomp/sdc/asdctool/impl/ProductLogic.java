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
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Created by mlando on 2/23/2016.
 */
public class ProductLogic {

    private static final Logger log = Logger.getLogger(ProductLogic.class.getName());

    public boolean deleteAllProducts(String janusGraphFile, String beHost, String bePort, String adminUser) {
        log.debug("retrieving all products from graph");
        List<String> productList = getAllProducts(janusGraphFile);
        if (productList.isEmpty()) {
            log.error("failed to get products from graph");
            return false;
        }
        for (String productUid : productList) {
            new RestUtils().deleteProduct(productUid, beHost, bePort, adminUser);
        }
        return true;
    }

    private List<String> getAllProducts(String janusGraphFile) {
        List<String> productsToDelete = new ArrayList<>();
        Transaction transac = null;
        try (JanusGraph graph = JanusGraphFactory.open(janusGraphFile)) {
            transac = graph.tx();
            Iterable vertices = graph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Product.getName()).vertices();
            if (vertices != null) {
                for (Vertex vertex : (Iterable<Vertex>) vertices) {
                    String id = vertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
                    productsToDelete.add(id);
                }
            }
            transac.commit();
        } catch (Exception e) {
            log.error("get All Products failed", e);
            if (transac != null) {
                transac.rollback();
            }
        }
        return productsToDelete;
    }
}
