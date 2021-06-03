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

import fj.data.Either;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.PropertyDefinition;

public interface IPropertyOperation {

    /**
     * Delete all properties of resource
     *
     * @param nodeType
     * @param uniqueId
     * @return
     */
    public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllPropertiesAssociatedToNode(NodeTypeEnum nodeType,
                                                                                                               String uniqueId);

    /**
     * same as deleteAllPropertiesAssociatedToNode but returns empty map if node has no properties
     *
     * @param nodeType
     * @param uniqueId
     * @return
     */
    Either<Map<String, PropertyDefinition>, StorageOperationStatus> deletePropertiesAssociatedToNode(NodeTypeEnum nodeType, String uniqueId);

    public boolean isPropertyDefaultValueValid(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes);

    public boolean isPropertyTypeValid(IComplexDefaultValue propertyDefinition, String model);

    public ImmutablePair<String, Boolean> isPropertyInnerTypeValid(IComplexDefaultValue propertyDefinition,
                                                                   Map<String, DataTypeDefinition> dataTypes);

    /**
     * @param dataTypeDefinition
     * @return
     */
    public Either<DataTypeDefinition, StorageOperationStatus> addDataType(DataTypeDefinition dataTypeDefinition);

    /**
     * @param name
     * @return
     */
    public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name, String validForModel);

    public Either<DataTypeDefinition, StorageOperationStatus> getDataTypeByName(String name, String validForModel, boolean inTransaction);

    public StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes);

    public Either<DataTypeDefinition, StorageOperationStatus> updateDataType(DataTypeDefinition newDataTypeDefinition,
                                                                             DataTypeDefinition oldDataTypeDefinition);
}
