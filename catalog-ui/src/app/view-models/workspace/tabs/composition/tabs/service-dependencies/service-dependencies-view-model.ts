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
import {Service, ComponentInstance, PropertiesGroup, ServiceInstanceObject, PropertyBEModel} from 'app/models';
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ConstraintObject} from "app/ng2/components/logic/service-dependencies/service-dependencies.component";
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {DEPENDENCY_EVENTS} from "app/utils/constants";
import {EventListenerService} from 'app/services';

interface IServiceDependenciesViewModelScope extends ICompositionViewModelScope {
    service: Service;
    selectedInstanceSiblings: Array<ServiceInstanceObject>;
    componentInstancesConstraints: Array<any>;
    selectedInstanceConstraints: Array<ConstraintObject>;
    selectedInstanceProperties: Array<PropertyBEModel>;
    updateSelectedInstanceConstraints(constraintsList:Array<ConstraintObject>): void;
    loadConstraints(): void;
    componentInstanceProperties: PropertiesGroup;
    notifyDependencyEventsObserver: Function;
}



export class ServiceDependenciesViewModel {

    static '$inject' = [
        '$scope',
        'ComponentServiceNg2',
        'EventListenerService'
    ];

    constructor(private $scope:IServiceDependenciesViewModelScope, private ComponentServiceNg2:ComponentServiceNg2, private eventListenerService: EventListenerService) {
        this.$scope.service = <Service>this.$scope.currentComponent;
        this.$scope.notifyDependencyEventsObserver = this.notifyDependencyEventsObserver;
        this.initInstancesWithProperties();
        this.loadConstraints();

        this.initScope();
    }

    private initInstancesWithProperties = ():void => {
        this.ComponentServiceNg2.getComponentInstanceProperties(this.$scope.currentComponent).subscribe((genericResponse:ComponentGenericResponse) => {
            this.$scope.componentInstanceProperties = genericResponse.componentInstancesProperties;
            this.updateInstanceAttributes();
        });
    }

    private updateInstanceAttributes = ():void => {
        if (this.$scope.isComponentInstanceSelected() && this.$scope.componentInstanceProperties) {
            let instancesMappedList = this.$scope.service.componentInstances.map(coInstance => new ServiceInstanceObject({
                id: coInstance.uniqueId,
                name: coInstance.name,
                properties: this.$scope.componentInstanceProperties[coInstance.uniqueId] || []
            }));
            this.$scope.selectedInstanceProperties = this.$scope.componentInstanceProperties[this.$scope.currentComponent.selectedInstance.uniqueId];
            this.$scope.selectedInstanceSiblings = instancesMappedList.filter(coInstance => coInstance.id !== this.$scope.currentComponent.selectedInstance.uniqueId);
        }
    }

    private initScope = ():void => {
        this.$scope.$watch('currentComponent.selectedInstance', (newInstance:ComponentInstance):void => {
            if (angular.isDefined(newInstance) && this.$scope.componentInstancesConstraints) {
                this.updateInstanceAttributes();
                this.$scope.selectedInstanceConstraints = this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId] ?
                    this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId].properties :
                    [];
            }
        });
        this.$scope.$watch('componentInstancesConstraints', (constraints: Array<any>):void => {
            if (angular.isDefined(constraints)) {
                if(this.$scope.isComponentInstanceSelected()) {
                    this.$scope.selectedInstanceConstraints = this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId] ?
                        this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId].properties || [] :
                        [];
                }
            }
        });

        this.$scope.updateSelectedInstanceConstraints = (constraintsList:Array<ConstraintObject>):void => {
            this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId].properties = constraintsList;
            this.$scope.selectedInstanceConstraints = this.$scope.componentInstancesConstraints[this.$scope.currentComponent.selectedInstance.uniqueId].properties;
        }

        this.$scope.loadConstraints = ():void => {
            this.loadConstraints();
        }

        this.$scope.registerCreateInstanceEvent(() => {
            this.initInstancesWithProperties();
        });

        this.$scope.registerChangeComponentInstanceNameEvent((updatedComponentInstance) => {
            this.$scope.currentComponent.selectedInstance = updatedComponentInstance;
        });

        this.$scope.$on('$destroy', this.$scope.unregisterCreateInstanceEvent);
        this.$scope.$on('$destroy', this.$scope.unregisterChangeComponentInstanceNameEvent);
    }

    private loadConstraints = ():void => {
        this.ComponentServiceNg2.getServiceFilterConstraints(this.$scope.service).subscribe((response) => {
            this.$scope.componentInstancesConstraints = response.nodeFilterData;
        });
    }

    public notifyDependencyEventsObserver = (isChecked: boolean):void => {
        this.eventListenerService.notifyObservers(DEPENDENCY_EVENTS.ON_DEPENDENCY_CHANGE, isChecked);
    }
}