package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationToolConfiguration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ArtifactValidatorTool {
	private static Logger log = LoggerFactory.getLogger(ValidationTool.class.getName());
	
	public static void main(String[] args) throws Exception {

        String outputPath = args[0];
        ValidationConfigManager.setOutputFullFilePath(outputPath);
        ValidationConfigManager.setCsvReportFilePath(outputPath);
        
        String appConfigDir = args[1];
        AnnotationConfigApplicationContext context = initContext(appConfigDir);
        ArtifactToolBL validationToolBL = context.getBean(ArtifactToolBL.class);

        System.out.println("Start ArtifactValidation Tool");
        Boolean result = validationToolBL.validateAll();
       // ReportManager.reportEndOfToolRun();
        if (result) {
            System.out.println("ArtifactValidation finished successfully");
            System.exit(0);
        } else {
            System.out.println("ArtifactValidation finished with warnings");
            System.exit(2);
        }
	}
	
	private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidationToolConfiguration.class);
		return context;
	}

}
