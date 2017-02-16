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

package org.openecomp.sdc.ci.tests.execute.resourceui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

public class VFCanvasTest extends SetupCDTest {

	public List<Integer> getposition(WebElement canvas, int width, int height) {

		width = canvas.getSize().getWidth();
		height = canvas.getSize().getHeight();
		Random r = new Random();
		int Resultx = r.nextInt(width);
		int Resulty = r.nextInt(height);
		List<Integer> position = new ArrayList<Integer>();
		position.add(Resultx);
		position.add(Resulty);
		return position;
	}

	@Test
	public void VFCanvasTest1() throws Exception {
		// GeneralUIUtils.waitForContainsdataTestIdVisibility("left-sectioin-element-QA");

		GeneralUIUtils.moveToStep(StepsEnum.COMPOSITION);
		Thread.sleep(2000);
		List<Integer> position = null;
		WebElement canvas = GeneralUIUtils.getWebElementWaitForVisible("canvas");
		int xPos = 0;
		int yPos = 0;
		position = getposition(canvas, xPos, yPos);
		WebElement otherElement = GeneralUIUtils
				.getWebElementWaitForVisible("left-sectioin-element-QA left-section-NeutronPort");
		for (int i = 0; i < 8; i++) {
			Actions builder = new Actions(GeneralUIUtils.getDriver());
			Action dragAndDrop = builder.clickAndHold(otherElement)
					.moveToElement(canvas, position.get(0), position.get(1)).release().build();
			dragAndDrop.perform();
			Thread.sleep(2000);
		}
		Thread.sleep(2000);
		Actions builder = new Actions(GeneralUIUtils.getDriver());
		builder.moveToElement(canvas, position.get(0), position.get(1));
		builder.clickAndHold();
		position = getposition(canvas, xPos, yPos);
		builder.moveToElement(canvas, position.get(0), position.get(1));
		builder.release();
		builder.build();
		builder.perform();
		builder.moveToElement(canvas, 200, 300);
		builder.release();
		builder.perform();

	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
