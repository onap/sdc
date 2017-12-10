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

import {ComponentInstance} from "../../../componentsInstances/componentInstance";
import {CommonCINodeBase} from "../common-ci-node-base";
import {ImageCreatorService} from "app/directives/graphs-v2/image-creator/image-creator.service";
import {ImagesUrl, GraphUIObjects} from "app/utils";
import {AngularJSBridge} from "app/services";

export interface ICompositionCiNodeBase {

}

export abstract class CompositionCiNodeBase extends CommonCINodeBase implements ICompositionCiNodeBase {

    public textPosition:string; //need to move to cp UCPE
    public isUcpe:boolean;
    public isInsideGroup:boolean;
    public isUcpePart:boolean;

    constructor(instance:ComponentInstance,
                public imageCreator:ImageCreatorService) {
        super(instance);
        this.init();
    }

    private init() {

        this.displayName = this.getDisplayName();
        this.isUcpe = false;
        this.isGroup = false;
        this.isUcpePart = false;
        this.isInsideGroup = false;
    }

    public initUncertifiedImage(node:Cy.Collection, nodeMinSize:number):string {
        
        let uncertifiedIconWidth:number = GraphUIObjects.HANDLE_SIZE;
        let nodeWidth:number = node.data('imgWidth') || node.width();
        let uncertifiedCanvasWidth: number = nodeWidth;
        
        if (nodeWidth < nodeMinSize) { //uncertified icon will overlap too much of the node, need to expand canvas.
            uncertifiedCanvasWidth = nodeWidth + uncertifiedIconWidth/2; //expand canvas so that only half of the icon overlaps with the node
        }
        


        this.imageCreator.getImageBase64(this.imagesPath + this.componentInstance.icon + '.png',
            this.imagesPath + 'uncertified.png', nodeWidth, uncertifiedCanvasWidth, uncertifiedIconWidth) 
            .then(imageBase64 => {
                this.img = imageBase64;
                node.style({
                    'background-image': this.img,
                    'background-width': uncertifiedCanvasWidth,
                    'background-height': uncertifiedCanvasWidth,
                    'width': uncertifiedCanvasWidth,
                    'height': uncertifiedCanvasWidth
                });
            });

        return this.img;
    }

    protected getDisplayName():string {

        let graphResourceName = AngularJSBridge.getFilter('graphResourceName');
        let resourceName = AngularJSBridge.getFilter('resourceName');
        return graphResourceName(resourceName(this.componentInstance.name));
    }

}
