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

package org.openecomp.sdc.externalApis;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.util.ZipUtil;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import com.aventstack.extentreports.Status;







public class DeploymentValiditaion extends ComponentBaseTest{


	
	
	@Rule
	public static TestName name = new TestName();

	public DeploymentValiditaion() {
		super(name, DeploymentValiditaion.class.getName());
	}
	
	protected final static String categoryFilterKey = "category";
	protected final static String subCategoryFilterKey = "subCategory";
	protected  String attVersionStr ;
	
	
	 public static List<File> listf(String directoryName) {
	        File directory = new File(directoryName);

	        List<File> resultList = new ArrayList<File>();

	        // get all the files from a directory
	        File[] fList = directory.listFiles();
	        resultList.addAll(Arrays.asList(fList));
	        for (File file : fList) {
	            if (file.isFile()) {
	                System.out.println(file.getAbsolutePath());
	            } else if (file.isDirectory()) {
	                resultList.addAll(listf(file.getAbsolutePath()));
	            }
	        }
	        //System.out.println(fList);
	        return resultList;
	    } 
	@BeforeTest
	public void beforeTest() throws Exception{
		RestResponse attVersion = CatalogRestUtils.getOsVersion();
		attVersionStr = ResponseParser.getVersionFromResponse(attVersion);
	}
	
	
	@Test
	public void pasrseNormativies() throws Exception{
		
		
		String path = "//apps//asdc_kits//catalog-be-" + attVersionStr + "//import//tosca//normative-types";
		String path2 = "//apps//asdc_kits//catalog-be-" + attVersionStr + "//import//tosca//heat-types";
				
//		String path = "C:\\Git_work\\Git_UGN\\d2-sdnc\\catalog-be\\src\\main\\resources\\import\\tosca\\normative-types";
//		String path2 = "C:\\Git_work\\Git_UGN\\d2-sdnc\\catalog-be\\src\\main\\resources\\import\\tosca\\heat-types";

		
		List<File> yamlList1 = getYamlFilesList(path);		
		List<String> nodeNamesFromYamlList1 = getNodeNamesFromYamlList(yamlList1);
		List<File> yamlList2 = getYamlFilesList(path2);		
		List<String> nodeNamesFromYamlList2 = getNodeNamesFromYamlList(yamlList2);
		
		
		List<String> expectedList = new ArrayList<String>();
		expectedList.addAll(nodeNamesFromYamlList1);
		expectedList.addAll(nodeNamesFromYamlList2);
		System.out.println("list of normatives from files:::::::::::");
		expectedList.forEach(System.out::println);
		getExtendTest().log(Status.INFO, "list of normatives from files:");
		getExtendTest().log(Status.INFO,expectedList.toString());
		
		
		String[] filter = { categoryFilterKey + "=" + ResourceCategoryEnum.GENERIC_ABSTRACT.getCategory(), subCategoryFilterKey + "=" + ResourceCategoryEnum.GENERIC_ABSTRACT.getSubCategory() };
		RestResponse assetResponse = AssetRestUtils.getComponentListByAssetType(true, AssetTypeEnum.RESOURCES);
		Map<String, String> resourceAssetList = AssetRestUtils.getResourceAssetMap(assetResponse);
		Map<String, String> resourceListFiltteredByWholeVersion = AssetRestUtils.getResourceListFiltteredByWholeVersion(resourceAssetList);
		List<String> resourceToscaNamesList = AssetRestUtils.getResourceObjectByNameAndVersionToscaNamesList(resourceListFiltteredByWholeVersion);
		System.out.println("list of normatives from APIs:::::::::::");
		resourceToscaNamesList.forEach(System.out::println);
		getExtendTest().log(Status.INFO, "list of normatives from APIs:");
		getExtendTest().log(Status.INFO, resourceToscaNamesList.toString());
		
		boolean good = true;
		List<String> missingNormatives =  new ArrayList<>();

		for (int i = 0; i < expectedList.size(); i ++) {
		    if (!resourceToscaNamesList.contains(expectedList.get(i))) {
		        good = false;
		        missingNormatives.add(expectedList.get(i));
		        
		        
		    }
		}
		
		System.out.println("<<<<<<<<<MISSING NORMATIVES>>>>>>");
		missingNormatives.forEach(System.out::println);
		getExtendTest().log(Status.INFO, "MISSING NORMATIVES:");
		getExtendTest().log(Status.INFO, missingNormatives.toString());
		
		assertTrue("missing normatives ",  good);
		
	}


