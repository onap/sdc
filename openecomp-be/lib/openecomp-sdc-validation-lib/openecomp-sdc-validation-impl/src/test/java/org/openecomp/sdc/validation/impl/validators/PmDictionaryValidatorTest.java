package org.openecomp.sdc.validation.impl.validators;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

public class PmDictionaryValidatorTest {

    private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/pm_dictionary_validator";
    private static final String VALID_PM_DICTIONARY_YAML = "valid_pm_dictionary.yaml";
    private static final String INVALID_PM_DICTIONARY_YAML = "invalid_pm_dictionary.yaml";

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

    @Test
    public void shouldMatchProperPmDictNames() {
        for(String ext : VALID_PM_DICTIONARY_EXTENSIONS) {
            assertTrue(PmDictionaryValidator.isPmDictionary(ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my" + ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my_" + ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my_" + ext.toUpperCase()));
        }
    }

    @Test
    public void shouldNotReturnErrorsWhenValidPmDict() {
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + VALID_PM_DICTIONARY_YAML);

        Assert.assertNotNull(messages);
        Assert.assertEquals(0, messages.size());
    }

    @Test
    public void shouldReturnErrorsWhenInvalidPmDict() {
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + INVALID_PM_DICTIONARY_YAML);

        Assert.assertNotNull(messages);
        Assert.assertNotNull(messages.get(INVALID_PM_DICTIONARY_YAML));
        Assert.assertEquals(4, messages.get(INVALID_PM_DICTIONARY_YAML).getErrorMessageList().size());
    }

    private Map<String, MessageContainer> runValidation(String path) {
        PmDictionaryValidator validator = new PmDictionaryValidator();
        return ValidationTestUtil.testValidator(validator, path);
    }
}