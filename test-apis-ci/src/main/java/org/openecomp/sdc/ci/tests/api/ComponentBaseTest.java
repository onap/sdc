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

package org.openecomp.sdc.ci.tests.api;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.cassandra.CassandraUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public abstract class ComponentBaseTest {

//	private static Logger logger = LoggerFactory.getLogger(ComponentBaseTest.class.getName());
	
	protected static Logger logger= Logger.getLogger(ComponentBaseTest.class);	
	

//	 public ComponentBaseTest(TestName testName, String className) {
//	 super(testName, className);
//	 }

	protected static final String REPORT_FOLDER = "." + File.separator + "ExtentReport" + File.separator;
	private static final String VERSIONS_INFO_FILE_NAME = "versions.info";
	private static final String REPORT_FILE_NAME = "SDC_CI_Extent_Report.html";
	protected static TitanGraph titanGraph;
	public static Config config;
	protected static ITestContext myContext;
	
	
	
	/**************** METHODS ****************/
	public static ExtentTest getExtendTest() {
		SomeInterface testManager = new ExtentTestManager(); 
		return testManager.getTest();
	}

	public static enum ComponentOperationEnum {
		CREATE_COMPONENT, UPDATE_COMPONENT, GET_COMPONENT, DELETE_COMPONENT, CHANGE_STATE_CHECKIN, CHANGE_STATE_CHECKOUT, CHANGE_STATE_UNDO_CHECKOUT
	};

	public ComponentBaseTest(TestName name, String name2) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger("com.thinkaurelius").setLevel(Level.INFO);
		lc.getLogger("com.datastax").setLevel(Level.INFO);
		lc.getLogger("io.netty").setLevel(Level.INFO);
		lc.getLogger("c.d").setLevel(Level.INFO);
	}
	
	public static String getReportFolder() {
		return REPORT_FOLDER;
	}

	@BeforeSuite(alwaysRun = true)
	public void setupBeforeSuite(ITestContext context) throws Exception {
		config = Utils.getConfig();
		myContext=context;
		ExtentManager.initReporter(getReportFolder(), REPORT_FILE_NAME, context);	
		AtomicOperationUtils.createDefaultConsumer(true);
		openTitanLogic();
		performClean();
		
		

	}
	
	@BeforeMethod(alwaysRun = true)
	public void setupBeforeTest(java.lang.reflect.Method method, ITestContext context) throws Exception {
		
		System.out.println("ExtentReport instance started from BeforeMethod...");
		String suiteName = ExtentManager.getSuiteName(context);
		ExtentTestManager.startTest(method.getName());
		ExtentTestManager.assignCategory(this.getClass());
      
	}
	
	@AfterMethod(alwaysRun = true)
	public void quitAfterTest(ITestResult result, ITestContext context) throws Exception {

		String testName = result.getName();
		Throwable throwable = result.getThrowable();
		int status = result.getStatus();
		
		switch(status){
		case ITestResult.SUCCESS:				
			getExtendTest().log(Status.PASS, "Test Result : <span class='label success'>Success</span>");
			break;
				
		case ITestResult.FAILURE:
			getExtendTest().log(Status.ERROR, "ERROR - The following exepction occured");
			getExtendTest().log(Status.ERROR, result.getThrowable());
			getExtendTest().log(Status.FAIL, "<span class='label failure'>Failure</span>");
			break;
			
		case ITestResult.SKIP:
			getExtendTest().log(Status.SKIP, "SKIP - The following exepction occured");
			break;
		default:
			break;
		}


	   	ExtentTestManager.endTest();
    	
		
	}

	@AfterClass(alwaysRun = true)
	public synchronized static void cleanAfterClass() throws Exception{

//		System.out.println("<<<<<<<<class name>>>>>"+method.getDeclaringClass());
//		System.out.println("<<<<<<<<class name>>>>>"+method.getName());


		System.out.println("delete components AfterClass");
		deleteCreatedComponents(getCatalogAsMap());
//		extentReport.flush();

	}
	
	@AfterSuite(alwaysRun = true)
	public static void shutdownTitan() throws Exception {
		
		performClean();
		shutdownTitanLogic();

	}

	protected static void openTitanLogic() throws Exception {
	
		logger.trace(config.toString());
		String titanConfigFilePath = config.getTitanPropertiesFile();
		titanGraph = TitanFactory.open(titanConfigFilePath);
		assertNotNull(titanGraph);
		
	}


	protected static void shutdownTitanLogic() {
		if (titanGraph.isOpen()) {
			titanGraph.close();
		}
		CassandraUtils.close();
	}

	

	public void setLog(String fromDataProvider) {

		String suiteName = ExtentManager.getSuiteName(myContext);
		ExtentTestManager.startTest(Thread.currentThread().getStackTrace()[2].getMethodName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fromDataProvider);
		ExtentTestManager.assignCategory(this.getClass());

	}

	protected static void performClean() throws Exception, FileNotFoundException {
//		cleanComponents();
		if(!config.getSystemUnderDebug()){
			deleteCreatedComponents(getCatalogAsMap());
			CassandraUtils.truncateAllKeyspaces();
		}else{
			System.out.println("Accordindig to configuration components will not be deleted, in case to unable option to delete, please change systemUnderDebug parameter value to false ...");
		}
	}

	public void verifyErrorCode(RestResponse response, String action, int expectedCode) {
		assertNotNull("check response object is not null after " + action, response);
		assertNotNull("check error code exists in response after " + action, response.getErrorCode());
		assertEquals("Check response code after  + action" + action, expectedCode, response.getErrorCode().intValue());
	}

	private static void cleanComponents() throws Exception {

		// Components to delete
		List<String> vfResourcesToDelete = new ArrayList<String>();
		List<String> nonVfResourcesToDelete = new ArrayList<String>();
		List<String> servicesToDelete = new ArrayList<String>();
		List<String> productsToDelete = new ArrayList<String>();

		// Categories to delete
		List<ImmutableTriple<String, String, String>> productGroupingsToDelete = new ArrayList<>();
		List<ImmutablePair<String, String>> productSubsToDelete = new ArrayList<>();
		List<ImmutablePair<String, String>> resourceSubsToDelete = new ArrayList<>();
		List<String> productCategoriesToDelete = new ArrayList<>();
		List<String> resourceCategoriesToDelete = new ArrayList<String>();
		List<String> serviceCategoriesToDelete = new ArrayList<String>();

		List<String> resourcesNotToDelete = config.getResourcesNotToDelete();
		List<String> resourceCategoriesNotToDelete = config.getResourceCategoriesNotToDelete();
		List<String> serviceCategoriesNotToDelete = config.getServiceCategoriesNotToDelete();

		Iterable<TitanVertex> vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Resource.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex vertex = iter.next();
				Boolean isAbstract = vertex.value(GraphPropertiesDictionary.IS_ABSTRACT.getProperty());
				// if (!isAbstract) {
				String name = vertex.value(GraphPropertiesDictionary.NAME.getProperty());
				String version = vertex.value(GraphPropertiesDictionary.VERSION.getProperty());

				if ((resourcesNotToDelete != null && !resourcesNotToDelete.contains(name)) || (version != null && !version.equals("1.0"))) {
					String id = vertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					String resourceType = vertex.value(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty());
					if (name.startsWith("ci")) {
						if (resourceType.equals(ResourceTypeEnum.VF.name())) {
							vfResourcesToDelete.add(id);
						} else {
							nonVfResourcesToDelete.add(id);
						}
					}
				} else if ((resourcesNotToDelete != null && !resourcesNotToDelete.contains(name)) || (version != null && version.equals("1.0"))) {
					if ((boolean) vertex.value(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty()) == false) {
						vertex.property(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
					}
				}
				// }
			}
		}
		vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Service.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex vertex = iter.next();
				String id = vertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				String name = vertex.value(GraphPropertiesDictionary.NAME.getProperty());
				if (name.startsWith("ci")) {
					servicesToDelete.add(id);
				}
			}
		}

		vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Product.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex vertex = iter.next();
				String id = vertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				String name = vertex.value(GraphPropertiesDictionary.NAME.getProperty());
				if (name.startsWith("Ci")) {
					productsToDelete.add(id);
				}
			}
		}

		// Getting categories

		vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.ResourceNewCategory.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex category = iter.next();
				String name = category.value(GraphPropertiesDictionary.NAME.getProperty());
				if (!resourceCategoriesNotToDelete.contains(name)) {
					String catId = category.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					resourceCategoriesToDelete.add(catId);
					Iterator<Vertex> subs = category.vertices(Direction.OUT, GraphEdgeLabels.SUB_CATEGORY.getProperty());
					while (subs.hasNext()) {
						Vertex sub = subs.next();
						String subCatId = sub.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
						resourceSubsToDelete.add(new ImmutablePair<String, String>(catId, subCatId));
					}
				}
			}
		}

		vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.ServiceNewCategory.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex category = iter.next();
				String name = category.value(GraphPropertiesDictionary.NAME.getProperty());
				if (!serviceCategoriesNotToDelete.contains(name)) {
					String id = category.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					serviceCategoriesToDelete.add(id);
				}
			}
		}

		vertices = titanGraph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.ProductCategory.getName()).vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iter = vertices.iterator();
			while (iter.hasNext()) {
				Vertex category = iter.next();
				String catId = category.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				productCategoriesToDelete.add(catId);
				Iterator<Vertex> subs = category.vertices(Direction.OUT, GraphEdgeLabels.SUB_CATEGORY.getProperty());
				while (subs.hasNext()) {
					Vertex sub = subs.next();
					String subCatId = sub.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					productSubsToDelete.add(new ImmutablePair<String, String>(catId, subCatId));
					Iterator<Vertex> groupings = sub.vertices(Direction.OUT, GraphEdgeLabels.GROUPING.getProperty());
					while (groupings.hasNext()) {
						Vertex grouping = groupings.next();
						String groupId = grouping.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
						productGroupingsToDelete.add(new ImmutableTriple<String, String, String>(catId, subCatId, groupId));
					}
				}

			}
		}

		titanGraph.tx().commit();

		String adminId = UserRoleEnum.ADMIN.getUserId();
	/*	String productStrategistId = UserRoleEnum.PRODUCT_STRATEGIST1.getUserId();*/

		// Component delete
		
