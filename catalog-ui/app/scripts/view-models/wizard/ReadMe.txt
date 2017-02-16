import-asset wizard
========================================================

What should be done in each wizard step:
----------------------------------------

1.	Each step scope should extend IAssetCreationStepScope (this way you can call methods on the wizard scope).
    Example:
    export interface IGeneralStepScope extends IAssetCreationStepScope {

    }

2.	Each step class should implements IAssetCreationStep
    Example: export class GeneralStepViewModel implements IAssetCreationStep {

    }

3.	Add the method: public save = (callback:Function):void => {}
    The method should perform the save and call: callback(true); in case of success, or callback(false); in case of error.
    Example:
    var onSuccess:Function = (resourceProperties:Services.IResourceResource) => {
        this.$scope.setAngularResourceOfResource(resourceProperties);
        var resourceObj = new Sdc.Models.Resource(resourceProperties);
        this.$scope.setEntity(resourceObj);
        this.$scope.latestEntityName = (resourceProperties.resourceName);
        callback(true);
    };

4.	Add the first line after the constructor: this.$scope.registerChild(this);
    This will register the current step reference in the wizard.

5.	Each step can get and set angular $resource of resource from the wizard.
    // Will be called from each step to get current entity.
    this.$scope.getAngularResourceOfResource = ():Services.IResourceResource => {
       return this.resourceProperties;
    };

    // Will be called from each step after save to update the resource.
    this.$scope.setAngularResourceOfResource = (resourceProperties:Services.IResourceResource):void => {
       this.resourceProperties = resourceProperties;
       this.fillAssetNameAndType();
    };

    Note: after success save, set setAngularResourceOfResource in the wizard (see example in step 3).

6.	The wizard needs to know if the step is valid (to know if to show next button), I used the following to update the wizard:
    this.$scope.$watch("editForm.$valid", function(newVal, oldVal){
        this.$scope.setValidState(newVal);
    });

    Note: in case there is no save for the step, and the step is always valid, call: this.$scope.setValidState(true);



