/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.util.HashMap;
import java.util.Map;


public class ServiceMetadataDataTest {

	private static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
			"src/test/resources/config/catalog-dao");
	private static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

	private ServiceMetadataData createTestSubject() {
		return new ServiceMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ServiceMetadataData(new GraphPropertiesDictionaryExtractor(new HashMap<>()));
		new ServiceMetadataData(new ServiceMetadataDataDefinition());
	}
	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ServiceMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ServiceMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}
