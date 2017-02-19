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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverTest {

	// @Test
	public void remoteTest() throws Exception {
		DesiredCapabilities cap = new DesiredCapabilities().firefox();
		cap.setPlatform(Platform.WINDOWS);
		cap.setBrowserName("firefox");

		RemoteWebDriver remoteDriver = new RemoteWebDriver(new URL("http://1.2.3.4:5555/wd/hub"), cap);
		remoteDriver.navigate().to("http://www.google.co.il");
		remoteDriver.findElementByName("q").sendKeys("execute automation");
		remoteDriver.findElementByName("btnK").click();
	}
}
