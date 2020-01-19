package org.openecomp.sdc.be.components.validation.service;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@org.springframework.stereotype.Component
public class ServiceCategoryValidator implements ServiceFieldValidator {

    private static final Logger log = Logger.getLogger(ServiceCategoryValidator.class.getName());
    private ComponentsUtils componentsUtils;
    protected IElementOperation elementDao;

    public ServiceCategoryValidator(ComponentsUtils componentsUtils, IElementOperation elementDao) {
        this.componentsUtils = componentsUtils;
        this.elementDao = elementDao;
    }

    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        log.debug("validate Service category");
        if (isEmpty(service.getCategories())) {
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
            componentsUtils.auditComponentAdmin(errorResponse, user, service, actionEnum, ComponentTypeEnum.SERVICE);
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
        }
        Either<Boolean, ResponseFormat> validatCategory = validateServiceCategory(service.getCategories());
        if (validatCategory.isRight()) {
            ResponseFormat responseFormat = validatCategory.right().value();
            componentsUtils.auditComponentAdmin(responseFormat, user, service, actionEnum, ComponentTypeEnum.SERVICE);
            throw new ByResponseFormatComponentException(responseFormat);
        }

    }

    private Either<Boolean, ResponseFormat> validateServiceCategory(List<CategoryDefinition> list) {
        if (list != null) {
            if (list.size() > 1) {
                log.debug("Must be only one category for service");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_TOO_MUCH_CATEGORIES, ComponentTypeEnum.SERVICE.getValue());
                return Either.right(responseFormat);
            }
            CategoryDefinition category = list.get(0);
            if (category.getSubcategories() != null) {
                log.debug("Subcategories cannot be defined for service");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.SERVICE_CANNOT_CONTAIN_SUBCATEGORY);
                return Either.right(responseFormat);
            }
            if (!ValidationUtils.validateStringNotEmpty(category.getName())) {
                log.debug("Resource category is empty");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.SERVICE.getValue());
                return Either.right(responseFormat);
            }

            log.debug("validating service category {} against valid categories list", list);
            Either<List<CategoryDefinition>, ActionStatus> categorys = elementDao.getAllServiceCategories();
            if (categorys.isRight()) {
                log.debug("failed to retrive service categories from JanusGraph");
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(categorys.right().value());
                return Either.right(responseFormat);
            }
            List<CategoryDefinition> categoryList = categorys.left().value();
            for (CategoryDefinition value : categoryList) {
                if (value.getName().equals(category.getName())) {
                    return Either.left(true);
                }
            }
            log.debug("Category {} is not part of service category group. Service category valid values are {}", list, categoryList);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.SERVICE.getValue()));
        }
        return Either.left(false);
    }
}
