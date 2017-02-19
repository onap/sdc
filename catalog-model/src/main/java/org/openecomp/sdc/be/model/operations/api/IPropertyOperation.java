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

package org.openecomp.sdc.be.model.operations.api;

import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyDefinition;

import fj.data.Either;

public interface IPropertyOperation {

	/**
	 * add property to resource
	 * 
	 * @param propertyName
	 * @param propertyDefinition
	 * @param nodeType
	 * @param id
	 * @return
	 * 
	 * 		public Either<PropertyDefinition, StorageOperationStatus>
	 *         addPropertyToResource( String propertyName, PropertyDefinition
	 *         propertyDefinition, NodeTypeEnum nodeType, String id);
	 */

	/**
	 * get property belongs to resource
	 * 
	 * @param propertyName
	 *            - property name
	 * @param resourceId
	 *            - resource unique id
	 * @return
	 */
	public Either<PropertyDefinition, StorageOperationStatus> getPropertyOfResource(String propertyName,
			String resourceId);

	/**
	 * Delete all properties of resource
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @return
	 */
	public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllPropertiesAssociatedToNode(
			NodeTypeEnum nodeType, String uniqueId);

	public boolean isPropertyDefaultValueValid(IComplexDefaultValue propertyDefinition,
			Map<String, DataTypeDefinition> dataTypes);

	public boolean isPropertyTypeValid(IComplexDefaultValue propertyDefinition);

	public ImmutablePair<String, Boolean> isPropertyInnerTypeValid(IComplexDefaultValue propertyDefinition,
			Map<String, DataTypeDefinition> dataTypes);

	/**
	 * @param dataTypeDefinition
	 * @return
	 */
	public Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition);

	public Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition,
			boolean inTransaction);

	/**
	 * @param name
	 * @return
	 */
	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name);

	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name, boolean inTransaction);

	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByNameWithoutDerived(String name,
			boolean inTransaction);

	public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByNameWithoutDerived(String name);

	public StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition,
			Map<String, DataTypeDefinition> dataTypes);

	public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition,
			DataTypeDefinition oldDataTypeDefinition, boolean inTransaction);

	public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition,
			DataTypeDefinition oldDataTypeDefinition);

}
