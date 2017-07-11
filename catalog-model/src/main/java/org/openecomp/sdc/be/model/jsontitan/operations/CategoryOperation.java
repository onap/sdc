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

package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("category-operation")
public class CategoryOperation extends BaseOperation{
	
	private static Logger log = LoggerFactory.getLogger(CategoryOperation.class.getName());

	/** 
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public  Either<GraphVertex, StorageOperationStatus> getCategory(String name, VertexTypeEnum type) {
		if (name != null) {
			String categoryUid = UniqueIdBuilder.buildComponentCategoryUid(name, type);
			Map<GraphPropertyEnum, Object> props = new HashMap<>();
			props.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(name));
			Either<List<GraphVertex>, TitanOperationStatus> either = titanDao.getByCriteria(type, props);

			if (either.isRight()) {
				TitanOperationStatus titanOperationStatus = either.right().value();
				log.debug("Problem while geting category with id {}. reason - {}", categoryUid, titanOperationStatus.name());
				if (titanOperationStatus == TitanOperationStatus.NOT_FOUND) {
					return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
				} else {
					return Either.right(StorageOperationStatus.GENERAL_ERROR);
				}
			}
			return Either.left(either.left().value().get(0));
		} else {
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
	}
	/** 
	 * 
	 * @param categoryV
	 * @param name
	 * @return
	 */
	public  Either<GraphVertex, StorageOperationStatus> getSubCategoryForCategory(GraphVertex categoryV, String name ) {
		Either<List<GraphVertex>, TitanOperationStatus> childrenVertecies = titanDao.getChildrenVertecies(categoryV, EdgeLabelEnum.SUB_CATEGORY, JsonParseFlagEnum.NoParse);
		if ( childrenVertecies.isRight() ){
			log.debug("Failed to fetch children verticies for category {} error {}", categoryV.getUniqueId(), childrenVertecies.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(childrenVertecies.right().value()));
		}
		for ( GraphVertex childV : childrenVertecies.left().value() ){
			if ( childV.getMetadataProperty(GraphPropertyEnum.NAME).equals(name) ){
				return Either.left(childV);
			}
		}
		return Either.right(StorageOperationStatus.NOT_FOUND);
	}
}
