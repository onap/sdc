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

package org.openecomp.sdc.be.model.cache;

import org.openecomp.sdc.be.model.operations.api.IProductOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;

public class DaoInfo {
	private IResourceOperation iResourceOperation;
	private IServiceOperation iServiceOperation;
	private IProductOperation iProductOperation;
	private ComponentCache ComponentCache;

	public DaoInfo(IResourceOperation iResourceOperation, IServiceOperation iServiceOperation,
			IProductOperation iProductOperation, org.openecomp.sdc.be.model.cache.ComponentCache componentCache) {
		this.iResourceOperation = iResourceOperation;
		this.iServiceOperation = iServiceOperation;
		this.iProductOperation = iProductOperation;
		ComponentCache = componentCache;
	}

	public IResourceOperation getResourceOperation() {
		return iResourceOperation;
	}

	public IServiceOperation getServiceOperation() {
		return iServiceOperation;
	}

	public IProductOperation getProductOperation() {
		return iProductOperation;
	}

	public org.openecomp.sdc.be.model.cache.ComponentCache getComponentCache() {
		return ComponentCache;
	}
}
