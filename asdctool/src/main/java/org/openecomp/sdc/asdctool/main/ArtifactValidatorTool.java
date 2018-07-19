package org.openecomp.sdc.asdctool.main;

import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationToolConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ArtifactValidatorTool {

	public static void main(String[] args) {

        String outputPath = args[0];
        ValidationConfigManager.setOutputFullFilePath(outputPath);
        ValidationConfigManager.setCsvReportFilePath(outputPath);
        
        String appConfigDir = args[1];
        AnnotationConfigApplicationContext context = initContext(appConfigDir);
        ArtifactToolBL validationToolBL = context.getBean(ArtifactToolBL.class);

        System.out.println("Start ArtifactValidation Tool");
        Boolean result = validationToolBL.validateAll();
        if (result) {
            System.out.println("ArtifactValidation finished successfully");
            System.exit(0);
        } else {
            System.out.println("ArtifactValidation finished with warnings");
            System.exit(2);
        }
	}
	
	private static AnnotationConfigApplicationContext initContext(String appConfigDir) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ValidationToolConfiguration.class);
		return context;
	}

}