	public List<String> getNodeNamesFromYamlList(List<File> yamlList) throws IOException {
		List<String> nodeNameList = new ArrayList<String>();

		for (File file : yamlList) {
		    String content = new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8);
			Yaml yaml = new Yaml();
			Map<String, Object> load = (Map<String, Object>) yaml.load(content);
			Map<String, Object> topology_template = (Map<String, Object>) load.get("node_types");
//			String string = topology_template.keySet().toString().replaceAll("tosca.nodes.", "");
			String string = topology_template.keySet().iterator().next().toString();
			System.out.println(string +" -----> "  +file.getPath());
			nodeNameList.add(string);
		}
		return nodeNameList;
	}


	public List<File> getYamlFilesList(String path) throws IOException {
		List<File> yamlList = new ArrayList<File>();
		File dir = new File(path);
		String[] extensions = new String[] { "yml" };
		System.out.println("Getting all .yml files in " + dir.getCanonicalPath()
				+ " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
		for (File file : files) {
			System.out.println("file: " + file.getCanonicalPath());
			yamlList.add(file);
		}
		return yamlList;
	}
	

	
	

	
	@Test (enabled=false)
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
	
	
	
	@Test
	public void pasrseDataTypes() throws Exception{
		
//		String path = "C:\\Git_work\\Git_UGN\\d2-sdnc\\catalog-be\\src\\main\\resources\\import\\tosca\\data-types\\dataTypes.yml";
		String path = "//apps//asdc_kits//catalog-be-" + attVersionStr + "//import//tosca//data-types//dataTypes.yml";
		
	    String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		
				
		Yaml yaml = new Yaml();
		Map<String, Object> load = (Map<String, Object>) yaml.load(content);
		List<String> listOfDataTypes = new ArrayList<String>();
		listOfDataTypes.addAll(load.keySet());
		System.out.println("<<<<<<<< List of Data Types >>>>>>>>>");
		listOfDataTypes.forEach(System.out::println);
		getExtendTest().log(Status.INFO, "List of Data Types:");
		getExtendTest().log(Status.INFO, listOfDataTypes.toString());
				
		Resource resource = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		PropertyReqDetails defaultProperty = ElementFactory.getDefaultListProperty();
		
//		SchemaDefinition schemaDefinition = new SchemaDefinition();
		
		
		
	
		defaultProperty.setPropertyDefaultValue(null);
//		defaultProperty.setSchema(schemaDefinition);
		
		
//		listOfDataTypes.remove("tosca.datatypes.Root");
		for (String dataType : listOfDataTypes) {
//			if (dataType.equals("map") ||  dataType.equals("list")){ 
			defaultProperty.setPropertyType(dataType);
			defaultProperty.setName(dataType);
			System.out.println("Adding proporty with data type: ----> " + dataType);
			getExtendTest().log(Status.INFO, "Adding proporty with data type: ----> " + dataType);
			AtomicOperationUtils.addCustomPropertyToResource(defaultProperty, resource, UserRoleEnum.DESIGNER, true);
//			}
		}

		
		listOfDataTypes.forEach(System.out::println);
	
	}
		





	@Test
	public void pasrseCategories() throws Exception{
		
//		String path = "C:\\Git_work\\Git_UGN\\d2-sdnc\\catalog-be\\src\\main\\resources\\import\\tosca\\categories\\categoryTypes.yml";
		String path = "//apps//asdc_kits//catalog-be-" + attVersionStr + "//import//tosca//categories//categoryTypes.yml";
		
	    String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		
		List<String> serviceCategories = new ArrayList<String>();
		List<String> resourceCategories = new ArrayList<String>();
				
		Yaml yaml = new Yaml();
		Map<String, Object> load = (Map<String, Object>) yaml.load(content);
		Map<String, Map> services = (Map<String, Map>) load.get("services");
		Map<String, Map> resources = (Map<String, Map>) load.get("resources");
		
		Map<String, List<String>> resourcesListFromFile = new HashMap<String, List<String>>() ;
				
		//retrieve subcategories
		for ( String resourceCategoryName : resources.keySet()) {
			Map<String, Map> subcategory = (Map) resources.get(resourceCategoryName).get("subcategories");
				
			resourceCategories = new ArrayList<String>();
			for (String subcategoryName : subcategory.keySet()) {	
				String name = (String) subcategory.get(subcategoryName).get("name");
//				resourceCategories.add(name.replaceAll("\\s",""));
				resourceCategories.add(name);
							
			}
			resourcesListFromFile.put(resources.get(resourceCategoryName).get("name").toString(), resourceCategories);	
		}		
		
			System.out.println(resourcesListFromFile.toString());
			getExtendTest().log(Status.INFO, "Expected categories:");
			getExtendTest().log(Status.INFO, resourcesListFromFile.toString());
			
		//retrieve service categories 	
//		for ( String serviceCategoryName : services.keySet()) {
//			String name = (String) services.get(serviceCategoryName).get("name");
//			serviceCategories.add(name);
//				}	
//		serviceCategories.forEach(System.out::println);
			
			
			//retrieve resource list from URL
			
			Map<String, List<CategoryDefinition>> categoriesMap = getCategories();
			List<CategoryDefinition> resourceSubCategories = categoriesMap.get(ComponentTypeEnum.RESOURCE_PARAM_NAME);
			List<SubCategoryDefinition> subcategories;
			for (CategoryDefinition categoryDefinition : resourceSubCategories) {
				subcategories =  categoryDefinition.getSubcategories();
			}
//			subcategories.stream().collect(toMap(i -> i, i -> items.get(i)));
			
//			resourceSubCategories.stream().collect(
//	                Collectors.groupingBy(CategoryDefinition::getName, Collectors.groupingBy(SubCategoryDefinition::getName)));
			
//			resourceSubCategories.stream().filter(p->p.getSubcategories()).map(m->m.getName()).collect(Collectors.toList()).collect(Collectors.toMap(CategoryDefinition::getName,m));
			
			
			Map<String, List<String>> resourceMapFromUrl = resourceSubCategories.stream().collect(Collectors.toMap( e -> e.getName() , e -> e.getSubcategories().stream().map(e1 -> e1.getName()).collect(Collectors.toList())));
			
			getExtendTest().log(Status.INFO, "Actual categories:");
			getExtendTest().log(Status.INFO, resourceMapFromUrl.toString());
			
			
			assertTrue("missing categories ", resourceMapFromUrl.keySet().containsAll(resourcesListFromFile.keySet()));
			
			

			
			

	}
	
	
	public Map<String, List<CategoryDefinition>> getCategories() throws Exception {
		
		User defaultAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		
		Map<String,List<CategoryDefinition>> map = new HashMap<String,List<CategoryDefinition>>();
				
		
		RestResponse allResourceCategories = CategoryRestUtils.getAllCategories(defaultAdminUser, ComponentTypeEnum.RESOURCE_PARAM_NAME);
		RestResponse allServiceCategories = CategoryRestUtils.getAllCategories(defaultAdminUser, ComponentTypeEnum.SERVICE_PARAM_NAME);

		List<CategoryDefinition> parsedResourceCategories = ResponseParser.parseCategories(allResourceCategories);
		List<CategoryDefinition> parsedServiceCategories = ResponseParser.parseCategories(allServiceCategories);
		
		map.put(ComponentTypeEnum.RESOURCE_PARAM_NAME, parsedResourceCategories);
		map.put(ComponentTypeEnum.SERVICE_PARAM_NAME, parsedServiceCategories);
		
		return map;
	}
	


	@Test (enabled=false)
	public void pasrseCategoriesClass2() throws IOException{
		
		String path = "C:\\Git_work\\Git_UGN\\d2-sdnc\\catalog-be\\src\\main\\resources\\import\\tosca\\categories\\categoryTypes.yml";

		FileReader reader = new FileReader(path);
		Yaml yaml=new Yaml();
		
		
		Map<?, ?> map = (Map<?, ?>) yaml.load(reader);
		
		Collection<Map> values = (Collection<Map>) map.values();
		for (Map map2 : values) {
			Collection values2 = map2.values();
			for (Object object : values2) {
				
				
			}
		}
			
		{
			
		}
		List<Object> collect = values.stream().map(e -> e.get("name")).collect(Collectors.toList());
		
//		resourcesArrayList.stream().filter(s -> s.getName().toLowerCase().startsWith("ci") && !s.getName().toLowerCase().equals("cindervolume")).map(e -> e.getUniqueId()).collect(Collectors.toList()).forEach((i)
		
		
		
		
	}

}
