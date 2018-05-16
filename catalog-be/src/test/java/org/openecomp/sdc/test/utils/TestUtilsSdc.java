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
