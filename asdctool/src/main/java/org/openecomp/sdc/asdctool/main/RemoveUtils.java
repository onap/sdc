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

package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.impl.ProductLogic;

/**
 * Created by mlando on 2/23/2016.
 */
public class RemoveUtils {

	public static void main(String[] args) {

		if (args == null || args.length < 1) {
			removeUsage();
		}else {
			String operation = args[0];
			if(operation.equalsIgnoreCase("remove-products")) {
				boolean isValid = verifyParamsLength(args, 5);
				if (!isValid) {
					removeUsage();
					System.exit(1);
				}
				ProductLogic productLogic = new ProductLogic();
				boolean result = productLogic.deleteAllProducts(args[1], args[2], args[3], args[4]);
	
				if (!result) {
					System.exit(2);
				}
			}else {
				removeUsage();
			}
		}
	}

	private static void removeUsage() {
		System.out.println("Usage: remove-products <janusgraph.properties> <BE host> <BE port> <admin user>");
	}

	private static boolean verifyParamsLength(String[] args, int i) {
		if (args == null) {
			if (i > 0) {
				return false;
			}
			return true;
		}

		if (args.length >= i) {
			return true;
		}
		return false;
	}
}
