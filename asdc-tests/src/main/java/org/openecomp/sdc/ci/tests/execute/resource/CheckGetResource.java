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

package org.openecomp.sdc.ci.tests.execute.resource;

import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.util.SerializationUtils;

import fj.data.Either;

public class CheckGetResource {

	public void checkGetVmmsc6() throws Exception {

		try {

			System.out.println("dddd");
			RestResponse getResource = ResourceRestUtils.getResource("96eb6583-2822-448b-a284-bfc144fa627e");

			Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);

			Either<byte[], Boolean> serialize = SerializationUtils.serializeExt(resource);

			SerializationUtils.deserializeExt(serialize.left().value(), Resource.class, "ffff");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
