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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.fe.impl;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ImportMetadataTest {

	private static final String CHECKSUM = "CHECKSUM";
	private static final String CREATOR = "CREATOR";
	private static final String MIME = "MIME";
	private static final long SIZE = 123L;
	private static final String NAME = "name";

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(ImportMetadata.class, hasValidGettersAndSetters());
	}

	@Test
	public void testConstructor() {
		ImportMetadata importMetadata = new ImportMetadata(NAME, SIZE, MIME, CREATOR, CHECKSUM);
		assertThat(importMetadata.getCreator(), equalTo(CREATOR));
		assertThat(importMetadata.getMd5Checksum(), equalTo(CHECKSUM));
		assertThat(importMetadata.getMime(), equalTo(MIME));
		assertThat(importMetadata.getName(), equalTo(NAME));
		assertThat(importMetadata.getSize(), equalTo(SIZE));
	}

}