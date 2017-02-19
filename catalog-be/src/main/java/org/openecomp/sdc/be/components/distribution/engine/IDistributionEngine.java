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

package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

public interface IDistributionEngine {

	public boolean isActive();

	public StorageOperationStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envName, String userId, String modifierName);

	public StorageOperationStatus isEnvironmentAvailable(String envName);

	/**
	 * Currently, it used for tests. For real implementation we need cancel the initialization task and the polling task.
	 * 
	 * @param envName
	 */
	public void disableEnvironment(String envName);

	public Either<INotificationData, StorageOperationStatus> isReadyForDistribution(Service service, String distributionId, String envName);

}
