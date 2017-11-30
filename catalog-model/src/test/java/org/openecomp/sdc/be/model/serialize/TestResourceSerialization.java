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

package org.openecomp.sdc.be.model.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.util.SerializationUtils;

import fj.data.Either;

public class TestResourceSerialization {

	// @Test
	public void findAllClassesUsedByResource() {

		Set<Class> classesWithoutSerialzable = new HashSet<>();
		Set<String> classestoIgnore = new HashSet<>();
		classestoIgnore.add("java.util.List");
		classestoIgnore.add("java.util.Map");
		classestoIgnore.add("long");

		Set<Class> allClasses = new HashSet<>();
		findAllClassesUsedByResource(Resource.class, allClasses);
		ArrayList l;
		for (Class clazz : allClasses) {
			Class[] interfaces = clazz.getInterfaces();
			if (interfaces != null) {
				String collect = Arrays.stream(interfaces).map(p -> p.getName()).collect(Collectors.joining("\n"));

				Class orElse = Arrays.stream(interfaces).filter(p -> p.getName().equals("java.io.Serializable"))
						.findAny().orElse(null);
				if (orElse == null) {
					classesWithoutSerialzable.add(clazz);
				}

			}
		}

		List<Class> collect = classesWithoutSerialzable.stream()
				.filter(p -> false == classestoIgnore.contains(p.getName())).collect(Collectors.toList());

		if (collect != null) {
			System.out.println(collect.stream().map(p -> p.getName()).collect(Collectors.joining("\n")));
			assertEquals("check all classes implements Serializable", 0, collect.size());
		}

	}

	public void findAllClassesUsedByResource(Class clazz, Set<Class> allClasses) {

		Class superclass = clazz.getSuperclass();
		findAllClassesOfClass(clazz, allClasses);

		if (superclass != null) {
			findAllClassesOfClass(superclass, allClasses);
		}

	}

	public void findAllClassesOfClass(Class clazz, Set<Class> allClasses) {

		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				String name = field.getName();
				Class type = field.getType();

				if (type.toString().contains(".List")) {
					ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
					Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
					// System.out.println(stringListClass); // class
					// java.lang.String.
					allClasses.add(stringListClass);
				}

				if (type.toString().contains("java.util.Map")) {
					ParameterizedType stringListType = (ParameterizedType) field.getGenericType();

					Type[] actualTypeArguments = stringListType.getActualTypeArguments();
					if (actualTypeArguments != null) {
						for (Type actualType : actualTypeArguments) {

							String typeName = actualType.getTypeName();
							// System.out.println("field " + name + "," +
							// typeName);

							if (typeName.startsWith("java.util.List<")) {
								String internalClass = typeName.replace("java.util.List<", "").replace(">", "");
								// create class from string
								Class myClass;
								try {
									myClass = Class.forName(internalClass);
									allClasses.add(myClass);
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
									assertTrue("Failed to convert " + internalClass + " to class", false);
								}

							} else {
								try {
									Class myClass = Class.forName(typeName);
									allClasses.add(myClass);
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
									assertTrue("Failed to convert " + typeName + " to class", false);
								}

							}
						}
					}

				}

				// System.out.println(type);
				allClasses.add(type);
			}
		}

	}

	private void addInternalTypeOfList(Class clazz) {

		// clazz.
		// ParameterizedType stringListType = (ParameterizedType)
		// field.getGenericType();
		// Class<?> stringListClass = (Class<?>)
		// stringListType.getActualTypeArguments()[0];
		// //System.out.println(stringListClass); // class java.lang.String.
		// allClasses.add(stringListClass);
		//
	}

	private boolean isClassImplementedSerialize(Class clazz) {

		Type[] genericInterfaces = clazz.getGenericInterfaces();
		if (genericInterfaces != null) {
			Type orElse = Arrays.stream(genericInterfaces).filter(p -> p.getTypeName().equals("java.io.Serializable"))
					.findAny().orElse(null);
			if (orElse != null) {
				return true;
			}
		}

		return false;
	}

	// @Test
	public void testSimpleResourceSerialize() {

		Resource resource = new Resource();
		String name = "res1";
		Map<String, String> allVersions = new HashMap<String, String>();
		allVersions.put("keya", "valuea");

		resource.setName(name);
		// all versions
		resource.setAllVersions(allVersions);
		List<ComponentInstance> resourceInstances = new ArrayList<ComponentInstance>();
		// component instances
		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setDescription("desc1");
		componentInstance.setComponentUid("comUid");
		resourceInstances.add(componentInstance);

		resource.setComponentInstances(resourceInstances);

		Either<byte[], Boolean> serialize = SerializationUtils.serialize(resource);
		assertTrue("check object serialized", serialize.isLeft());
		byte[] value = serialize.left().value();

		Either<Object, Boolean> deserialize = SerializationUtils.deserialize(value);
		assertTrue("check object deserialized", deserialize.isLeft());
		Object obj = deserialize.left().value();
		Resource desResource = (Resource) obj;
		assertEquals("check name", name, desResource.getName());
		verifyAllVersions(desResource);

	}

	private void verifyAllVersions(Resource desResource) {
		assertNotNull("check all versions", desResource.getAllVersions());
		assertEquals("check all version size", 1, desResource.getAllVersions().size());
		assertEquals("check all version key", "keya", desResource.getAllVersions().keySet().iterator().next());
		assertEquals("check all version value", "valuea", desResource.getAllVersions().values().iterator().next());
	}
}
