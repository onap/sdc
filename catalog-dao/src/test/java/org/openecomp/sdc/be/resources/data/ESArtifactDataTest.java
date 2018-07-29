package org.openecomp.sdc.be.resources.data;

import org.junit.Test;

import java.nio.ByteBuffer;

public class ESArtifactDataTest {

	private ESArtifactData createTestSubject() {
		return new ESArtifactData();
	}
	
	@Test
	public void testCtor() throws Exception {
		new ESArtifactData("mock");
		new ESArtifactData("mock", new byte[0]);
	}
	
	@Test
	public void testGetDataAsArray() throws Exception {
		ESArtifactData testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataAsArray();
	}

	@Test
	public void testSetDataAsArray() throws Exception {
		ESArtifactData testSubject;
		byte[] data = new byte[] { ' ' };

		// test 1
		testSubject = createTestSubject();
		data = null;
		testSubject.setDataAsArray(data);

		// test 2
		testSubject = createTestSubject();
		data = new byte[] { ' ' };
		testSubject.setDataAsArray(data);
	}

	@Test
	public void testGetData() throws Exception {
		ESArtifactData testSubject;
		ByteBuffer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getData();
	}

	@Test
	public void testSetData() throws Exception {
		ESArtifactData testSubject;
		ByteBuffer data = null;

		// test 1
		testSubject = createTestSubject();
		data = null;
		testSubject.setData(data);
	}

	@Test
	public void testGetId() throws Exception {
		ESArtifactData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	@Test
	public void testSetId() throws Exception {
		ESArtifactData testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}
}