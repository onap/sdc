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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.impl.GraphJsonValidator;
import org.openecomp.sdc.asdctool.impl.GraphMLConverter;
import org.openecomp.sdc.asdctool.impl.GraphMLDataAnalyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExportImportMenu {

	enum ExportImportEnum {
		DATA_REPORT("Usage: get-data-report-from-graph-ml <full path of .graphml file>", "get-data-report-from-graph-ml"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 2)) {
					usage();
					System.exit(1);
				}
				String[] dataArgs = new String[] { args[1] };
				if (new GraphMLDataAnalyzer().analyzeGraphMLData(dataArgs) == null) {
					System.exit(2);
				}
			}
		},
		EXPORT("Usage: export <janusgraph.properties> <output directory>", "export"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}

				if (!GRAPH_ML_CONVERTER.exportGraph(args)) {
					System.exit(2);
				}
			}
		},EXPORT_AS_GRAPH("Usage: export-as-graph-ml <janusgraph.properties> <output directory>", "export-as-graph-ml"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}
				if (GRAPH_ML_CONVERTER.exportGraphMl(args) == null) {
					System.exit(2);
				}
			}
		},EXPORT_USERS("Usage: exportusers <janusgraph.properties> <output directory>", "exportusers"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}
				if (!GRAPH_ML_CONVERTER.exportUsers(args)) {
					System.exit(2);
				}
			}
		},EXPORT_WITH_REPORT("Usage: export-as-graph-ml-with-data-report <janusgraph.properties> <output directory>", "export-as-graph-ml-with-data-report"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}
				if (GRAPH_ML_CONVERTER.exportGraphMl(args) == null) {
					System.exit(2);
				}
				String[] dataArgs = new String[] {GRAPH_ML_CONVERTER.exportGraphMl(args)};
				if (new GraphMLDataAnalyzer().analyzeGraphMLData(dataArgs) == null) {
					System.exit(2);
				}
			}
		},FIND_PROBLEM("Usage: findproblem <janusgraph.properties> <graph file location>", "findproblem"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}
				if (!GRAPH_ML_CONVERTER.findErrorInJsonGraph(args)) {
					System.exit(2);
				}
			}
		},IMPORT("Usage: import <janusgraph.properties> <graph file location>", "import"){
			@Override
			void handle(String[] args) {
				if (verifyParamsLength(args, 3)) {
					usage();
					System.exit(1);
				}
				if (!GRAPH_ML_CONVERTER.importGraph(args)) {
					System.exit(2);
				}
			}
		},VALIDATE_JSON("Usage: validate-json <export graph path>", "validate-json"){
			@Override
			void handle(String[] args) throws IOException {
				if (verifyParamsLength(args, 2)) {
					usage();
					System.exit(1);
				}
				String jsonFilePath = args[1];
				GraphJsonValidator graphJsonValidator = new GraphJsonValidator();
				if (graphJsonValidator.verifyJanusGraphJson(jsonFilePath)) {
					System.exit(2);
				}
			}
		},NONE{
			@Override
			void handle(String[] args) {
				usage();
				System.exit(1);
			}

			void usage(){
				Arrays.stream(ExportImportEnum.values()).filter(type -> type != NONE).forEach(ExportImportEnum::usage);
			}
		};

		private static final GraphMLConverter GRAPH_ML_CONVERTER = new GraphMLConverter();
		private String usage;
		private String keyword;

		ExportImportEnum(String usage, String keyword) {
			this.usage = usage;
			this.keyword = keyword;
		}

		ExportImportEnum() {}

		void usage(){
			System.out.println(usage);
		}

		static ExportImportEnum getByKeyword(String keyword) {
			List<ExportImportEnum> collected = Arrays.stream(ExportImportEnum.values())
				.filter(type -> type != NONE)
				.filter(type -> type.keyword.equals(keyword))
				.collect(Collectors.toList());
			return collected.isEmpty() ? NONE : collected.get(0);
		}

		abstract void handle(String[] args) throws IOException;

		private static boolean verifyParamsLength(String[] args, int i) {
			if (args == null) {
				return i > 0;
			}
			return args.length < i;
		}
	}

	public static void main(String[] args) throws Exception {
		ExportImportEnum type;
		if (args == null || args.length < 1) {
			type = ExportImportEnum.NONE;
		}else{
			type = ExportImportEnum.getByKeyword(getOperation(args).toLowerCase());
		}
		type.handle(args);
	}

	private static String getOperation(String[] args) {
		String operation = null;
		if (args != null) {
			operation = args[0];
		}
		return operation;
	}

}