/*		for (String id : productsToDelete) {
			RestResponse deleteProduct = ProductRestUtils.deleteProduct(id, productStrategistId);

		}*/
		for (String id : servicesToDelete) {
			RestResponse deleteServiceById = ServiceRestUtils.deleteServiceById(id, adminId);

		}
		for (String id : vfResourcesToDelete) {
			RestResponse deleteResource = ResourceRestUtils.deleteResource(id, adminId);

		}

		for (String id : nonVfResourcesToDelete) {
			RestResponse deleteResource = ResourceRestUtils.deleteResource(id, adminId);

		}

		// Categories delete - product
		/*String componentType = BaseRestUtils.PRODUCT_COMPONENT_TYPE;
		for (ImmutableTriple<String, String, String> triple : productGroupingsToDelete) {
			CategoryRestUtils.deleteGrouping(triple.getRight(), triple.getMiddle(), triple.getLeft(), productStrategistId, componentType);
		}
		for (ImmutablePair<String, String> pair : productSubsToDelete) {
			CategoryRestUtils.deleteSubCategory(pair.getRight(), pair.getLeft(), productStrategistId, componentType);
		}
		for (String id : productCategoriesToDelete) {
			CategoryRestUtils.deleteCategory(id, productStrategistId, componentType);
		}*/

		// Categories delete - resource
		String componentType = BaseRestUtils.RESOURCE_COMPONENT_TYPE;
		for (ImmutablePair<String, String> pair : resourceSubsToDelete) {
			CategoryRestUtils.deleteSubCategory(pair.getRight(), pair.getLeft(), adminId, componentType);
		}
		for (String id : resourceCategoriesToDelete) {
			CategoryRestUtils.deleteCategory(id, adminId, componentType);
		}
		// Categories delete - resource
		componentType = BaseRestUtils.SERVICE_COMPONENT_TYPE;
		for (String id : serviceCategoriesToDelete) {
			CategoryRestUtils.deleteCategory(id, adminId, componentType);
		}

	}

	private static void deleteCreatedComponents(Map<String, List<Component>> convertCatalogResponseToJavaObject) throws IOException {
		final String userId = UserRoleEnum.DESIGNER.getUserId();
		
		List<Component> resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.PRODUCT_PARAM_NAME);
		if (resourcesArrayList.size() > 0) {
			List<String> collect = buildCollectionUniqueId(resourcesArrayList);
			for (String uId : collect) {
				ProductRestUtils.deleteProduct(uId, userId);
			}
		}
		
		
		resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.SERVICE_PARAM_NAME);
		if (resourcesArrayList.size() > 0) {
			List<String> collect = buildCollectionUniqueId(resourcesArrayList);
			for (String uId : collect) {
				ServiceRestUtils.markServiceToDelete(uId, userId);
			}
			ServiceRestUtils.deleteMarkedServices(userId);
		}

		
		resourcesArrayList = convertCatalogResponseToJavaObject.get(ComponentTypeEnum.RESOURCE_PARAM_NAME);

		// List<String> collect = resourcesArrayList.stream().filter(s ->
		// s.getName().startsWith("ci")).map(e ->
		// e.getUniqueId()).collect(Collectors.toList());

		// List<Map<String, String>> collect =
		// resourcesArrayList.stream().filter(s ->
		// s.getName().startsWith("ci")).map(e ->
		// e.getAllVersions()).collect(Collectors.toList());
		/*
		 * List<String> collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith("ci")) .flatMap(e -> e.getAllVersions().values().stream()).collect(Collectors.toList());
		 */

		if (!CollectionUtils.isEmpty(resourcesArrayList)) {
			List<String> collect = buildCollectionUniqueId(resourcesArrayList);
			for (String uId : collect) {
				ResourceRestUtils.markResourceToDelete(uId, userId);
			}
			ResourceRestUtils.deleteMarkedResources(userId);
		}

	

	}

	private void deleteCollection(List<Component> componentArrayList, Consumer<String> deleteHandler) {

		if (componentArrayList.size() > 0) {
			List<String> collect = buildCollectionUniqueId(componentArrayList);
			for (String uId : collect) {
				deleteHandler.accept(uId);
				// ProductRestUtils.deleteProduct(uId, userId);
			}
		}
	}

	protected static List<String> buildCollectionUniqueId(List<Component> resourcesArrayList) {

		// Stream<String> flatMap = resourcesArrayList.stream().filter(s ->
		// s.getName().startsWith("ci")).map(e -> e.getAllVersions()).map( e ->
		// e.values()).flatMap( e -> e.stream());

		// List<String> collect = resourcesArrayList.stream()
		// //
		// .filter(s -> s.getName().startsWith("ci") )
		// //
		// .map(e -> e.getUniqueId())

		// .map( e -> e.values())
		// .filter(out -> out!=null )
		// .flatMap( e -> e.stream())
		// .collect(Collectors.toList());

		// List<String> collect = resourcesArrayList.stream().filter(s ->
		// s.getName().startsWith("ci"))
		// .flatMap(e ->
		// e.getAllVersions().values().stream()).collect(Collectors.toList());
		ComponentTypeEnum componentTypeEnum = resourcesArrayList.get(0).getComponentType();
 
		List<String> genericCollection = new ArrayList<String>();
		
		resourcesArrayList.stream().filter(s -> s.getName().toLowerCase().startsWith("ci") && !s.getName().toLowerCase().equals("cindervolume")).map(e -> e.getUniqueId()).collect(Collectors.toList()).forEach((i) -> {
			buildCollectionBaseOnComponentType(componentTypeEnum, genericCollection, i);
		});
		
	
		//

		// List<String> collect =
		// genericCollection.stream().collect(Collectors.toList());

		return genericCollection;
	}

	public static void buildCollectionBaseOnComponentType(ComponentTypeEnum componentTypeEnum,
			List<String> genericCollection, String i) {
		try {
			switch (componentTypeEnum) {
			case RESOURCE:
				RestResponse resource = ResourceRestUtils.getResource(i);
				Resource convertResourceResponseToJavaObject = ResponseParser.convertResourceResponseToJavaObject(resource.getResponse());
				Map<String, String> allVersions = convertResourceResponseToJavaObject.getAllVersions();
				Collection<String> values = allVersions.values();
				genericCollection.addAll(values);
					
				break;
			case SERVICE:
				RestResponse service = ServiceRestUtils.getService(i);
				Service convertServiceResponseToJavaObject = ResponseParser.convertServiceResponseToJavaObject(service.getResponse());
				allVersions = convertServiceResponseToJavaObject.getAllVersions();
				values = allVersions.values();
				genericCollection.addAll(values);

				break;
					

			case PRODUCT:
				RestResponse product = ProductRestUtils.getProduct(i);
				Product convertProductResponseToJavaObject = ResponseParser.convertProductResponseToJavaObject(product.getResponse());
				allVersions = convertProductResponseToJavaObject.getAllVersions();
				values = allVersions.values();
				genericCollection.addAll(values);

				break;

			// default:
			// break;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected static Map<String, List<Component>> getCatalogAsMap() throws Exception {
		RestResponse catalog = CatalogRestUtils.getCatalog(UserRoleEnum.DESIGNER.getUserId());
		Map<String, List<Component>> convertCatalogResponseToJavaObject = ResponseParser.convertCatalogResponseToJavaObject(catalog.getResponse());
		return convertCatalogResponseToJavaObject;
	}
	protected Resource createVfFromCSAR(User sdncModifierDetails, String csarId) throws Exception {
		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();

		resourceDetails.setCsarUUID(csarId);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource createdResource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());
		return createdResource;
	}
	


}
