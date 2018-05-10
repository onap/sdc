package org.openecomp.sdc.common.util;

import javax.annotation.Generated;

import org.junit.Test;

import fj.data.Either;

public class SerializationUtilsTest {

	private SerializationUtils createTestSubject() {
		return new SerializationUtils();
	}

	@Test
	public void testSerialize() throws Exception {
		Object object = null;
		Either<byte[], Boolean> result;

		// default test
		result = SerializationUtils.serialize(object);
	}

	@Test
	public void testDeserialize() throws Exception {
		byte[] bytes = new byte[] { ' ' };
		Either<Object, Boolean> result;

		// default test
		result = SerializationUtils.deserialize(bytes);
	}

	@Test
	public void testSerializeExt() throws Exception {
		Object object = null;
		Either<byte[], Boolean> result;

		// default test
		result = SerializationUtils.serializeExt(object);
	}

}