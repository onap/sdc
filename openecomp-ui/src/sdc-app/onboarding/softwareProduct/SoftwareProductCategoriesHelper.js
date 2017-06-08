/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
export default {

	getCurrentCategoryOfSubCategory(selectedSubCategory, softwareProductCategories) {
		let category, subCategory;
		for (var i = 0; i < softwareProductCategories.length; i++) {
			let {subcategories = []} = softwareProductCategories[i];
			subCategory = subcategories.find(sub => sub.uniqueId === selectedSubCategory);
			if (subCategory) {
				category = softwareProductCategories[i].uniqueId;
				break;
			}
		}
		return category;
	}
};
