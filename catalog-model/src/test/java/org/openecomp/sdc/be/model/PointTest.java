package org.openecomp.sdc.be.model;

import javax.annotation.Generated;

import org.junit.Test;


public class PointTest {

	private Point createTestSubject() {
		return new Point();
	}

	
	@Test
	public void testGetX() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getX();
	}

	
	@Test
	public void testSetX() throws Exception {
		Point testSubject;
		String x = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setX(x);
	}

	
	@Test
	public void testGetY() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getY();
	}

	
	@Test
	public void testSetY() throws Exception {
		Point testSubject;
		String y = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setY(y);
	}

	
	@Test
	public void testToString() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}