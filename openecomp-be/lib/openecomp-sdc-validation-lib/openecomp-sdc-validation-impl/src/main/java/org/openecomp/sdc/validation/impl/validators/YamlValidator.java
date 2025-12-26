/*
 * Copyright © 2016-2017 European Support Limited
 * Copyright © 2020-2021 Nokia
 *
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
 */
package org.openecomp.sdc.validation.impl.validators;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.onap.sdc.tosca.services.MyPropertyUtils;
import org.onap.sdc.tosca.services.StrictMapAppenderConstructor;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.impl.util.YamlValidatorUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

public class YamlValidator implements Validator {

    private static final ErrorMessageCode ERROR_CODE_YML_1 = new ErrorMessageCode("YML1");
    private static final ErrorMessageCode ERROR_CODE_YML_2 = new ErrorMessageCode("YML2");

    @Override
    public void validate(GlobalValidationContext globalContext) {
        Set<String> pmDictionaryFiles = GlobalContextUtil.findFilesByType(globalContext, Type.PM_DICTIONARY);
        Collection<String> files = globalContext
            .files((fileName, globalValidationContext) -> FileExtensionUtils.isYaml(fileName) && !pmDictionaryFiles.contains(fileName));
        files.forEach(fileName -> validate(fileName, globalContext));
    }

    private void validate(String fileName, GlobalValidationContext globalContext) {
        Optional<InputStream> rowContent = globalContext.getFileContent(fileName);
        if (rowContent.isEmpty()) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_YML_1, Messages.INVALID_YAML_FORMAT_REASON.getErrorMessage(),
                    Messages.EMPTY_YAML_FILE.getErrorMessage()));
            return; /* no need to continue validation */
        }

        try (var yamlContent = rowContent.get()) {
     LoaderOptions options = new LoaderOptions();
    options.setAllowDuplicateKeys(false);

    // Constructor now requires LoaderOptions
    Constructor constructor = new StrictMapAppenderConstructor(Map.class, options);
    constructor.setPropertyUtils(new MyPropertyUtils());
    TypeDescription yamlFileDescription = new TypeDescription(Map.class);
    constructor.addTypeDescription(yamlFileDescription);
    constructor.setAllowDuplicateKeys(false);

    // Representer also requires DumperOptions and LoaderOptions
    DumperOptions dumperOptions = new DumperOptions();
    Representer representer = new Representer(dumperOptions);

    // Updated Yaml constructor
    Yaml yaml = new Yaml(constructor, representer, dumperOptions, options);

    Object yamlObj = yaml.load(yamlContent);
            if (yamlObj == null) {
                throw new Exception("Empty YAML content");
            }
        } catch (Exception exception) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_YML_2, Messages
                        .INVALID_YAML_FORMAT_REASON.getErrorMessage(),
                    YamlValidatorUtil.getParserExceptionReason(exception)));
        }
    }

}

