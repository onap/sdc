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
import { Component, ElementRef, forwardRef, Inject } from '@angular/core';
import {Link} from './link-row/link.model';
import {ForwardingPath} from 'app/models/forwarding-path';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {ForwardingPathLink} from "app/models/forwarding-path-link";
import {ServicePathMapItem} from "app/models/graph/nodes-and-links-map";
import {CompositionService} from "app/ng2/pages/composition/composition.service";

@Component({
    selector: 'service-path-creator',
    templateUrl: './service-path-creator.component.html',
    styleUrls:['./service-path-creator.component.less'],
    providers: [ServiceServiceNg2]
})

export class ServicePathCreatorComponent {

    linksMap:Array<ServicePathMapItem>;
    links:Array<Link> = [];
    input:any;
    headers: Array<string> = [];
    removeRow: Function;
    forwardingPath:ForwardingPath;
    //isExtendAllowed:boolean = false;

    constructor(private serviceService: ServiceServiceNg2,
                private compositionService: CompositionService) {
        this.forwardingPath = new ForwardingPath();
        this.links = [new Link(new ForwardingPathLink('', '', '', '', '', ''), true, false, true)];
        this.headers = ['Source', 'Source Connection Point', 'Target', 'Target Connection Point', ' '];
        this.removeRow = () => {
            if (this.links.length === 1) {
                return;
            }
            this.links.splice(this.links.length-1, 1);
            this.enableCurrentRow();
        };
    }

    ngOnInit() {
        this.serviceService.getNodesAndLinksMap(this.input.serviceId).subscribe((res:any) => {
            this.linksMap = res;
        });
        this.processExistingPath();

    }

    private processExistingPath() {
        if (this.input.pathId) {
            let forwardingPath = <ForwardingPath>{...this.compositionService.forwardingPaths[this.input.pathId]};
            this.forwardingPath.name = forwardingPath.name;
            this.forwardingPath.destinationPortNumber = forwardingPath.destinationPortNumber;
            this.forwardingPath.protocol = forwardingPath.protocol;
            this.forwardingPath.uniqueId = forwardingPath.uniqueId;
            this.links = [];
            _.forEach(forwardingPath.pathElements.listToscaDataDefinition, (link:ForwardingPathLink) => {
                this.links[this.links.length] = new Link(link, false, false, false);
            });
            this.links[this.links.length - 1].canEdit = true;
            this.links[this.links.length - 1].canRemove = true;
            this.links[0].isFirst = true;
        }
    }

    isExtendAllowed():boolean {
        if (this.links[this.links.length-1].toCP) {
            return true;
        }
        return false;
    }

    enableCurrentRow() {
        this.links[this.links.length-1].canEdit = true;
        if (this.links.length !== 1) {
            this.links[this.links.length-1].canRemove = true;
        }
    }

    addRow() {
        this.disableRows();
        this.links[this.links.length] = new Link(
            new ForwardingPathLink(this.links[this.links.length-1].toNode,
                this.links[this.links.length-1].toCP,
                '',
                '',
                this.links[this.links.length-1].toCPOriginId,
                ''
            ),
            true,
            true,
            false
        );
    }

    disableRows() {
        for (let i = 0 ; i < this.links.length ; i++) {
            this.links[i].canEdit = false;
            this.links[i].canRemove = false;
        }
    }

    createPathLinksObject() {
        for (let i = 0 ; i < this.links.length ; i++) {
            let link = this.links[i];
            this.forwardingPath.addPathLink(link.fromNode, link.fromCP, link.toNode, link.toCP, link.fromCPOriginId, link.toCPOriginId);
        }
    }

    createServicePathData() {
        this.createPathLinksObject();
        return this.forwardingPath;
    }

    checkFormValidForSubmit():boolean {
        if (this.forwardingPath.name && this.isPathValid() ) {
            return true;
        }
        return false;
    }

    isPathValid():boolean {
        let lastLink = this.links[this.links.length -1] ;
        if (lastLink.toNode && lastLink.toCP && lastLink.fromNode && lastLink.fromCP) {
            return true;
        }
        return false;
    }
}