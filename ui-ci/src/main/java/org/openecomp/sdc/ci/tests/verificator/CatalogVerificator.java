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

package org.openecomp.sdc.ci.tests.verificator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.TypesEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.testng.Assert;
import org.testng.TestNGException;

import com.aventstack.extentreports.Status;

public class CatalogVerificator {
	
	public static int getResourceNumber(ResourceTypeEnum resourceType, Map<String, List<Component>> catalogAsMap) throws Exception {
		List<Component> resourcesArrayList = catalogAsMap.get("resources");
		return resourcesArrayList.stream().
				                  filter(s -> ((Resource)s).getResourceType().equals(resourceType)).
				                  collect(Collectors.toList()).size();
	}
	
	public static int getTypeNumber(TypesEnum enumtype) throws Exception{
		Map<String, List<Component>> catalogAsMap = RestCDUtils.getCatalogAsMap();
		switch (enumtype) {
		case RESOURCE:
			return catalogAsMap.get("resources").size();
		case SERVICE:
			return catalogAsMap.get("services").size();
		case PRODUCT:
			return catalogAsMap.get("products").size();
		default:
			return getResourceNumber(ResourceTypeEnum.valueOf(enumtype.name()), catalogAsMap);
		}		
	}
	
	public static void validateType(TypesEnum enumtype) throws Exception{		
		int numberOfElementsFromBE = getTypeNumber(enumtype);
		int numberOfElementsFromUI = getNumberOfElementsFromCatalogHeader();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating number of %s elements, should be %s ...", enumtype.name(), numberOfElementsFromBE));
		Assert.assertEquals(numberOfElementsFromBE, numberOfElementsFromUI, String.format("Expected : %s, Actual: %s", numberOfElementsFromBE, numberOfElementsFromUI));
	}
	
	public static int getStatusNumber(List<LifeCycleStateEnum> status) throws Exception {
		Map<String, List<Component>> catalogAsMap = RestCDUtils.getCatalogAsMap();
		return catalogAsMap.entrySet().stream().
				            map(s -> s.getValue()).
				            flatMap(List::stream).
				            filter(s -> status.contains(mapBeLifecycleToUIStatus(s))).
				            collect(Collectors.toList()).size();
	}
	
	public static void validateStatus(List<LifeCycleStateEnum> status, String checkboxName) throws Exception{
		int numberOfElementsFromBE = getStatusNumber(status);
		int numberOfElementsFromUI = getNumberOfElementsFromCatalogHeader();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating number of %s elements , should be %s ...", checkboxName, numberOfElementsFromBE));
		Assert.assertEquals(numberOfElementsFromBE, numberOfElementsFromUI, String.format("Expected : %s, Actual: %s", numberOfElementsFromBE, numberOfElementsFromUI));
	}
	
	public static int getCategoryNumber(String categoryName) throws Exception {
		Map<String, List<Component>> catalogAsMap = RestCDUtils.getCatalogAsMap();
		List<Component> serviceAndResourceList = new ArrayList<Component>();
		serviceAndResourceList.addAll(catalogAsMap.get("resources"));
		serviceAndResourceList.addAll(catalogAsMap.get("services"));
		return serviceAndResourceList.stream().
				                      filter(s -> s.getCategories().get(0).getName().equals(categoryName)).
				                      collect(Collectors.toList()).size();
	}
	
	public static void validateCategory(String categoryName) throws Exception{
		int numberOfElementsFromBE = getCategoryNumber(categoryName);
		int numberOfElementsFromUI = getNumberOfElementsFromCatalogHeader();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating number of %s category elements , should be %s ...", categoryName, numberOfElementsFromBE));
		Assert.assertEquals(numberOfElementsFromBE, numberOfElementsFromUI, String.format("Expected : %s, Actual: %s", numberOfElementsFromBE, numberOfElementsFromUI));
	}
	
	public static int getSubCategoryNumber(String categoryName , String subCategoryName) throws Exception {
		Map<String, List<Component>> catalogAsMap = RestCDUtils.getCatalogAsMap();
		List<Component> resourcesArrayList = catalogAsMap.get("resources");
		return resourcesArrayList.stream().
				                  filter(s -> s.getCategories().get(0).getName().equals(categoryName) &&
				                              s.getCategories().get(0).getSubcategories().get(0).getName().equals(subCategoryName)).
				                  collect(Collectors.toList()).size(); 
	}
	
	public static void validateSubCategory(String categoryName, String subCategoryName) throws Exception{
		int numberOfElementsFromBE = getSubCategoryNumber(categoryName, subCategoryName);
		int numberOfElementsFromUI = getNumberOfElementsFromCatalogHeader();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating number of %s/%s subcategory elements , should be %s ...", categoryName, subCategoryName, numberOfElementsFromBE));
		Assert.assertEquals(numberOfElementsFromBE, numberOfElementsFromUI, String.format("Expected : %s, Actual: %s", numberOfElementsFromBE, numberOfElementsFromUI));
	}
	
	public static int getNumberOfElementsFromCatalogHeader(){
		String elementsAsString = GeneralUIUtils.getWebElementByClassName("w-sdc-dashboard-catalog-header").getText();
		String numberOfElementsAsString = elementsAsString.split(" ")[0];
		if (numberOfElementsAsString.equals("No")){
			return 0;
		} else {
			return Integer.parseInt(numberOfElementsAsString);
		}		
	}
	
	private static LifeCycleStateEnum mapBeLifecycleToUIStatus(Component component){
		boolean isServiceAndDistributed = component.getComponentType().equals(ComponentTypeEnum.SERVICE) && 
				                          ((Service) component).getDistributionStatus().equals(DistributionStatusEnum.DISTRIBUTED);
		switch (component.getLifecycleState()) {
		case CERTIFIED:
			if (isServiceAndDistributed){
				return LifeCycleStateEnum.DISTRIBUTED;
			} else {
			    return LifeCycleStateEnum.CERTIFIED;
			}
		case READY_FOR_CERTIFICATION:
			return LifeCycleStateEnum.READY_FOR_TESTING;
		case CERTIFICATION_IN_PROGRESS:
			return LifeCycleStateEnum.IN_TESTING;
		case NOT_CERTIFIED_CHECKIN:
			return LifeCycleStateEnum.CHECKIN;  //to IN DESIGN
		case NOT_CERTIFIED_CHECKOUT:
			return LifeCycleStateEnum.CHECKOUT;	//to IN DESIGN
		default:
	        throw new TestNGException("Missing enum value in enum converter");
		}
	}
		
}
