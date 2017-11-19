package org.openecomp.core.impl;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.core.converter.errors.CreateToscaObjectErrorBuilder;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ToscaConverterUtil {
  private static final String set = "set";

  public static <T> Optional<T> createObjectFromClass(String objectId,
                                       Object objectCandidate,
                                       Class<T> classToCreate) {
    try {
      return createObjectUsingSetters(objectCandidate, classToCreate);
    } catch (Exception e) {
      throw new CoreException(
          new CreateToscaObjectErrorBuilder(classToCreate.getSimpleName(), objectId, e.getMessage())
              .build());
    }
  }

  private static <T> Optional<T> createObjectUsingSetters(Object objectCandidate,
                                                          Class<T> classToCreate) throws Exception {
    if(!(objectCandidate instanceof Map)){
      return Optional.empty();
    }

    Map<String, Object> objectAsMap = (Map<String, Object>) objectCandidate;
    Field[] classFields = classToCreate.getDeclaredFields();
    T result = classToCreate.newInstance();

    for(Field field : classFields){
      Object fieldValueToAssign = objectAsMap.get(field.getName());
      String methodName = set + StringUtils.capitalize(field.getName());

      if(shouldSetterMethodNeedsToGetInvoked(classToCreate, field, fieldValueToAssign, methodName)){
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
      return false;
    }
  }
}
