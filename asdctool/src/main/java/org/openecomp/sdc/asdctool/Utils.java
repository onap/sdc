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

package org.openecomp.sdc.asdctool;

import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraph;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {

	private static Logger log = Logger.getLogger(Utils.class.getName());

	public final static String NEW_LINE = System.getProperty("line.separator");

	public static Response buildOkResponse(
			/*
			 * ResponseFormat errorResponseWrapper,
			 */int status, Object entity, Map<String, String> additionalHeaders) {
		ResponseBuilder responseBuilder = Response.status(status);
		if (entity != null) {
			log.trace("returned entity is {}", entity.toString());
			responseBuilder = responseBuilder.entity(entity);
		}
		if (additionalHeaders != null) {
			for (Entry<String, String> additionalHeader : additionalHeaders.entrySet()) {
				String headerName = additionalHeader.getKey();
				String headerValue = additionalHeader.getValue();
				log.trace("Adding header {} with value {} to the response", headerName, headerValue);
				responseBuilder.header(headerName, headerValue);
			}
		}
		return responseBuilder.build();
	}

	public static JanusGraph openGraph(Configuration conf) {

		JanusGraph graph = null;
		try {

			graph = JanusGraphFactory.open(conf);

		} catch (Exception e) {
			log.error("Failed to start open graph", e);
		}

		return graph;

	}

	public static boolean vertexLeftContainsRightProps(Map<String, Object> leftProps, Map<String, Object> rightProps) {

		if (rightProps != null) {

			for (Entry<String, Object> entry : rightProps.entrySet()) {
				String key = entry.getKey();
				Object leftValue = leftProps.get(key);
				Object rightValue = entry.getValue();
				if (leftValue == null) {
					if (rightValue == null) {
						continue;
					} else {
						log.debug("The key {} cannot be found in the properties {}",key,leftProps);
						return false;
					}
				}

				if (!leftValue.equals(rightValue)) {
					log.trace("The value of key {} is differnet between properties. {} vs {}",key,leftValue,rightValue);
					return false;
				}
			}

		}

		return true;
	}

	public static void setProperties(Element element, Map<String, Object> properties) {

		if (properties != null && !properties.isEmpty()) {

			Object[] propertyKeyValues = new Object[properties.size() * 2];
			int i = 0;
			for (Entry<String, Object> entry : properties.entrySet()) {
				propertyKeyValues[i++] = entry.getKey();
				propertyKeyValues[i++] = entry.getValue();
			}

			ElementHelper.attachProperties(element, propertyKeyValues);

		}

	}

	public static Map<String, Object> getProperties(Element element) {

		Map<String, Object> result = new HashMap<String, Object>();
		;

		if (element.keys() != null && element.keys().size() > 0) {
			Map<String, Property> propertyMap = ElementHelper.propertyMap(element,
					element.keys().toArray(new String[element.keys().size()]));

			for (Entry<String, Property> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue().value();

				result.put(key, value);
			}
		}
		return result;
	}
}
