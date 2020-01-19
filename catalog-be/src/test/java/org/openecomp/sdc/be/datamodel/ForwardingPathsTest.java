/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.datamodel;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ForwardingPathsTest {

	@Test
	public void testForwardingPaths()  {
		ForwardingPaths testForwardingPaths = new ForwardingPaths();
		Set<String> path= new HashSet<>(Arrays.asList("test"));
		testForwardingPaths.setForwardingPathToDelete(path);
		Set<String> getPath = testForwardingPaths.getForwardingPathToDelete();
		assertThat(getPath).isEqualTo(path);
	}

}