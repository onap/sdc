/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.ecomp.converters;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetMetadataConverterTest {

	private AssetMetadataConverter createTestSubject() {
		return new AssetMetadataConverter();
	}

	@Test
	public void testConvertToAssetMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		List<? extends Component> componentList = null;
		String serverBaseURL = "";
		boolean detailed = false;
		Either<List<? extends AssetMetadata>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		componentList = null;
		result = testSubject.convertToAssetMetadata(componentList, serverBaseURL, detailed);
		assertThat(result.isLeft()).isTrue();
	}

	@Test
	public void testConvertToSingleAssetMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		Resource component = new Resource();
		String serverBaseURL = "";
		boolean detailed = false;
		Either<? extends AssetMetadata, ResponseFormat> result;
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setComponentType(ComponentTypeEnum.RESOURCE);
		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToSingleAssetMetadata(component, serverBaseURL, detailed);
		assertThat(result.isLeft()).isTrue();
	}
}
