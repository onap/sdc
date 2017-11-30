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

package org.openecomp.sdc.ci.tests.utilities;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.paulhammant.ngwebdriver.NgWebDriver;

public class AdditionalConditions {
		
	public static ExpectedCondition<Boolean> jQueryAJAXCallsHaveCompleted() {
	    return new ExpectedCondition<Boolean>() {
	        @Override
			public Boolean apply(WebDriver driver) {
	        	return (Boolean) ((JavascriptExecutor)driver).
	        			executeScript("return (window.jQuery!= null) && (jQuery.active === 0);");
			}
		};
	}
	
	public static ExpectedCondition <Boolean> angularHasFinishedProcessing() {
	   return new ExpectedCondition<Boolean>() {
		   	@Override
		   	public Boolean apply(WebDriver driver) {
//		   		String scriptJS = "return (window.angular !==undefined) &&"
//		   				+ " (angular.element(document).injector() !==undefined) &&"
//		   				+ " (angular.element(document).injector().get('$http').pendingRequests.length === 0)";
//		   		return Boolean.valueOf(( (JavascriptExecutor) driver).executeScript(scriptJS).toString());
		   		new NgWebDriver((JavascriptExecutor) driver).waitForAngularRequestsToFinish();
		   		return true;
		   	}
	   };
	}
	
    public static ExpectedCondition <Boolean> pageLoadWait() {
        return new ExpectedCondition<Boolean>() {
                   @Override
                   public Boolean apply(WebDriver driver) {
                          String scriptJS = 
                                       "try {\r\n" + 
                                       "  if (document.readyState !== 'complete') {\r\n" + 
                                       "    return false; // Page not loaded yet\r\n" + 
                                       "  }\r\n" + 
                                       "  if (window.jQuery) {\r\n" + 
                                       "    if (window.jQuery.active) {\r\n" + 
                                       "      return false;\r\n" + 
                                       "    } else if (window.jQuery.ajax && window.jQuery.ajax.active) {\r\n" + 
                                       "      return false;\r\n" + 
                                       "    }\r\n" + 
                                       "  }\r\n" + 
                                       "  if (window.angular) {\r\n" + 
                                       "    if (!window.qa) {\r\n" + 
                                       "      // Used to track the render cycle finish after loading is complete\r\n" + 
                                       "      window.qa = {\r\n" + 
                                       "        doneRendering: false\r\n" + 
                                       "      };\r\n" + 
                                       "    }\r\n" + 
                                       "    // Get the angular injector for this app (change element if necessary)\r\n" + 
                                       "    var injector = window.angular.element('body').injector();\r\n" + 
                                       "    // Store providers to use for these checks\r\n" + 
                                       "    var $rootScope = injector.get('$rootScope');\r\n" + 
                                       "    var $http = injector.get('$http');\r\n" + 
                                       "    var $timeout = injector.get('$timeout');\r\n" + 
                                       "    // Check if digest\r\n" + 
                                       "    if ($rootScope.$$phase === '$apply' || $rootScope.$$phase === '$digest' || $http.pendingRequests.length !== 0) {\r\n" + 
                                       "      window.qa.doneRendering = false;\r\n" + 
                                       "      return false; // Angular digesting or loading data\r\n" + 
                                       "    }\r\n" + 
                                       "    if (!window.qa.doneRendering) {\r\n" + 
                                       "      // Set timeout to mark angular rendering as finished\r\n" + 
                                       "      $timeout(function() {\r\n" + 
                                       "        window.qa.doneRendering = true;\r\n" + 
                                       "      }, 0);\r\n" + 
                                       "      return false;\r\n" + 
                                       "    }\r\n" + 
                                       "  }\r\n" + 
                                       "  return true;\r\n" + 
                                       "} catch (ex) {\r\n" + 
                                       "  return false;\r\n" + 
                                       "}";
                          return Boolean.valueOf(( (JavascriptExecutor) driver).executeScript(scriptJS).toString());
                   }
        };
     }

	
	
}
