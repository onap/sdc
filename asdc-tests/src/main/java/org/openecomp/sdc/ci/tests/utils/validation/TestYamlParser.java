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

package org.openecomp.sdc.ci.tests.utils.validation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.common.util.ZipUtil;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;




public class TestYamlParser extends ComponentBaseTest{


	
	
	@Rule
	public static TestName name = new TestName();

	public TestYamlParser() {
		super(name, TestYamlParser.class.getName());
	}

	
	

	
	@Test
	public void testYaml() throws IOException{
		
		System.out.println("");
		
		File file = new File("\\\\Comp-1\\FileIO\\Stop.txt");
		
		
		//read file


		Map<String, byte[]> readZip = null;
		Path path = Paths.get("C:\\Users\\ys9693\\Documents\\csar\\attributesWithProporties\\attributesWithProporties.csar");
		byte[] data = Files.readAllBytes(path);
		if (data != null && data.length > 0) {
			readZip = ZipUtil.readZip(data);

		}

		byte[] artifactsBs = readZip.get("Definitions/VF_RI2_G6.yaml");
		String str = new String(artifactsBs, StandardCharsets.UTF_8);
		
				

		
		Yaml yaml = new Yaml();
		Map<String, Object> load = (Map<String, Object>) yaml.load(str);
		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		
		Set<String> keySet = node_templates.keySet();
	}
	

}
