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

package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class SchemaFiles {
	
	@Test
	public void testValidateYamlNormativeFiles(){
		String importToscaPath = "src/main/resources/import/tosca";
		assertTrue(checkValidYamlInFileTree(importToscaPath));
	}
	
	@Test
	public void testRainyYamlNormativeFiles(){
		String importToscaPathTest = "src/test/resources/yamlValidation";
		assertFalse(checkValidYamlInFileTree(importToscaPathTest));
	}
	
	private boolean checkValidYamlInFileTree(String fileTree)  {
		
		try {
			List<Path> fileTreeYamlList = Files.walk(Paths.get(fileTree))
			  .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".yml"))
			  .collect(Collectors.toList());
			
			for (Path yamlFile : fileTreeYamlList) {
				try {
					FileInputStream inputStream = new FileInputStream(yamlFile.toAbsolutePath().toString());
			    	Yaml yaml = new Yaml();
			    	Object content = yaml.load(inputStream);
				} catch (Exception e) {	
					System.out.println("Not valid yaml in file creation : " + yamlFile.toAbsolutePath().toString());
					return false;
				}
			}
		} catch (IOException e) {
			System.out.println("Error in reading file from folder : " + fileTree);
			return false;
		}
		return true;
	}
	
	
}
