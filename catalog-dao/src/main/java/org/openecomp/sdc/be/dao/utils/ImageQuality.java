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

package org.openecomp.sdc.be.dao.utils;

/**
 * Available qualities for images in Alien 4 Cloud.
 *
 * @author luc boutier
 */
public enum ImageQuality {
	QUALITY_16(16), QUALITY_32(32), QUALITY_64(64), QUALITY_128(128), QUALITY_512(512), QUALITY_BEST(-1);

	private final int size;

	private ImageQuality(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
}
