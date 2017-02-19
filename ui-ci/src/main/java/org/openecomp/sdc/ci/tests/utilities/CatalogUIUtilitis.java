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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.testng.annotations.Test;

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

}
