/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import * as _ from "lodash";
import {Component, ComponentRef} from '@angular/core';
import {ForwardingPath} from "app/models/forwarding-path";
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {ModalService} from "app/ng2/services/modal.service";
import {ModalComponent} from "app/ng2/components/ui/modal/modal.component";
import {CompositionService} from "app/ng2/pages/composition/composition.service";

@Component({
    selector: 'service-paths-list',
    templateUrl: './service-paths-list.component.html',
    styleUrls:['service-paths-list.component.less'],
    providers: [ServiceServiceNg2, ModalService]
})
export class ServicePathsListComponent {
    modalInstance: ComponentRef<ModalComponent>;
    headers: Array<string> = [];
    paths: Array<ForwardingPath> = [];
    input:any;
    onAddServicePath: Function;
    onEditServicePath: Function;
    isViewOnly: boolean;

    constructor(private serviceService:ServiceServiceNg2,
                private compositionService: CompositionService) {
        this.headers = ['Flow Name','Actions'];
    }

    ngOnInit() {
        _.forEach(this.compositionService.forwardingPaths, (path: ForwardingPath)=> {
            this.paths[this.paths.length] = path;
        });
        this.paths.sort((a:ForwardingPath, b:ForwardingPath)=> {
            return a.name.localeCompare(b.name);
        });
        this.onAddServicePath = this.input.onCreateServicePath;
        this.onEditServicePath = this.input.onEditServicePath;
        this.isViewOnly =  this.input.isViewOnly;
    }

    deletePath = (id:string):void =>   {
        this.serviceService.deleteServicePath(this.input.serviceId, id).subscribe((res:any) => {
            delete this.compositionService.forwardingPaths[id];
            this.paths = this.paths.filter(function(path){
                return path.uniqueId !== id;
            });
        });
    };

}