/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdcrests.uniquevalue.types;

import java.util.Map;

/**
 * The unique value service exposes APIs to list unique types and check whether a value is already taken. The modules which uses the unique value
 * logic and would like to enable these APIs must implement this class.
 */
public interface UniqueTypesProvider {

    Map<String, String> listUniqueTypes();
}
