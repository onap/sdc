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

    ngOnInit() {
        this.connectWizardService.changedCapabilityProperties = [];
    }

    onPropertySelectedUpdate = ($event) => {
        console.log("==>" + 'PROPERTY VALUE SELECTED');
        // this.selectedFlatProperty = $event;
        // let parentProperty:PropertyFEModel = this.propertiesService.getParentPropertyFEModelFromPath(this.instanceFePropertiesMap[this.selectedFlatProperty.instanceName], this.selectedFlatProperty.path);
        // parentProperty.expandedChildPropertyId = this.selectedFlatProperty.path;
    };

    propertyValueChanged = (property: PropertyFEModel) => {
        if (!property.isDeclared) {
            const propChangedIdx = this.connectWizardService.changedCapabilityProperties.indexOf(property);
            if (this.componentInstanceServiceNg2.hasPropertyChanged(property)) {
                console.log("==>" + this.constructor.name + ": propertyValueChanged " + property);
                if (propChangedIdx === -1) {
                    this.connectWizardService.changedCapabilityProperties.push(property);
                }
            }
            else {
                if (propChangedIdx !== -1) {
                    console.log("==>" + this.constructor.name + ": propertyValueChanged (reset to original) " + property);
                    this.connectWizardService.changedCapabilityProperties.splice(propChangedIdx, 1);
                }
            }
        }
    };

    preventNext = ():boolean => {
        return false;
    }

    preventBack = ():boolean => {
        return this.savingProperty;
    }
}
