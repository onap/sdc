package org.openecomp.sdc.asdctool.simulator.tenant;

import com.opencsv.bean.CsvToBeanBuilder;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.dao.cassandra.schema.Table;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Imports CSV file into 
 * Example of line in the file
 * 		00002,135.42.43.45:5757,Context,FALSE,2017-10-11 12:02:01,INITIAL,personal tenant,abcd123456789,bbbbbbbbbbb
 * 		Date format is fixed: yyyy-MM-dd HH:mm:ss
 * @author dr2032
 *
 */
public class OperationalEvnironmentImportHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationalEvnironmentImportHandler.class);
	private static final String TABLE_NAME = Table.SDC_OPERATIONAL_ENVIRONMENT.getTableDescription().getTableName();
	
	private OperationalEvnironmentImportHandler() {
		
	}
	
	public static void execute(String fileName) {
		try {
			List<OperationalEnvironment> beans = new CsvToBeanBuilder<OperationalEnvironment>(new FileReader(fileName))
				       .withType(OperationalEnvironment.class).build().parse();
			
			List<OperationalEnvironmentEntry> entries = map(beans);
			modifyDb(entries);
			LOGGER.info("File {} has been successfully imported  into the [{}] table.", fileName, TABLE_NAME);
		} catch (IllegalStateException | FileNotFoundException e) {
			String errorMessage = String.format("Failed to import file: %s into the [%s] table ", fileName, TABLE_NAME);
			LOGGER.error(errorMessage, e);
		}
	}
	
	private static List<OperationalEnvironmentEntry> map(List<OperationalEnvironment> beans) {
		return beans.stream()
				.map(OperationalEvnironmentImportHandler::map)
				.collect(Collectors.toList());
		
	}
	
	private static OperationalEnvironmentEntry map(OperationalEnvironment perationalEnvironment) {
		OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
		
		entry.setEnvironmentId(perationalEnvironment.getEnvironmentId());
		entry.addDmaapUebAddress(perationalEnvironment.getDmaapUebAddress());
		entry.setEcompWorkloadContext(perationalEnvironment.getEcompWorkloadContext());
		entry.setIsProduction(perationalEnvironment.getIsProduction());
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			entry.setLastModified(formatter.parse(perationalEnvironment.getLastModified()));
		} catch (ParseException e) {
			LOGGER.error("Faild to pase Date, expected format is [yyyy-MM-dd HH:mm:ss].", e);
			throw new RuntimeException(e);
		}
		
		entry.setStatus(perationalEnvironment.getStatus());
		entry.setTenant(perationalEnvironment.getTenant());
		entry.setUebApikey(perationalEnvironment.getUebApikey());
		entry.setUebSecretKey(perationalEnvironment.getUebSecretKey());
		
		return entry;
		
	}
	
	private static OperationalEnvironmentDao createDaoObj() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ImportTableConfig.class);
		return (OperationalEnvironmentDao) context.getBean("operational-environment-dao");
	}
	
	private static void modifyDb(List<OperationalEnvironmentEntry> environments) {
		OperationalEnvironmentDao daoObj = createDaoObj();
		
		daoObj.deleteAll();
		
		environments.forEach(daoObj::save);
	}

	public static String getTableName() {
		return TABLE_NAME;
	}


}
