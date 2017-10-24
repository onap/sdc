package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.OrchestrationTemplateFileExtensionErrorBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_CONFIG_NAMESPACE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_IMPL_KEY;

public class OrchestrationUploadFactory {
    private static Map<String, ImplementationConfiguration> fileHanlders;

    static {
        Configuration config = ConfigurationManager.lookup();
        fileHanlders = new ConcurrentHashMap<>(config.populateMap(ORCHESTRATION_CONFIG_NAMESPACE,
                ORCHESTRATION_IMPL_KEY, ImplementationConfiguration.class));

    }

    public static final OrchestrationTemplateFileHandler createOrchestrationTemplateFileHandler(String filePrefix) {
        String fileExtension = filePrefix.toLowerCase();
        ImplementationConfiguration orchestrationTemplateFileHandler = fileHanlders.get(fileExtension);

        if(Objects.isNull(orchestrationTemplateFileHandler)){
            throw new CoreException(new OrchestrationTemplateFileExtensionErrorBuilder
                ().build());
        }

        return  CommonMethods.newInstance(orchestrationTemplateFileHandler.getImplementationClass(),
                        OrchestrationTemplateFileHandler.class);
    }
}
