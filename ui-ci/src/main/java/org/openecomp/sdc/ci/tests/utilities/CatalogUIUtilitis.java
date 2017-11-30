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

package org.openecomp.sdc.ci.tests.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openecomp.sdc.ci.tests.datatypes.CheckBoxStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.TopMenuButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.TypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class CatalogUIUtilitis {
	
	
	
	

	// Get all Categories , Subcategories and Icons.
	public void getAllCategoriesAndSubcategories() throws IOException, JSONException {
		RestResponse allcategoriesJson = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();
		JSONArray categories = new JSONArray(allcategoriesJson.getResponse());
		for (int i = 0; i < categories.length(); i++) {
			String categoryname = (String) categories.getJSONObject(i).get("name");
			JSONArray subcategories = (JSONArray) categories.getJSONObject(i).get("subcategories");
			for (int j = 0; j < subcategories.length(); j++) {
				String subcategoryname = (String) subcategories.getJSONObject(j).get("name");
				System.out.println(subcategoryname);
			}
			for (int j = 0; j < subcategories.length(); j++) {
				JSONArray icons = (JSONArray) subcategories.getJSONObject(j).get("icons");
				for (int k = 0; k < icons.length(); k++) {
					System.out.println(icons.get(k));
				}
			}
			System.out.println("-------------------------------");
		}
	}

	@Test
	// FOr testing---delete.
	public static List<String> abcd() throws IOException, JSONException {
		RestResponse allcategoriesJson = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();
		JSONArray categories = new JSONArray(allcategoriesJson.getResponse());
		List<String> allcat = new ArrayList<>();
		String uniqueId = null;
		for (int i = 0; i < categories.length(); i++) {
			String categoryname = (String) categories.getJSONObject(i).get("name");
			uniqueId = (String) categories.getJSONObject(i).get("uniqueId");
			allcat.add(uniqueId);
			JSONArray subcategories = (JSONArray) categories.getJSONObject(i).get("subcategories");
			for (int j = 0; j < subcategories.length(); j++) {
				String subcategoryname = (String) subcategories.getJSONObject(j).get("name");
				uniqueId = (String) subcategories.getJSONObject(j).get("uniqueId");
				allcat.add(uniqueId);
			}
		}
		return allcat;

	}
	
	public static void clickTopMenuButton(TopMenuButtonsEnum button) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s button ...", button.name()));
		switch (button) {
		case CATALOG:
			GeneralUIUtils.getWebElementByTestID(button.getButton()).click();
			break;
		case HOME:
			GeneralUIUtils.getWebElementByTestID(button.getButton()).click();
			break;
		case ON_BOARDING:
			GeneralUIUtils.getWebElementByTestID(button.getButton()).click();
			break;
		default:
			break;
		}
		GeneralUIUtils.ultimateWait();
	}
	
	public static String catalogFilterTypeChecBox(TypesEnum enumtype) throws Exception {
		String Type = enumtype.toString().toLowerCase();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s ...", Type));
		GeneralUIUtils.getWebElementByTestID(enumtype.getValue()).click();
		return Type;
	}
	
	public static List<String> catalogFilterStatusChecBox(CheckBoxStatusEnum statusEnum) throws Exception {
		List<String> status = null;
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s status", statusEnum.name()));
		switch (statusEnum) {
		case IN_DESIGN:
			status = Arrays.asList("NOT_CERTIFIED_CHECKIN", "NOT_CERTIFIED_CHECKOUT");
			GeneralUIUtils.getWebElementByTestID(statusEnum.getCatalogValue()).click();
			break;
		case READY_FOR_TESTING:
			status = Arrays.asList("READY_FOR_CERTIFICATION");
			GeneralUIUtils.getWebElementByTestID(statusEnum.getCatalogValue()).click();
			break;
		case IN_TESTING:
			status = Arrays.asList("CERTIFICATION_IN_PROGRESS");
			GeneralUIUtils.getWebElementByTestID(statusEnum.getCatalogValue()).click();
			break;
		case CERTIFIED:
			status = Arrays.asList("CERTIFIED");
			GeneralUIUtils.getWebElementByTestID(statusEnum.getCatalogValue()).click();
			break;
		case DISTRIBUTED:
			status = Arrays.asList("CERTIFIED");
			GeneralUIUtils.getWebElementByTestID(statusEnum.getCatalogValue()).click();
			break;
		}
		return status;
	}

	// Get all Categories uniqueID .//The parent categories.
	public static List<String> getCategories() throws IOException, JSONException {
		List<String> allCategoriesList = new ArrayList<>();
		RestResponse allcategoriesJson = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();
		JSONArray categories = new JSONArray(allcategoriesJson.getResponse());
		for (int i = 0; i < categories.length(); i++) {
			String categoryname = (String) categories.getJSONObject(i).get("name");
			System.out.println(categoryname);
			allCategoriesList.add(categoryname);
		}
		return allCategoriesList;
	}

	@Test
	// Get Subcategories by Category name
	public static List<String> getAllSubcategoriesByUniqueId(String uniqueId) throws IOException, JSONException {

		RestResponse allcategoriesJson = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();
		JSONArray categories = new JSONArray(allcategoriesJson.getResponse());
		List<String> subCategories = new ArrayList<>();// subCategories to
														// return.
		JSONArray subcategories = null;

		for (int i = 0; i < categories.length(); i++) {

			String categoryuniqueId = (String) categories.getJSONObject(i).get("uniqueId");

			if (categoryuniqueId.contentEquals(uniqueId)) {
				subcategories = (JSONArray) categories.getJSONObject(i).get("subcategories");

				for (int j = 0; j < subcategories.length(); j++) {

					subCategories.add((String) subcategories.getJSONObject(j).get("uniqueId"));
				}

				break;
			}
		}
		if (subcategories == null) {
			subCategories.add(uniqueId);
		}
		return subCategories;
	}

	@Test
	// Get icons by category name
	public void getSubCategoryIcons() throws IOException, JSONException {
		RestResponse allcategoriesJson = CatalogRestUtils.getAllCategoriesTowardsCatalogBe();

		JSONArray categories = new JSONArray(allcategoriesJson.getResponse());
		for (int i = 0; i < categories.length(); i++) {
			String subcategoryname = (String) categories.getJSONObject(i).get("name");
			if (subcategoryname.contentEquals("Generic")) {
				JSONArray subcategories = (JSONArray) categories.getJSONObject(i).get("subcategories");
				for (int j = 0; j < subcategories.length(); j++) {
					JSONArray icons = (JSONArray) subcategories.getJSONObject(j).get("icons");
					for (int k = 0; k < icons.length(); k++) {
						System.out.println(icons.get(k));
					}
				}
				break;
			}
		}
	}
	
	
	public static WebElement clickOnUpperCategoryCheckbox() throws InterruptedException {
		List<WebElement> categorieCheckboxes = GeneralUIUtils.getElementsByCSS("span[data-tests-id*='category']"); // get all categories and subcategories
		WebElement categorieCheckbox = categorieCheckboxes.get(0);
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s category ...", categorieCheckbox.getText()));
		categorieCheckbox.click();
		GeneralUIUtils.ultimateWait();
		return categorieCheckbox;
	}

	public static void clickOnLeftPanelElement(DataTestIdEnum.CatalogPageLeftPanelFilterTitle leftPanelElement) throws InterruptedException {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", leftPanelElement.name()));
		GeneralUIUtils.getElementsByCSS(leftPanelElement.getValue()).forEach(WebElement::click);
	}

	public static WebElement catalogSearchBox(String searchText) {
		WebElement searchBox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
		searchBox.clear();
		searchBox.sendKeys(searchText);
		return searchBox;
	}

}
