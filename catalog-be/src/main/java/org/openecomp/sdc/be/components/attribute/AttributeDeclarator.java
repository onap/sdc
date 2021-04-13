/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.components.attribute;

import fj.data.Either;
import java.util.List;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceAttribOutput;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public interface AttributeDeclarator {

    /**
     * creates a list of outputs from the given list of attributes and updates the attributes accordingly
     *
     * @param component         the container
     * @param propertiesOwnerId the id of the owner of the attributes to declare (e.g ComponentInstance, Policy, Group etc)
     * @param attribsToDeclare  the list of attributes that are being declared as outputs
     * @return the list of outputs that were created from the given attributes
     */
    Either<List<OutputDefinition>, StorageOperationStatus> declareAttributesAsOutputs(final Component component,
                                                                                      final String propertiesOwnerId,
                                                                                      final List<ComponentInstanceAttribOutput> attribsToDeclare);

    /**
     * returns the values of declared attributes to each original state before it was declared as an input. this function is to be called when an
     * input, that was created by declaring a property, is deleted.
     *
     * @param component the container of the input to be deleted
     * @param output    the input to be deleted
     */
    StorageOperationStatus unDeclareAttributesAsOutputs(final Component component, final OutputDefinition output);
}
