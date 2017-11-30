package org.openecomp.sdc.asdctool.migration.tasks.handlers;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XlsOutputHandler implements OutputHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(XlsOutputHandler.class);
	
	private Workbook workbook;
	private Sheet activeSheet;
	private Row currentRow;
	int rowCount = 0;
	
	public XlsOutputHandler(Object... title){
		initiate(title);
	}
	
	@Override
	public void initiate(Object... title) {
		LOGGER.info("Starting to initiate xls output handler. ");
		workbook = new HSSFWorkbook();
		activeSheet = workbook.createSheet("Upgrade Migration 1710.0 results");
		addRecord(title);
		LOGGER.info("Xls output handler has been initiated. ");
	}

	@Override
	public void addRecord(Object... record) {
		LOGGER.debug("Going to add record {} to output. ", record);
		currentRow = activeSheet.createRow(rowCount++);
		LOGGER.debug("A new row has been created");
        int columnCount = 0;
        Cell cell;
        for(Object cellValue : record){
            cell = currentRow.createCell(columnCount++);
            if(cellValue != null)
            	cell.setCellValue(cellValue.toString());
        }
	}

	@Override
	public boolean writeOutput() {
        try {
			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        	String fileName = "UpgradeMigration1710Results_" + df.format(System.currentTimeMillis()) + ".xls";
        	LOGGER.info("Going to write xls output file {}. ", fileName);
			workbook.write(new FileOutputStream(fileName));
			return true;
		} catch (Exception e) {
			LOGGER.error("Failed to write an output file upon  Upgrade migration 1710. Exception {} occured. ", e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

}
