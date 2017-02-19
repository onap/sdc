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

package org.openecomp.sdc.be.datamodel.utils;

import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class NodeTypeConvertUtils {
	public static NodeTypeEnum getCategoryNodeTypeByComponentParam(ComponentTypeEnum componentTypeEnum, CategoryTypeEnum categoryType) {
		NodeTypeEnum res = null;
		if (componentTypeEnum != null) {
			switch (componentTypeEnum) {
			case SERVICE:
				switch (categoryType) {
				case CATEGORY:
					res = NodeTypeEnum.ServiceNewCategory;
					break;

				default:
					// doesn't support subcategories or grouping
					break;
				}
				break;
			case RESOURCE:
				switch (categoryType) {
				case CATEGORY:
					res = NodeTypeEnum.ResourceNewCategory;
					break;
				case SUBCATEGORY:
					res = NodeTypeEnum.ResourceSubcategory;
					break;
				default:
					// doesn't support grouping
					break;
				}
				break;
			case PRODUCT:
				switch (categoryType) {
				case CATEGORY:
					res = NodeTypeEnum.ProductCategory;
					break;
				case SUBCATEGORY:
					res = NodeTypeEnum.ProductSubcategory;
					break;
				case GROUPING:
					res = NodeTypeEnum.ProductGrouping;
					break;
				}
				break;
			default:
				break;
			}
		}
		return res;
	}

}
