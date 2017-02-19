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

package org.openecomp.sdc.be.dao.neo4j.filters;

import java.util.HashMap;
import java.util.Map;

public class UpdateFilter extends MatchFilter {

	private Map<String, Object> toUpdate;

	public UpdateFilter(Map<String, Object> toUpdate) {
		super();
		this.toUpdate = toUpdate;
	}

	public UpdateFilter() {
		super();
		toUpdate = new HashMap<String, Object>();
	}

	public UpdateFilter(Map<String, Object> toMatch, Map<String, Object> toUpdate) {
		super(toMatch);
		this.toUpdate = toUpdate;
	}

	public Map<String, Object> getToUpdate() {
		return toUpdate;
	}

	public void setToUpdate(Map<String, Object> toUpdate) {
		this.toUpdate = toUpdate;
	}

	public void addToUpdate(String property, Object value) {
		toUpdate.put(property, value);
	}
}
