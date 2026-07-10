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
import {Component, DisplayModule, PropertyBEModel, PropertyModel} from 'app/models';
import {ComponentInstanceServiceNg2} from 'app/ng2/services/component-instance-services/component-instance.service';

/**
 * BE-call routing for the module/group property modal, ported from the AngularJS
 * ModulePropertyView.save() (view-models/forms/property-forms/module-property-modal/module-property-model.ts:72-112):
 *   resource -> PUT {id}/groups/{groupId}/properties                  (updateComponentGroupInstanceProperties)
 *   service  -> PUT {svcId}/resourceInstance/{ri}/groupInstance/{gi}  (updateGroupInstanceProperties)
 * The old save() only issued the PUT when the property was editable and its value changed; that guard lives in the
 * component (ModulePropertyModalComponent) so this service is BE-call-only.
 */
@Injectable()
export class ModulePropertyModalService {

    constructor(private componentInstanceService: ComponentInstanceServiceNg2) {
    }

    public save(component: Component, selectedModule: DisplayModule, property: PropertyModel): Observable<Array<PropertyBEModel>> {
        if (component.isResource()) {
            return this.componentInstanceService.updateComponentGroupInstanceProperties(
                component.componentType, component.uniqueId, selectedModule.uniqueId, [property]);
        }
        // Service: find the component instance whose groupInstances contain this group instance
        // (old save() lines 96-101).
        const componentInstance: any = _.find(component.componentInstances, (ci: any) =>
            _.find(ci.groupInstances, {uniqueId: selectedModule.groupInstanceUniqueId}) !== undefined);
        return this.componentInstanceService.updateGroupInstanceProperties(
            component.componentType, component.uniqueId, componentInstance.uniqueId, selectedModule.groupInstanceUniqueId, [property]);
    }
}
