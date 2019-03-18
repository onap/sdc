package org.openecomp.sdc.be.components.property;

import com.google.gson.Gson;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.json.simple.JSONObject;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.sdc.common.api.Constants.GET_INPUT;

public abstract class DefaultPropertyDeclarator<PROPERTYOWNER extends PropertiesOwner, PROPERTYTYPE extends PropertyDataDefinition> implements PropertyDeclarator {

    private static final Logger log = Logger.getLogger(DefaultPropertyDeclarator.class);
    private static final short LOOP_PROTECTION_LEVEL = 10;
    private final Gson gson = new Gson();
    private ComponentsUtils componentsUtils;
    private PropertyOperation propertyOperation;

    public DefaultPropertyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation) {
        this.componentsUtils = componentsUtils;
        this.propertyOperation = propertyOperation;
    }

    @Override
    public Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesAsInputs(Component component, String propertiesOwnerId, List<ComponentInstancePropInput> propsToDeclare) {
        log.debug("#declarePropertiesAsInputs - declaring properties as inputs for component {} from properties owner {}", component.getUniqueId(), propertiesOwnerId);
        return resolvePropertiesOwner(component, propertiesOwnerId)
                .map(propertyOwner -> declarePropertiesAsInputs(component, propertyOwner, propsToDeclare))
                .orElse(Either.right(onPropertiesOwnerNotFound(component.getUniqueId(), propertiesOwnerId)));
    }

    abstract PROPERTYTYPE createDeclaredProperty(PropertyDataDefinition prop);

    abstract Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String propertiesOwnerId, List<PROPERTYTYPE> properties);

    abstract Optional<PROPERTYOWNER> resolvePropertiesOwner(Component component, String propertiesOwnerId);

    abstract void addPropertiesListToInput(PROPERTYTYPE declaredProp, InputDefinition input);

    private StorageOperationStatus onPropertiesOwnerNotFound(String componentId, String propertiesOwnerId) {
        log.debug("#declarePropertiesAsInputs - properties owner {} was not found on component {}", propertiesOwnerId, componentId);
        return StorageOperationStatus.NOT_FOUND;
    }

    private Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesAsInputs(Component component, PROPERTYOWNER propertiesOwner, List<ComponentInstancePropInput> propsToDeclare) {
        PropertiesDeclarationData inputsProperties = createInputsAndOverridePropertiesValues(component.getUniqueId(), propertiesOwner, propsToDeclare);
        return updatePropertiesValues(component, propertiesOwner.getUniqueId(), inputsProperties.getPropertiesToUpdate())
                .left()
                .map(updatePropsRes -> inputsProperties.getInputsToCreate());
    }

    private PropertiesDeclarationData createInputsAndOverridePropertiesValues(String componentId, PROPERTYOWNER propertiesOwner, List<ComponentInstancePropInput> propsToDeclare) {
        List<PROPERTYTYPE> declaredProperties = new ArrayList<>();
        List<InputDefinition> createdInputs = propsToDeclare.stream()
                .map(propInput -> declarePropertyInput(componentId, propertiesOwner, declaredProperties, propInput))
                .collect(Collectors.toList());
        return new PropertiesDeclarationData(createdInputs, declaredProperties);
    }

    private InputDefinition declarePropertyInput(String componentId, PROPERTYOWNER propertiesOwner, List<PROPERTYTYPE> declaredProperties, ComponentInstancePropInput propInput) {
        PropertyDataDefinition prop = resolveProperty(declaredProperties, propInput);
        InputDefinition inputDefinition = createInput(componentId, propertiesOwner, propInput, prop);
        PROPERTYTYPE declaredProperty = createDeclaredProperty(prop);
        if(!declaredProperties.contains(declaredProperty)){
            declaredProperties.add(declaredProperty);
        }
        addPropertiesListToInput(declaredProperty, inputDefinition);
        return inputDefinition;
    }

    private InputDefinition createInput(String componentId, PROPERTYOWNER propertiesOwner,
                                        ComponentInstancePropInput propInput, PropertyDataDefinition prop) {
        String generatedInputPrefix = propertiesOwner.getNormalizedName();
        if (propertiesOwner.getUniqueId().equals(propInput.getParentUniqueId())) {
            //Creating input from property create on self using add property..Do not add the prefix
            generatedInputPrefix = null;
        }
        String generatedInputName = generateInputName(generatedInputPrefix, propInput);
        return createInputFromProperty(componentId, propertiesOwner, generatedInputName, propInput, prop);
    }

    private String generateInputName(String inputName, ComponentInstancePropInput propInput) {
        String declaredInputName;
        String[] parsedPropNames = propInput.getParsedPropNames();

        if(parsedPropNames != null){
            declaredInputName = handleInputName(inputName, parsedPropNames);
        } else {
            String[] propName = {propInput.getName()};
            declaredInputName = handleInputName(inputName, propName);
        }

        return declaredInputName;
    }

    private String handleInputName(String inputName, String[] parsedPropNames) {
        StringBuilder prefix = new StringBuilder();
        int startingIndex;

        if(Objects.isNull(inputName)) {
            prefix.append(parsedPropNames[0]);
            startingIndex = 1;
        } else {
            prefix.append(inputName);
            startingIndex = 0;
        }

        while(startingIndex < parsedPropNames.length){
            prefix.append("_");
            prefix.append(parsedPropNames[startingIndex]);
            startingIndex ++;
        }

        return prefix.toString();
    }

    private PropertyDataDefinition resolveProperty(List<PROPERTYTYPE> propertiesToCreate, ComponentInstancePropInput propInput) {
        Optional<PROPERTYTYPE> resolvedProperty = propertiesToCreate.stream()
                .filter(p -> p.getName().equals(propInput.getName()))
                .findFirst();
        return resolvedProperty.isPresent() ? resolvedProperty.get() : propInput;
    }

    InputDefinition createInputFromProperty(String componentId, PROPERTYOWNER propertiesOwner, String inputName, ComponentInstancePropInput propInput, PropertyDataDefinition prop) {
        String propertiesName = propInput.getPropertiesName() ;
        PropertyDefinition selectedProp = propInput.getInput();
        String[] parsedPropNames = propInput.getParsedPropNames();
        InputDefinition input;
        boolean complexProperty = false;
        if(propertiesName != null && !propertiesName.isEmpty() && selectedProp != null){
            complexProperty = true;
            input = new InputDefinition(selectedProp);
            input.setDefaultValue(selectedProp.getValue());
        }else{
            input = new InputDefinition(prop);
            input.setDefaultValue(prop.getValue());
        }
        input.setName(inputName);
        input.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, input.getName()));
        input.setInputPath(propertiesName);
        input.setInstanceUniqueId(propertiesOwner.getUniqueId());
        input.setPropertyId(propInput.getUniqueId());
        changePropertyValueToGetInputValue(inputName, parsedPropNames, input, prop, complexProperty);

        if(prop instanceof IComponentInstanceConnectedElement) {
            ((IComponentInstanceConnectedElement) prop)
                .setComponentInstanceId(propertiesOwner.getUniqueId());
            ((IComponentInstanceConnectedElement) prop)
                .setComponentInstanceName(propertiesOwner.getName());
        }
        return input;
    }

    private void changePropertyValueToGetInputValue(String inputName, String[] parsedPropNames, InputDefinition input, PropertyDataDefinition prop, boolean complexProperty) {
        JSONObject jobject = new JSONObject();
        String value = (String) prop.getValue();
        if(value == null || value.isEmpty()){
            if(complexProperty){

                jobject = createJSONValueForProperty(parsedPropNames.length -1, parsedPropNames, jobject, inputName);
                prop.setValue(jobject.toJSONString());

            }else{

                jobject.put(GET_INPUT, input.getName());
                prop.setValue(jobject.toJSONString());

            }

        }else{

            //String value = value;
            Object objValue =  new Yaml().load(value);
            if( objValue instanceof Map || objValue  instanceof List){
                if(!complexProperty){
                    jobject.put(GET_INPUT, input.getName());
                    prop.setValue(jobject.toJSONString());


                }else{
                    Map<String, Object> mappedToscaTemplate = (Map<String, Object>) objValue;
                    createInputValue(mappedToscaTemplate, 1, parsedPropNames, inputName);

                    String json = gson.toJson(mappedToscaTemplate);
                    prop.setValue(json);

                }

            }else{
                jobject.put(GET_INPUT, input.getName());
                prop.setValue(jobject.toJSONString());

            }

        }


        if(CollectionUtils.isEmpty(prop.getGetInputValues())){
            prop.setGetInputValues(new ArrayList<>());
        }
        List<GetInputValueDataDefinition> getInputValues = prop.getGetInputValues();

        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputId(input.getUniqueId());
        getInputValueDataDefinition.setInputName(input.getName());
        getInputValues.add(getInputValueDataDefinition);
    }

    private  JSONObject createJSONValueForProperty (int i, String [] parsedPropNames, JSONObject ooj, String inputName){

        while(i >= 1){
            if( i == parsedPropNames.length -1){
                JSONObject jobProp = new JSONObject();
                jobProp.put(GET_INPUT, inputName);
                ooj.put(parsedPropNames[i], jobProp);
                i--;
                return createJSONValueForProperty (i, parsedPropNames, ooj, inputName);
            }else{
                JSONObject res = new JSONObject();
                res.put(parsedPropNames[i], ooj);
                i --;
                res =  createJSONValueForProperty (i, parsedPropNames, res, inputName);
                return res;
            }
        }

        return ooj;
    }

    private  Map<String, Object> createInputValue(Map<String, Object> lhm1, int index, String[] inputNames, String inputName){
        while(index < inputNames.length){
            if(lhm1.containsKey(inputNames[index])){
                Object value = lhm1.get(inputNames[index]);
                if (value instanceof Map){
                    if(index == inputNames.length -1){
                        ((Map) value).put(GET_INPUT, inputName);
                        return (Map) value;

                    }else{
                        index++;
                        return  createInputValue((Map)value, index, inputNames, inputName);
                    }
                }else{
                    Map<String, Object> jobProp = new HashMap<>();
                    if(index == inputNames.length -1){
                        jobProp.put(GET_INPUT, inputName);
                        lhm1.put(inputNames[index], jobProp);
                        return lhm1;
                    }else{
                        lhm1.put(inputNames[index], jobProp);
                        index++;
                        return  createInputValue(jobProp, index, inputNames, inputName);
                    }
                }
            }else{
                Map<String, Object> jobProp = new HashMap<>();
                lhm1.put(inputNames[index], jobProp);
                if(index == inputNames.length -1){
                    jobProp.put(GET_INPUT, inputName);
                    return jobProp;
                }else{
                    index++;
                    return  createInputValue(jobProp, index, inputNames, inputName);
                }
            }
        }
        return lhm1;
    }

    private class PropertiesDeclarationData {
        private List<InputDefinition> inputsToCreate;
        private List<PROPERTYTYPE> propertiesToUpdate;

        PropertiesDeclarationData(List<InputDefinition> inputsToCreate, List<PROPERTYTYPE> propertiesToUpdate) {
            this.inputsToCreate = inputsToCreate;
            this.propertiesToUpdate = propertiesToUpdate;
        }

        List<InputDefinition> getInputsToCreate() {
            return inputsToCreate;
        }

        List<PROPERTYTYPE> getPropertiesToUpdate() {
            return propertiesToUpdate;
        }
    }

    Either<InputDefinition, ResponseFormat>  prepareValueBeforeDelete(InputDefinition inputForDelete, PropertyDataDefinition inputValue, List<String> pathOfComponentInstances) {
        Either<InputDefinition, ResponseFormat> deleteEither = Either.left(inputForDelete);
        String value = inputValue.getValue();
        Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(value);

        resetInputName(mappedToscaTemplate, inputForDelete.getName());

        value = "";
        if(!mappedToscaTemplate.isEmpty()){
            Either result = cleanNestedMap(mappedToscaTemplate , true);
            Map modifiedMappedToscaTemplate = mappedToscaTemplate;
            if (result.isLeft())
                modifiedMappedToscaTemplate = (Map)result.left().value();
            else
                log.warn("Map cleanup failed -> " +result.right().value().toString());    //continue, don't break operation
            value = gson.toJson(modifiedMappedToscaTemplate);
        }
        inputValue.setValue(value);


        List<GetInputValueDataDefinition> getInputsValues = inputValue.getGetInputValues();
        if(getInputsValues != null && !getInputsValues.isEmpty()){
            Optional<GetInputValueDataDefinition> op = getInputsValues.stream().filter(gi -> gi.getInputId().equals(inputForDelete.getUniqueId())).findAny();
            if(op.isPresent()){
                getInputsValues.remove(op.get());
            }
        }
        inputValue.setGetInputValues(getInputsValues);

        Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(pathOfComponentInstances, inputValue.getUniqueId(), inputValue.getDefaultValue());
        if (findDefaultValue.isRight()) {
            deleteEither = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(findDefaultValue.right().value()))));
            return deleteEither;

        }
        String defaultValue = findDefaultValue.left().value();
        inputValue.setDefaultValue(defaultValue);
        log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);
        return deleteEither;
    }

    private void resetInputName(Map<String, Object> lhm1, String inputName){
        for (Map.Entry<String, Object> entry : lhm1.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && ((String) value).equalsIgnoreCase(inputName) && key.equals(GET_INPUT)) {
                value = "";
                lhm1.remove(key);
            } else if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>)value;
                resetInputName(subMap, inputName);
            } else {
                continue;
            }

        }
    }

    private Either cleanNestedMap( Map mappedToscaTemplate , boolean deepClone  ){
        if (MapUtils.isNotEmpty( mappedToscaTemplate ) ){
            if (deepClone){
                if (!(mappedToscaTemplate instanceof HashMap))
                    return Either.right("expecting mappedToscaTemplate as HashMap ,recieved "+ mappedToscaTemplate.getClass().getSimpleName() );
                else
                    mappedToscaTemplate = (HashMap)((HashMap) mappedToscaTemplate).clone();
            }
            return Either.left( (Map) cleanEmptyNestedValuesInMap( mappedToscaTemplate , LOOP_PROTECTION_LEVEL ) );
        }
        else {
            log.debug("mappedToscaTemplate is empty ");
            return Either.right("mappedToscaTemplate is empty ");
        }
    }

    /*        Mutates the object
     *        Tail recurse -> traverse the tosca elements and remove nested empty map properties
     *        this only handles nested maps, other objects are left untouched (even a Set containing a map) since behaviour is unexpected
     *
     *        @param  toscaElement - expected map of tosca values
     *        @return mutated @param toscaElement , where empty maps are deleted , return null for empty map.
     **/
    private Object cleanEmptyNestedValuesInMap(Object toscaElement , short loopProtectionLevel ){
        if (loopProtectionLevel<=0 || toscaElement==null || !(toscaElement instanceof  Map))
            return toscaElement;
        if ( MapUtils.isNotEmpty( (Map)toscaElement ) ) {
            Object ret;
            Set<Object> keysToRemove = new HashSet<>();                                                                 // use different set to avoid ConcurrentModificationException
            for( Object key : ((Map)toscaElement).keySet() ) {
                Object value = ((Map) toscaElement).get(key);
                ret = cleanEmptyNestedValuesInMap(value , --loopProtectionLevel );
                if ( ret == null )
                    keysToRemove.add(key);
            }
            Collection set = ((Map) toscaElement).keySet();
            if (CollectionUtils.isNotEmpty(set))
                set.removeAll(keysToRemove);

            if ( isEmptyNestedMap(toscaElement) )
                return null;
        }
        else
            return null;
        return toscaElement;
    }

    //@returns true iff map nested maps are all empty
    //ignores other collection objects
    private boolean isEmptyNestedMap(Object element){
        boolean isEmpty = true;
        if (element != null){
            if ( element instanceof Map ){
                if (MapUtils.isEmpty((Map)element))
                    isEmpty = true;
                else
                {
                    for( Object key : ((Map)(element)).keySet() ){
                        Object value =  ((Map)(element)).get(key);
                        isEmpty &= isEmptyNestedMap( value );
                    }
                }
            } else {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

}
