package org.openecomp.sdc.be.components.lifecycle;

import org.junit.Test;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;

public class LifecycleChangeInfoWithActionTest {

	private LifecycleChangeInfoWithAction createTestSubject() {
		return new LifecycleChangeInfoWithAction();
	}

	@Test
	public void testConstructor() throws Exception {
		new LifecycleChangeInfoWithAction("mock");
		new LifecycleChangeInfoWithAction("mock", LifecycleChanceActionEnum.CREATE_FROM_CSAR);
	}
	
	@Test
	public void testGetAction() throws Exception {
		LifecycleChangeInfoWithAction testSubject;
		LifecycleChanceActionEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		LifecycleChangeInfoWithAction testSubject;
		LifecycleChanceActionEnum action = LifecycleChanceActionEnum.CREATE_FROM_CSAR;

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}
}