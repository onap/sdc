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

package org.openecomp.sdc.test.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtilsSdc {
	/*
	 * Can be used to set the logger instance to overcome mockito limitation 
	 * private static final Logger log
	 * 
	 * TestUtilsSdc.setFinalStatic(testSubject.getClass(), "log", LoggerFactory.getLogger(testSubject.getClass()));
	 */
	public static void setFinalStatic(Class targetClass, String fieldName, Object newValue) throws Exception {
		Field field = targetClass.getDeclaredField("log");
		field.setAccessible(true);

		// remove final modifier from field
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(null, newValue);
	}
}
