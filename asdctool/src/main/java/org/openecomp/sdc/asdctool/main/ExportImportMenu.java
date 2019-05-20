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

import org.openecomp.sdc.asdctool.impl.GraphJsonValidator;
import org.openecomp.sdc.asdctool.impl.GraphMLConverter;
import org.openecomp.sdc.asdctool.impl.GraphMLDataAnalyzer;

public class ExportImportMenu {

	private static void usageAndExit() {
		exportUsage();
		importUsage();
		exportUsersUsage();
		validateJsonUsage();

		System.exit(1);
	}

	private static void importUsage() {
		System.out.println("Usage: import <janusgraph.properties> <graph file location>");
	}

	private static void validateJsonUsage() {
		System.out.println("Usage: validate-json <export graph path>");
	}

	private static void exportUsage() {
		System.out.println("Usage: export <janusgraph.properties> <output directory>");
	}

	private static void dataReportUsage() {
		System.out.println("Usage: get-data-report-from-graph-ml <full path of .graphml file>");
	}

	private static void exportUsersUsage() {
		System.out.println("Usage: exportusers <janusgraph.properties> <output directory>");
	}

	public static void main(String[] args) throws Exception {

		if (args == null || args.length < 1) {
			usageAndExit();
		}

		String operation = args[0];
		GraphMLConverter graphMLConverter = new GraphMLConverter();
		switch (operation.toLowerCase()) {

		case "export":
			boolean isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				exportUsage();
				System.exit(1);
			}

			boolean result = graphMLConverter.exportGraph(args);
			if (result == false) {
				System.exit(2);
			}

			break;
		case "import":
			isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				importUsage();
				System.exit(1);
			}
			result = graphMLConverter.importGraph(args);
			if (result == false) {
				System.exit(2);
			}
			break;

		case "exportusers":
			isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				importUsage();
				System.exit(1);
			}
			result = graphMLConverter.exportUsers(args);
			if (result == false) {
				System.exit(2);
			}
			break;

		case "findproblem":
			isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				importUsage();
				System.exit(1);
			}
			result = graphMLConverter.findErrorInJsonGraph(args);
			if (result == false) {
				System.exit(2);
			}
			break;
		case "validate-json":
			String jsonFilePath = validateAndGetJsonFilePath(args);
			GraphJsonValidator graphJsonValidator = new GraphJsonValidator();
			if (graphJsonValidator.verifyJanusGraphJson(jsonFilePath)) {
				System.exit(2);
			}
			break;

		case "export-as-graph-ml":
			isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				exportUsage();
				System.exit(1);
			}
			String mlFile = graphMLConverter.exportGraphMl(args);
			if (mlFile == null) {
				System.exit(2);
			}
			break;
		case "export-as-graph-ml-with-data-report":
			isValid = verifyParamsLength(args, 3);
			if (false == isValid) {
				exportUsage();
				System.exit(1);
			}
			mlFile = graphMLConverter.exportGraphMl(args);
			if (mlFile == null) {
				System.exit(2);
			}
			String[] dataArgs = new String[] { mlFile };
			mlFile = new GraphMLDataAnalyzer().analyzeGraphMLData(dataArgs);
			if (mlFile == null) {
				System.exit(2);
			}
			break;
		case "get-data-report-from-graph-ml":
			isValid = verifyParamsLength(args, 2);
			if (false == isValid) {
				dataReportUsage();
				System.exit(1);
			}
			dataArgs = new String[] { args[1] };
			mlFile = new GraphMLDataAnalyzer().analyzeGraphMLData(dataArgs);
			if (mlFile == null) {
				System.exit(2);
			}
			break;
		default:
			usageAndExit();
		}

	}

	private static String validateAndGetJsonFilePath(String[] args) {
		boolean isValid;
		isValid = verifyParamsLength(args, 2);
		if (!isValid) {
            validateJsonUsage();
            System.exit(1);
        }
        return args[1];
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
