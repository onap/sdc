import {Module, AttributeModel, ResourceInstance, PropertyModel, InputFEModel} from "../models";
import {ComponentInstanceFactory} from "./component-instance-factory";
import {PropertyBEModel, RelationshipModel} from "app/models";

export class CommonUtils {

    static initProperties(propertiesObj:Array<PropertyModel>, uniqueId?:string):Array<PropertyModel> {

        let properties = new Array<PropertyModel>();
        if (propertiesObj) {
            _.forEach(propertiesObj, (property:PropertyModel):void => {
                if (uniqueId) {
                    property.readonly = property.parentUniqueId != uniqueId;
                }
                properties.push(new PropertyModel(property));
            });
        }
        return properties;
    };

    static initAttributes(attributesObj:Array<AttributeModel>, uniqueId?:string):Array<AttributeModel> {

        let attributes = new Array<AttributeModel>();
        if (attributesObj) {
            _.forEach(attributesObj, (attribute:AttributeModel):void => {
                if (uniqueId) {
                    attribute.readonly = attribute.parentUniqueId != uniqueId;
                }
                attributes.push(new AttributeModel(attribute));
            });
        }
        return attributes;
    };

    static initComponentInstances(componentInstanceObj:Array<ResourceInstance>):Array<ResourceInstance> {

        let componentInstances = new Array<ResourceInstance>();
        if (componentInstanceObj) {
            _.forEach(componentInstanceObj, (instance:ResourceInstance):void => {
                componentInstances.push(ComponentInstanceFactory.createComponentInstance(instance));
            });
        }
        return componentInstances;
    };

    static initModules(moduleArrayObj:Array<Module>):Array<Module> {

        let modules = new Array<Module>();

        if (moduleArrayObj) {
            _.forEach(moduleArrayObj, (module:Module):void => {
                if (module.type === "org.openecomp.groups.VfModule") {
                    modules.push(new Module(module));
                }
            });
        }
        return modules;
    };

    static initInputs(inputsObj:Array<PropertyBEModel>):Array<PropertyBEModel> {

        let inputs = new Array<PropertyBEModel>();

        if(inputsObj) {
            _.forEach(inputsObj, (input:PropertyBEModel):void => {
                inputs.push(new PropertyBEModel(input));
            })
        }

        return inputs;
    }

    static initBeProperties(propertiesObj: Array<PropertyBEModel>): Array<PropertyBEModel> {

        let properties = new Array<PropertyBEModel>();

        if (propertiesObj) {
            _.forEach(propertiesObj, (property: PropertyBEModel): void => {
                properties.push(new PropertyBEModel(property));
            })
        }

        return properties;
    }

    static initComponentInstanceRelations = (componentInstanceRelationsObj:Array<RelationshipModel>):Array<RelationshipModel> => {
        if (componentInstanceRelationsObj) {
             let componentInstancesRelations: Array<RelationshipModel> = [];
            _.forEach(componentInstanceRelationsObj, (instanceRelation:RelationshipModel):void => {
                componentInstancesRelations.push(new RelationshipModel(instanceRelation));
            });
            return componentInstancesRelations;
        }
    };
}

