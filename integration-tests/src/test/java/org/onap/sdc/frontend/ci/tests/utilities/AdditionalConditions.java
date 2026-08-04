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

package org.onap.sdc.frontend.ci.tests.utilities;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class AdditionalConditions {

    private AdditionalConditions() {

    }

    public static ExpectedCondition<Boolean> pageLoadWait() {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                String scriptJS = "try {\n"
                                + "  if (document.readyState !== 'complete') {\r\n"
                                + "    return false; // Page not loaded yet\r\n"
                                + "  }\r\n"
                                + "  if (window.jQuery) {\r\n"
                                + "    if (window.jQuery.active) {\r\n"
                                + "      return false;\r\n"
                                + "    } else if (window.jQuery.ajax && window.jQuery.ajax.active) {\r\n"
                                + "      return false;\r\n"
                                + "    }\r\n"
                                + "  }\r\n"
                                // Requests issued through Angular's HttpClient are reported to Testability
                                // by HeadersInterceptor. Angular's own isStable()/whenStable() are unusable
                                // here because the runtime can always have a macrotask pending.
                                + "  if (window.getAllAngularTestabilities) {\r\n"
                                + "    var testabilities = window.getAllAngularTestabilities();\r\n"
                                + "    for (var i = 0; i < testabilities.length; i++) {\r\n"
                                + "      if (testabilities[i].getPendingRequestCount\r\n"
                                + "          && testabilities[i].getPendingRequestCount() !== 0) {\r\n"
                                + "        return false; // Angular HttpClient request still in flight\r\n"
                                + "      }\r\n"
                                + "    }\r\n"
                                + "  }\r\n"
                                + "  return true;\r\n"
                                + "} catch (ex) {\r\n"
                                + "  return false;\r\n"
                                + "}";
                return Boolean.valueOf(((JavascriptExecutor) driver).executeScript(scriptJS).toString());
            }
        };
    }


}
