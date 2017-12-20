package org.openecomp.core.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.converter.errors.CreateToscaObjectErrorBuilder;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToscaConverterUtil {
  private static final String SET = "set";
  private static final String DEFAULT = "default";
  private static final String DEFAULT_CAPITAL = "Default";
  private static Set<String> defaultValueKeys;

  private static Logger LOGGER = LoggerFactory.getLogger(ToscaConverterUtil.class.getName());

  static {
    defaultValueKeys =
        Stream.of(DEFAULT, DEFAULT_CAPITAL).collect(Collectors.toSet());
  }

  public static <T> Optional<T> createObjectFromClass(String objectId,
                                                      Object objectCandidate,
                                                      Class<T> classToCreate) {
    try {
      return createObjectUsingSetters(objectCandidate, classToCreate);
    } catch (Exception ex) {
      throw new CoreException(
          new CreateToscaObjectErrorBuilder(classToCreate.getSimpleName(), objectId)
              .build(), ex);
    }
  }

  private static <T> Optional<T> createObjectUsingSetters(Object objectCandidate,
                                                          Class<T> classToCreate) throws Exception {
    if (Objects.isNull(objectCandidate)
        || !(objectCandidate instanceof Map)) {
      return Optional.empty();
    }

    Map<String, Object> objectAsMap = (Map<String, Object>) objectCandidate;
    Field[] classFields = classToCreate.getDeclaredFields();
    T result = classToCreate.newInstance();

    for (Field field : classFields) {
      Object fieldValueToAssign = objectAsMap.get(field.getName());
      String methodName = SET + StringUtils.capitalize(field.getName());

      if(shouldSetterMethodNeedsToGetInvoked(classToCreate, field, fieldValueToAssign, methodName)) {
        classToCreate.getMethod(methodName, field.getType()).invoke(result, fieldValueToAssign);
      }
    }

    return Optional.of(result);
  }
  private static <T> boolean shouldSetterMethodNeedsToGetInvoked(Class<T> classToCreate,
                                                                 Field field,
                                                                 Object fieldValueToAssign,
                                                                 String methodName) {

    try {
      return Objects.nonNull(fieldValueToAssign)
          && Objects.nonNull(classToCreate.getMethod(methodName, field.getType()));
    } catch (NoSuchMethodException e) {
      LOGGER.debug(String.format("Could not extract method '%s' from class '%s'. returning false " +
              "with filedType '%s'.", methodName, classToCreate, field.getType()), e);
      return false;
    }
  }

  public static Optional<Object> getDefaultValue(Object entryValue,
                                       Object objectToAssignDefaultValue) {
    if (!(entryValue instanceof Map)
        || Objects.isNull(objectToAssignDefaultValue)) {
      return Optional.empty();
    }

    return Optional.ofNullable(getDefaultParameterValue((Map<String, Object>) entryValue));
  }

  private static Object getDefaultParameterValue(Map<String, Object> entryValue) {
    Object defaultValue = null;
    Set<String> keys = new HashSet<>(entryValue.keySet());
    keys.retainAll(defaultValueKeys);

    if (CollectionUtils.isNotEmpty(keys)) {
      defaultValue = entryValue.get(keys.iterator().next());
    }

    return defaultValue;
  }
}
