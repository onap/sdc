/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
import {Injectable} from '@angular/core';
import * as _ from 'lodash';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import {Component, InputBEModel, InputFEModel, PropertyModel} from 'app/models';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentInstanceServiceNg2} from 'app/ng2/services/component-instance-services/component-instance.service';
import {TopologyTemplateService} from 'app/ng2/services/component-services/topology-template.service';
import {CompositionService} from 'app/ng2/pages/composition/composition.service';
import {WorkspaceService} from 'app/ng2/pages/workspace/workspace.service';

/**
 * Everything the save routing needs, assembled by the component from its reactive form + the launch inputs.
 * This is the pure-data contract between {@link PropertyFormModalComponent#save} and this BE-call-only service.
 * The component is responsible for description strip/sanitize and complex-value serialization BEFORE calling
 * save() — this service only routes to the correct backend endpoint (mirroring the old view-model's save()).
 */
export interface PropertyFormModalSaveContext {
    property: PropertyModel;
    component: Component;
    filteredProperties: PropertyModel[];
    currentPropertyIndex: number;
    isPropertyValueOwner: boolean;
    propertyOwnerType: string;
    propertyOwnerId: string;
    inputProperty: InputFEModel;
    // Task 5 seam: the serialized complex value (JSON string of the recursive value editor's valueObj).
    // For scalar types the component sources property.value/defaultValue from the form and leaves this unset.
    myValueJson?: string;
}

/**
 * BE-call routing for the property edit modal, ported verbatim from
 * view-models/forms/property-forms/component-property-form/property-form-view-model.ts
 * (save() lines 354-430, updateInstanceProperties 598-616, addOrUpdateProperty 618-636, deleteProperty 638-647).
 *
 * The old view-model closed the modal itself; here save() RETURNS an Observable emitting the saved
 * PropertyModel (or void). The ModalsHandler Save callback resolves its deferred and closes the modal —
 * this service never touches the modal.
 */
@Injectable()
export class PropertyFormModalService {

    constructor(private componentService: ComponentServiceNg2,
                private componentInstanceService: ComponentInstanceServiceNg2,
                private topologyTemplateService: TopologyTemplateService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService) {
    }

    /**
     * Routes the (already-assembled) property to the correct backend endpoint, in the same branch order the
     * old view-model used. On success the shared caches (filteredProperties / componentInstancesProperties)
     * are updated in place, exactly as the old onPropertySuccess + updateInstanceProperties/addOrUpdateProperty did.
     */
    public save(ctx: PropertyFormModalSaveContext): Observable<PropertyModel | void> {
        const property: PropertyModel = ctx.property;
        const metadata = this.workspaceService.metadata;

        // Branch 1: input-property (old VM lines 357-372). The input carries the property's constraints + metadata.
        if (property.propertyView) {
            const input: InputBEModel = ctx.inputProperty;
            input.constraints = property.constraints;
            input.metadata = property.metadata;
            return this.componentService.updateComponentInputs(ctx.component, [input])
                .map((response) => {
                    console.debug('Input property updated');
                    return;
                });
        }

        // Branch 3: group owner (old VM lines 401-403).
        if (ctx.propertyOwnerType === 'group') {
            return this.componentInstanceService
                .updateComponentGroupInstanceProperties(metadata.componentType, metadata.uniqueId, ctx.propertyOwnerId, [property])
                .map((propertiesFromBE) => this.onPropertySuccess(ctx, <PropertyModel>propertiesFromBE[0]));
        }

        // Branch 4: policy owner (old VM lines 404-411).
        if (ctx.propertyOwnerType === 'policy') {
            return this.componentInstanceService
                .updateComponentPolicyInstanceProperties(metadata.componentType, metadata.uniqueId, ctx.propertyOwnerId, [property])
                .map((propertiesFromBE) => this.onPropertySuccess(ctx, <PropertyModel>propertiesFromBE[0]));
        }

        // Branch 5a: instance value-owner (old VM lines 414-419).
        if (ctx.isPropertyValueOwner) {
            return this.updateInstanceProperties(property.resourceInstanceUniqueId, [property])
                .map((propertiesFromBE) => this.onPropertySuccess(ctx, propertiesFromBE[0]));
        }

        // Branch 5b: plain property add/update (old VM lines 420-428 -> addOrUpdateProperty).
        return this.addOrUpdateProperty(ctx, property)
            .map((propertyFromBE) => this.onPropertySuccess(ctx, propertyFromBE));
    }

    // Old VM onPropertySuccess (lines 389-398), minus the modal-close: keep the shared filteredProperties
    // cache in sync and return the BE property so the ModalsHandler callback can resolve with it.
    private onPropertySuccess(ctx: PropertyFormModalSaveContext, propertyFromBE: PropertyModel): PropertyModel {
        ctx.filteredProperties[ctx.currentPropertyIndex] = propertyFromBE;
        return propertyFromBE;
    }

    // Ported verbatim from property-form-view-model.ts:598-616 — the componentInstancesProperties path-fixup.
    private updateInstanceProperties(componentInstanceId: string, properties: PropertyModel[]): Observable<PropertyModel[]> {
        const metadata = this.workspaceService.metadata;
        return this.componentInstanceService
            .updateInstanceProperties(metadata.componentType, metadata.uniqueId, componentInstanceId, properties)
            .map((newProperties) => {
                newProperties.forEach((newProperty) => {
                    if (!_.isNil(newProperty.path)) {
                        if (newProperty.path[0] === newProperty.resourceInstanceUniqueId) {
                            newProperty.path.shift();
                        }
                        // find exist instance property in parent component for update the new value ( find bu uniqueId & path)
                        const existProperty: PropertyModel = <PropertyModel>_.find(this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId], {
                            uniqueId: newProperty.uniqueId,
                            path: newProperty.path
                        });
                        const index = this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId].indexOf(existProperty);
                        this.compositionService.componentInstancesProperties[newProperty.resourceInstanceUniqueId][index] = newProperty;
                    }
                });
                return newProperties;
            });
    }

    // Ported verbatim from property-form-view-model.ts:618-636.
    private addOrUpdateProperty(ctx: PropertyFormModalSaveContext, property: PropertyModel): Observable<PropertyModel> {
        const metadata = this.workspaceService.metadata;
        if (!property.uniqueId) {
            const onSuccess = (newProperty: PropertyModel): PropertyModel => {
                ctx.filteredProperties.push(newProperty);
                return newProperty;
            };
            return this.topologyTemplateService.addProperty(metadata.componentType, metadata.uniqueId, property).map(onSuccess);
        } else {
            const onSuccess = (newProperty: PropertyModel): PropertyModel => {
                // find exist instance property in parent component for update the new value ( find bu uniqueId )
                const existProperty: PropertyModel = <PropertyModel>_.find(ctx.filteredProperties, {uniqueId: newProperty.uniqueId});
                const propertyIndex = ctx.filteredProperties.indexOf(existProperty);
                ctx.filteredProperties[propertyIndex] = newProperty;
                return newProperty;
            };
            return this.topologyTemplateService.updateProperty(metadata.componentType, metadata.uniqueId, property).map(onSuccess);
        }
    }

    // Ported verbatim from property-form-view-model.ts:638-647.
    public deleteProperty(componentType: string, componentId: string, propertyId: string): Observable<void> {
        const onSuccess = (): void => {
            console.debug('Property deleted');
        };
        const onFailed = (): void => {
            console.debug('Failed to delete property');
        };
        return this.topologyTemplateService.deleteProperty(componentType, componentId, propertyId).map(onSuccess, onFailed);
    }
}
