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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class OnboardCSVReport {

	private StringBuilder sb;
	private PrintWriter pw;

	public OnboardCSVReport(String filepath, String filename) {
		sb = new StringBuilder();
		try {
			File csvFile = new File(filepath + filename);
			pw = new PrintWriter(csvFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public StringBuilder appendStringToFile(String content) {
		return sb.append(content + ",");
	}

	public void openNewRow() {
		sb.append("\n");
	}

	public void writeRow(String... content) {
		for (String str : content) {
			appendStringToFile(str);
		}
		openNewRow();
	}

	public void closeFile() {
		pw.write(sb.toString());
		pw.close();
	}

}
