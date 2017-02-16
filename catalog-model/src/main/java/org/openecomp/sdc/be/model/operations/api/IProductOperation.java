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

import java.util.List;
import java.util.Set;

import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;

import fj.data.Either;

public interface IProductOperation extends IComponentOperation {
	public Either<List<Product>, StorageOperationStatus> getProductCatalogData(boolean inTransaction);

	public Either<Product, StorageOperationStatus> createProduct(Product product);

	public Either<Product, StorageOperationStatus> createProduct(Product product, boolean inTransaction);

	public Either<Product, StorageOperationStatus> deleteProduct(String productId, boolean inTransaction);

	public Either<List<Product>, StorageOperationStatus> getFollowed(String userId,
			Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction);

	public void rollback();

	public void commit();

}
