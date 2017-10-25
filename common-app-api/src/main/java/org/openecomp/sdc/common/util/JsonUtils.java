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

package org.openecomp.sdc.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

	public static String toString(JsonElement jsonElement) {

		if (jsonElement == null) {
			return null;
		}

		if (false == jsonElement.isJsonNull()) {
			if (false == jsonElement.isJsonObject()) {
				return jsonElement.getAsString();
			} else {
				return jsonElement.toString();
			}
		} else {
			return null;
		}

	}

	public static boolean containsEntry(JsonObject json, String key) {
		return json.get(key) != null;
	}

	public static boolean isEmptyJson(JsonObject json) {
		return json.entrySet().isEmpty();
	}

	public static boolean isEmptyJson(JsonElement json) {
		return json.isJsonPrimitive() ? false : JsonUtils.isEmptyJson(json.getAsJsonObject());
	}
	
	public static boolean isJsonNullOrEmpty(JsonObject json) {
		return json.isJsonNull() || isEmptyJson(json);
	}
}
