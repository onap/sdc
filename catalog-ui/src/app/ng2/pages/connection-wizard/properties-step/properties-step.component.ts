/**
 * Created by ob0695 on 9/4/2017.
 */
/**
 * Created by rc2122 on 9/4/2017.
 */
import {Component, Inject, forwardRef} from '@angular/core';
import {IStepComponent} from "app/models"
import {ConnectionWizardService} from "../connection-wizard.service";
import {PropertyFEModel} from "../../../../models/properties-inputs/property-fe-model";
import {InstanceFePropertiesMap} from "../../../../models/properties-inputs/property-fe-map";
import {PropertiesUtils} from "../../properties-assignment/services/properties.utils";
import {PropertyBEModel} from "../../../../models/properties-inputs/property-be-model";
import {ComponentInstanceServiceNg2} from "../../../services/component-instance-services/component-instance.service";

@Component({
    selector: 'properties-step',
    templateUrl: './properties-step.component.html',
    styleUrls: ['./properties-step.component.less']
})

export class PropertiesStepComponent implements IStepComponent{

    capabilityPropertiesMap: InstanceFePropertiesMap;
    savingProperty:boolean = false;

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService, private componentInstanceServiceNg2:ComponentInstanceServiceNg2, private propertiesUtils:PropertiesUtils) {

        this.capabilityPropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren({'capability' : connectWizardService.selectedMatch.capabilityProperties}, false);
    }

    onPropertySelectedUpdate = ($event) => {
        console.log("==>" + 'PROPERTY VALUE SELECTED');
        // this.selectedFlatProperty = $event;
        // let parentProperty:PropertyFEModel = this.propertiesService.getParentPropertyFEModelFromPath(this.instanceFePropertiesMap[this.selectedFlatProperty.instanceName], this.selectedFlatProperty.path);
        // parentProperty.expandedChildPropertyId = this.selectedFlatProperty.path;
    };

    propertyValueChanged = (event: PropertyFEModel) => {
        this.savingProperty = true;
        console.log("==>" + this.constructor.name + ": propertyValueChanged " + event);
        // Copying the actual value from the object ref into the value if it's from a complex type
        event.value = event.getJSONValue();

        // change property owned by the selected capability
        const propertyBe = new PropertyBEModel(event);
        propertyBe.parentUniqueId = this.connectWizardService.selectedMatch.relationship.capabilityOwnerId;
        //TODO - orit remove comment and test after BE finished
        this.componentInstanceServiceNg2
            .updateInstanceCapabilityProperty(this.connectWizardService.currentComponent, this.connectWizardService.selectedMatch.toNode, this.connectWizardService.selectedMatch.relationship.relationship.type, this.connectWizardService.selectedMatch.relationship.capability, propertyBe)
            .subscribe(response => {
                console.log("Update resource instance capability property response: ", response);
                this.connectWizardService.selectedMatch.capabilityProperties.find((property:PropertyBEModel)=>{
                    return property.uniqueId ==  response.uniqueId;
                }).value = response.value;
                this.savingProperty = false;
            }, error => {this.savingProperty = false;}); //ignore error
        console.log(event);


    };

    preventNext = ():boolean => {
        return false;
    }

    preventBack = ():boolean => {
        return this.savingProperty;
    }
}
