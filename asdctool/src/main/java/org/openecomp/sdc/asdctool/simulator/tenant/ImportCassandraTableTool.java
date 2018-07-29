package org.openecomp.sdc.asdctool.simulator.tenant;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Main class of utility imports CSV file into the specified table
 * The old stuff of the table is removed.
 * 
 * Accepts 3 mandatory arguments:
 * 			1. Path to configuration folder
 * 			2. Name of the table
 * 			3. Path to the CSV file
 *   
 *  Example of usage:
 *  		\src\main\resources\config\ operationalenvironment "C:\Users\dr2032\Documents\env.csv"
 *  
 *  See relevant import handler for example of csv file line. 
 *  
 *  The list of supported tables:
 *  		1. operationalenvironment
 *  
 *  
 * @author dr2032
 *
 */
public class ImportCassandraTableTool {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportCassandraTableTool.class);
	
	private static Map<String, Consumer<String>> mapHandlers = new HashMap<>();
	
	static {
		mapHandlers.put(OperationalEvnironmentImportHandler.getTableName().toLowerCase(), OperationalEvnironmentImportHandler::execute);
	}
	
	public static void main(String[] args) {
		if(args.length == 3) {
			String appConfigDir = args[0];
			String tableName = args[1];
			String fileName = args[2];
			
			ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
			new ConfigurationManager(configurationSource);
		
			Consumer<String> executor = mapHandlers.get(tableName.toLowerCase());
			if (executor != null) {
				executor.accept(fileName);
			} 
			else {
				LOGGER.warn("Import to table [{}] is not supported yet!", tableName);
			}
		}
		else {
			LOGGER.warn("Invalid number of arguments. The 1st shoduld be path to config dir, the 2nd - table name and the 3rd - path to CSV file.");
		}
		
		
		System.exit(0);
	}
	
}
