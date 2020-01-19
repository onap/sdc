/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.handlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class XlsOutputHandler implements OutputHandler {

	private static final Logger log = Logger.getLogger(XlsOutputHandler.class);
	private Workbook workbook;
	private Sheet activeSheet;
	private int rowCount = 0;
	private String sheetName;
    private String outputPath;
	
	public XlsOutputHandler(String outputPath, String sheetName, Object... title){
		this.outputPath = outputPath;
	    this.sheetName = sheetName;
		initiate(sheetName, title);
	}
	
	@Override
	public void initiate(String sheetName, Object... title) {
		log.info("#initiate - Starting to initiate XlsOutputHandler. ");
		workbook = new HSSFWorkbook();
		activeSheet = workbook.createSheet(sheetName);
		addRecord(title);
		log.info("#initiate - XlsOutputHandler has been initiated. ");
	}

	@Override
	public void addRecord(Object... record) {
		log.info("#addRecord - Going to add record {} to output. ", record);
        Row currentRow = activeSheet.createRow(rowCount++);
		log.info("#addRecord - A new row has been created");
        int columnCount = 0;
        Cell cell;
        for(Object cellValue : record){
            cell = currentRow.createCell(columnCount++);
            if (cellValue != null) {
                cell.setCellValue(cellValue.toString());
            }
        }
	}

	@Override
	public boolean writeOutputAndCloseFile() {
		if (rowCount <= 1) {
			return false;
		}
        try {
			FileOutputStream file = getXlsFile();
			workbook.write(file);
			file.close();
			return true;
		} catch (Exception e) {
			log.debug("#writeOutputAndCloseFile - Failed to write an output file. The {} exception occurred. ", e.getMessage());
			return false;
		}
	}

	public String getOutputPath() {
		return outputPath;
	}

	FileOutputStream getXlsFile() throws FileNotFoundException {
        String fileName = buildFileName();
        log.info("#getXlsFile - Going to write the output file {}. ", fileName);
        return new FileOutputStream(fileName);
    }

    private String buildFileName() {
        StringBuilder fileName = new StringBuilder();
        if(StringUtils.isNotEmpty(outputPath)){
            fileName.append(outputPath);
        }
        return fileName.append(sheetName)
                .append("_")
                .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis()))
                .append(".xls")
				.toString();
    }

}
