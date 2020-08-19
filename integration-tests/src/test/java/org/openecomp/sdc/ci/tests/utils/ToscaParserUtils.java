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

package org.openecomp.sdc.ci.tests.utils;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaImportsDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaMetadataDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaNodeTemplatesTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaParameterConstants;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaSubstitutionMappingsDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class ToscaParserUtils {

	private static Logger log = LoggerFactory.getLogger(ToscaParserUtils.class.getName());

	
	/**method get csarUUID and send GET API request toward BE 
	 * @param csarUUID
	 * @return
	 * @throws Exception
	 */
	public static ToscaDefinition parseToscaYamlToJavaObjectByCsarUuid(String csarUUID) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String TOSCAMetaLocation = ToscaParameterConstants.TOSCA_META_PATH;
		Map<?, ?> map = getToscaYamlMap(csarUUID, TOSCAMetaLocation);
		assertNotNull("Tosca Entry-Definitions is null", map);
		if (map != null) {
			File definitionYamlLocation = (File) map.get(ToscaParameterConstants.ENTRY_DEFINITION);
			toscaDefinition = parseToscaYamlToJavaObject(definitionYamlLocation);
		}
		return toscaDefinition;

	}

	/**method read csar from location
	 * @param csarNameLocation - full path with csar name 
	 * @return
	 * @throws Exception
	 */
	public static ToscaDefinition parseToscaMainYamlToJavaObjectByCsarLocation(File csarNameLocation) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String TOSCAMetaLocation = ToscaParameterConstants.TOSCA_META_PATH;
//		read file location of main yaml file(location+name) from TOSCA.meta file by 
		Map<?, ?> map = getToscaYamlMap(csarNameLocation, TOSCAMetaLocation);
		
		assertNotNull("Tosca Entry-Definitions is null", map);

		String definitionYamlLocation = (String) map.get(ToscaParameterConstants.ENTRY_DEFINITION);
		String csarPayload = getYamlPayloadFromCsar(csarNameLocation, definitionYamlLocation);
		toscaDefinition = parseToscaYamlPayloadToJavaObject(csarPayload);
		return toscaDefinition;

	}
	
	public static ToscaDefinition parseToscaAnyYamlToJavaObjectByCsarLocation(File csarNameLocation, String yamlLocation) throws Exception {
		
		ToscaDefinition toscaDefinition = null;
		String csarPayload = getYamlPayloadFromCsar(csarNameLocation, yamlLocation);
		toscaDefinition = parseToscaYamlPayloadToJavaObject(csarPayload);
		return toscaDefinition;

	}
	
	public static ToscaDefinition parseToscaYamlToJavaObject(File path) throws Exception {

		ToscaDefinition toscaDefinition = null;
		
//        File path = new File("C:/Data/D2.0/TOSCA_Ex/Definitions/tosca_definition_version.yaml");
        FileInputStream fis = null;
        try {
			try (FileInputStream fileInputStream = fis = new FileInputStream(path)) {
			}
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
        
        Constructor constructor = initToscaDefinitionObject();
    	
        Yaml yaml = new Yaml(constructor);
        try {
        	toscaDefinition = (ToscaDefinition) yaml.load(fis);
		} catch (Exception e) {
			log.debug("Failed to parse tosca yaml file");
			System.out.println("Exception: " + e);
		} finally {
			fis.close();
		}
        return toscaDefinition;
        
	}

	public static ToscaDefinition parseToscaYamlPayloadToJavaObject(String payload){

		ToscaDefinition toscaDefinition = null;
        Constructor constructor = initToscaDefinitionObject();
    	
        Yaml yaml = new Yaml(constructor);
        try {
        	toscaDefinition = (ToscaDefinition) yaml.load(payload);
		} catch (Exception e) {
			log.debug("Failed to parse tosca yaml file", e);
			fail("Exception: " + e);
		}
        return toscaDefinition;
        
	}
	
	
	public static Constructor initToscaDefinitionObject() {
		Constructor toscaStructure = new Constructor(ToscaDefinition.class);
        toscaStructure.addTypeDescription(ToscaDefinition.getTypeDescription());
        toscaStructure.addTypeDescription(ToscaTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaNodeTemplatesTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaGroupsTopologyTemplateDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaSubstitutionMappingsDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaImportsDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaMetadataDefinition.getTypeDescription());
    	toscaStructure.addTypeDescription(ToscaInputsTopologyTemplateDefinition.getTypeDescription());
//    	Skip properties which are found in YAML, but not found in POJO
    	PropertyUtils propertyUtils = new PropertyUtils();
    	propertyUtils.setSkipMissingProperties(true);
    	toscaStructure.setPropertyUtils(propertyUtils);
		return toscaStructure;
	}

	public static Map<?, ?> getToscaYamlMap(String csarUUID, String yamlFileLocation) throws Exception {
		String csarPayload = getCsarPayload(csarUUID, yamlFileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}
	
	public static Map<?, ?> getToscaYamlMap(File csarPath, String yamlFileLocation) throws Exception {
		String csarPayload = getYamlPayloadFromCsar(csarPath, yamlFileLocation);
		if (csarPayload != null) {
			Yaml yaml = new Yaml();
			Map<?, ?> map = (Map<?, ?>) yaml.load(csarPayload);
			return map;
		}
		return null;
	}
	
	
	public static String getCsarPayload(String csarName, String yamlFileLocation) throws Exception {

	    HttpResponse<byte []> csar = ImportRestUtils.getCsar(csarName, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertTrue("Return response code different from 200", csar.getStatusCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		byte[] data = csar.getResponse();
		return getDataFromZipFileByBytes(yamlFileLocation, data);

	}

	public static String getYamlPayloadFromCsar(File csarName, String fileLocation) throws IOException, ZipException {
		Path path = csarName.toPath();
		byte[] data = Files.readAllBytes(path);
		return getDataFromZipFileByBytes(fileLocation, data);
	}

	/** method get file data from zip data by file location in the zip structure 
	 * @param fileLocation
	 * @param data
	 * @return
	 */
	public static String getDataFromZipFileByBytes(String fileLocation, byte[] data) throws ZipException {
		if (data == null || data.length == 0) {
			return null;
		}
		final Map<String, byte[]> readZip = ZipUtils.readZip(data, false);
		if (MapUtils.isEmpty(readZip)) {
			return null;
		}
		byte[] artifactsBytes = readZip.get(fileLocation);
		return new String(artifactsBytes, StandardCharsets.UTF_8);
	}
	
}
