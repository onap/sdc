package org.openecomp.sdc.validation.impl.validators;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.onap.validation.yaml.YamlFileValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.validation.Validator;

public class PmDictionaryValidator implements Validator {

    private static final List<String> VALID_PM_DICTIONARY_EXTENSIONS = List.of(
        "pmdict.yml",
        "pmdict.yaml",
        "pm_dict.yml",
        "pm_dict.yaml",
        "pmdictionary.yml",
        "pmdictionary.yaml",
        "pm_dictionary.yml",
        "pm_dictionary.yaml"
    );
    private static final ErrorMessageCode PM_DICT_ERROR_CODE = new ErrorMessageCode("PM_DICT");

    @Override
    public void validate(GlobalValidationContext globalContext) {
        globalContext.getFiles().stream()
            .filter(PmDictionaryValidator::isPmDictionary)
            .forEach(fileName -> validate(fileName, globalContext));
    }

    static boolean isPmDictionary(String fileName) {
        return VALID_PM_DICTIONARY_EXTENSIONS.stream()
            .anyMatch(extension -> fileName.toLowerCase().endsWith(extension));
    }

    private void validate(String fileName, GlobalValidationContext globalContext) {
        Optional<InputStream> rowContent = globalContext.getFileContent(fileName); // rowContent.get().readAllBytes()
        try {
            List<YamlDocumentValidationError> validationErrors = new YamlFileValidator()
                .validateYamlWithSchema(rowContent.orElseThrow(NoSuchElementException::new).readAllBytes());

            validationErrors.stream()
                .map(this::prepareValidationMessage)
                .forEach(message -> addErrorToContext(globalContext, fileName, message));
        } catch (NoSuchElementException e) {
            addErrorToContext(globalContext, fileName, formatMessage("File is empty"));
        } catch (YamlProcessingException | IOException e) {
            addErrorToContext(globalContext, fileName, formatMessage(e.getMessage()));
        }
    }

    private String prepareValidationMessage(YamlDocumentValidationError error) {
        final String errorMessage = String.format("Document Number: %s, Path: %s, Problem: %s",
            error.getYamlDocumentNumber(),
            error.getPath(),
            error.getMessage()
        );
        return formatMessage(errorMessage);
    }

    private String formatMessage(String message) {
        return ErrorMessagesFormatBuilder
            .getErrorWithParameters(PM_DICT_ERROR_CODE, message);
    }

    private void addErrorToContext(GlobalValidationContext globalContext, String fileName, String message) {
        globalContext.addMessage(fileName, ErrorLevel.ERROR, message);
    }
}
