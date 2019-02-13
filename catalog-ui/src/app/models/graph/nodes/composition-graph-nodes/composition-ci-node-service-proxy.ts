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

import {ComponentInstance, CompositionCiNodeBase} from "app/models";
import {ImageCreatorService} from "app/directives/graphs-v2/image-creator/image-creator.service";
import {ImagesUrl, GraphUIObjects} from "app/utils";
export class CompositionCiNodeServiceProxy extends CompositionCiNodeBase {
    private isDependent: boolean;
    private originalImg: string;

    constructor(instance:ComponentInstance,
                imageCreator:ImageCreatorService) {
        super(instance, imageCreator);
        this.isDependent =instance.isDependent();
        this.initService();
    }

    private initService():void {
        this.imagesPath = this.imagesPath + ImagesUrl.SERVICE_PROXY_ICONS;
        this.img = this.imagesPath + this.componentInstance.icon + '.png';
        this.originalImg = this.img;
        this.imgWidth = GraphUIObjects.DEFAULT_RESOURCE_WIDTH;
        this.classes = 'service-node';
        if(this.archived){
            this.classes = this.classes + ' archived';
            return;
        }
        if (this.isDependent) {
            this.classes += ' dependent';
        }
        if (!this.certified) {
            this.classes = this.classes + ' not-certified';
        }

    }

    public initUncertifiedDependentImage(node:Cy.Collection, nodeMinSize:number):string {
        return this.enhanceImage(node, nodeMinSize, this.imagesPath + 'uncertified_dependent.png');
    }

    public initDependentImage(node:Cy.Collection, nodeMinSize:number):string {
        return this.enhanceImage(node, nodeMinSize, this.imagesPath + 'dependent.png');
    }

}
