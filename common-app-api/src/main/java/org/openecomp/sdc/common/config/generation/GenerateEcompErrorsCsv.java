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

package org.openecomp.sdc.common.config.generation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.config.EcompErrorEnum.AlarmSeverity;
import org.openecomp.sdc.common.config.EcompErrorEnum.ErrorType;
import org.openecomp.sdc.common.config.EcompErrorLogUtil;

public class GenerateEcompErrorsCsv {

	private static String DATE_FORMAT = "dd-M-yyyy-hh-mm-ss";

	private static String NEW_LINE = System.getProperty("line.separator");

	private static void usage() {
		System.out.println("java org.openecomp.sdc.common.config.generation.GenerateEcompErrorsCsv <target folder>");
		System.exit(1);
	}

	public static void main(String[] args) {

		String targetFolder = "target";
		if (args.length > 1) {
			targetFolder = args[0];
		}

		GenerateEcompErrorsCsv ecompErrorsCsv = new GenerateEcompErrorsCsv();

		ecompErrorsCsv.generateEcompErrorsCsvFile(targetFolder, true);
	}

	public static class EcompErrorRow {

		String errorName;
		String errorCode;
		String description;
		ErrorType errorType;
		AlarmSeverity alarmSeverity;
		String cleanErrorCode;
		String resolution;

		public String getErrorName() {
			return errorName;
		}

		public void setErrorName(String errorName) {
			this.errorName = errorName;
		}

		public String getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public ErrorType getErrorType() {
			return errorType;
		}

		public void setErrorType(ErrorType errorType) {
			this.errorType = errorType;
		}

		public AlarmSeverity getAlarmSeverity() {
			return alarmSeverity;
		}

		public void setAlarmSeverity(AlarmSeverity alarmSeverity) {
			this.alarmSeverity = alarmSeverity;
		}

		public String getCleanErrorCode() {
			return cleanErrorCode;
		}

		public void setCleanErrorCode(String cleanErrorCode) {
			this.cleanErrorCode = cleanErrorCode;
		}

		public String getResolution() {
			return resolution;
		}

		public void setResolution(String resolution) {
			this.resolution = resolution;
		}

	}

	public boolean generateEcompErrorsCsvFile(String targetFolder, boolean addTimeToFileName) {

		targetFolder += File.separator;

		boolean result = false;
		String dateFormatted = "";

		if (addTimeToFileName == true) {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

			Date date = new Date();

			dateFormatted = "." + dateFormat.format(date);

		}

		String outputFile = targetFolder + "ecompErrorCodes" + dateFormatted + ".csv";

		FileWriter writer = null;

		try {
			writer = new FileWriter(outputFile);

			List<EcompErrorRow> errors = new ArrayList<>();

			for (EcompErrorEnum ecompErrorEnum : EcompErrorEnum.values()) {

				EcompErrorRow ecompErrorRow = new EcompErrorRow();

				String errorCode = EcompErrorLogUtil.createEcode(ecompErrorEnum);

				EcompErrorEnum clearCodeEnum = ecompErrorEnum.getClearCode();
				String cleanErrorCode = null;
				if (clearCodeEnum != null) {
					cleanErrorCode = EcompErrorLogUtil.createEcode(clearCodeEnum);
				}

				ecompErrorRow.setAlarmSeverity(ecompErrorEnum.getAlarmSeverity());
				ecompErrorRow.setCleanErrorCode(cleanErrorCode);
				ecompErrorRow.setDescription(ecompErrorEnum.getEcompErrorCode().getDescription());
				ecompErrorRow.setErrorCode(errorCode);
				ecompErrorRow.setErrorName(ecompErrorEnum.name().toString());
				ecompErrorRow.setErrorType(ecompErrorEnum.geteType());
				ecompErrorRow.setResolution(ecompErrorEnum.getEcompErrorCode().getResolution());

				errors.add(ecompErrorRow);
			}

			writeHeaders(writer);

			for (EcompErrorRow ecompErrorRow : errors) {
				writer.append(addInvertedCommas(ecompErrorRow.getErrorCode()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getErrorType().toString()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getDescription()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getResolution()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getAlarmSeverity().toString()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getErrorName()));
				writer.append(',');
				writer.append(addInvertedCommas(ecompErrorRow.getCleanErrorCode()));
				writer.append(NEW_LINE);
			}

			result = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		return result;
	}

	private void writeHeaders(FileWriter writer) throws IOException {

		writer.append("\"ERROR CODE\"");
		writer.append(',');
		writer.append("\"ERROR TYPE\"");
		writer.append(',');
		writer.append("\"DESCRIPTION\"");
		writer.append(',');
		writer.append("\"RESOLUTION\"");
		writer.append(',');
		writer.append("\"ALARM SEVERITY\"");
		writer.append(',');
		writer.append("\"ERROR NAME\"");
		writer.append(',');
		writer.append("\"CLEAN CODE\"");
		writer.append(NEW_LINE);
	}

	private String addInvertedCommas(String str) {

		if (str == null) {
			return "\"\"";
		}

		return "\"" + str + "\"";
	}

}
