/*!
* Copyright © 2016-2018 European Support Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/


import {ICompositionViewModelScope} from "../../composition-view-model";
import {Service, PropertiesGroup, InputsGroup, ServiceInstanceObject, InterfaceModel, InputBEModel} from 'app/models';
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";

interface IServiceConsumptionViewModelScope extends ICompositionViewModelScope {
    service: Service;
    instancesMappedList: Array<ServiceInstanceObject>;
    componentInstancesProperties: PropertiesGroup;
    componentInstancesInputs: InputsGroup;
    componentInstancesInterfaces: Map<string, Array<InterfaceModel>>;
    componentInputs: Array<InputBEModel>;
}


export class ServiceConsumptionViewModel {

    static '$inject' = [
        '$scope',
        'ServiceServiceNg2'
    ];

    constructor(private $scope:IServiceConsumptionViewModelScope, private ServiceServiceNg2:ServiceServiceNg2) {
        this.$scope.service = <Service>this.$scope.currentComponent;
        this.initInstances();
        this.initScope();
    }

    private initInstances = ():void => {
        this.ServiceServiceNg2.getServiceConsumptionData(this.$scope.service).subscribe((genericResponse:ComponentGenericResponse) => {
            this.$scope.componentInstancesProperties = genericResponse.componentInstancesProperties;
            this.$scope.componentInstancesInputs = genericResponse.componentInstancesInputs;
            this.$scope.componentInstancesInterfaces = genericResponse.componentInstancesInterfaces;
            this.$scope.componentInputs = genericResponse.inputs;
            this.updateInstanceAttributes();
        });
    }

    private updateInstanceAttributes = ():void => {
        if (this.$scope.isComponentInstanceSelected() && this.$scope.componentInstancesProperties) {
            this.$scope.instancesMappedList = this.$scope.service.componentInstances.map(coInstance => new ServiceInstanceObject({
                id: coInstance.uniqueId,
                name: coInstance.name,
                properties: this.$scope.componentInstancesProperties[coInstance.uniqueId] || [],
                inputs: this.$scope.componentInstancesInputs[coInstance.uniqueId] || [],
                interfaces: this.$scope.componentInstancesInterfaces[coInstance.uniqueId] || []
            }));
        }
    }

    private initScope = ():void => {
        this.$scope.$watch('currentComponent.selectedInstance', ():void => {
            this.updateInstanceAttributes();
        });

        this.$scope.registerCreateInstanceEvent(() => {
            this.initInstances();
        });

        this.$scope.$on('$destroy', this.$scope.unregisterCreateInstanceEvent);
    }
}
