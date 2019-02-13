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
import {ICanvasImage, ImageCreatorService} from "app/directives/graphs-v2/image-creator/image-creator.service";
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


    protected enhanceImage(node:Cy.Collection, nodeMinSize:number, imgUrl: string):string {
        let infoIconWidth:number = GraphUIObjects.HANDLE_SIZE;
        let nodeWidth:number = node.data('imgWidth') || node.width();
        let infoCanvasWidth: number = nodeWidth;

        if (nodeWidth < nodeMinSize) { //info icon will overlap too much of the node, need to expand canvas.
            infoCanvasWidth = nodeWidth + infoIconWidth/2; //expand canvas so that only half of the icon overlaps with the node
        }

        const x = infoCanvasWidth - nodeWidth, y = x, width = nodeWidth, height = width;

        const canvasImages:ICanvasImage[] = [
            { src: this.imagesPath + this.componentInstance.icon + '.png', x, y, width, height},
            { src: imgUrl, x: 0, y: 0, width: infoIconWidth, height: infoIconWidth}
        ];

       //Create the image and update the node background styles
        this.imageCreator.getMultiLayerBase64Image(canvasImages, infoCanvasWidth, infoCanvasWidth).then(img => this.updateNodeStyles(node,infoCanvasWidth,img));
        return this.img; // Return the referance to the image (in Base64 format)
    }


    public setArchivedImageBgStyle(node:Cy.Collection, nodeMinSize:number):string {
        let archivedIconWidth:number = GraphUIObjects.HANDLE_SIZE;
        let nodeWidth:number = node.data('imgWidth') || node.width();
        let archivedCanvasWidth: number = nodeWidth;

        const x = archivedCanvasWidth - nodeWidth, y = x, width = nodeWidth, height = width;
        const archiveImage = nodeWidth < 50? 'archive_small.png':'archive_big.png';

        const canvasImages = [
            { src: this.imagesPath + this.componentInstance.icon + '.png', x, y, width, height},
            { src: AngularJSBridge.getAngularConfig().imagesPath + ImagesUrl.RESOURCE_ICONS + archiveImage, x, y, width, height}
        ];

        //Create the image and update the node background styles
        this.imageCreator.getMultiLayerBase64Image(canvasImages, archivedCanvasWidth, archivedCanvasWidth).then(img => this.updateNodeStyles(node, archivedCanvasWidth, img));
        return this.img; // Return the default img
    }

    public initUncertifiedImage(node:Cy.Collection, nodeMinSize:number):string {
        return this.enhanceImage(node, nodeMinSize, this.imagesPath + 'uncertified.png');
    }

    protected getDisplayName():string {
        let graphResourceName = AngularJSBridge.getFilter('graphResourceName');
        let resourceName = AngularJSBridge.getFilter('resourceName');
        return graphResourceName(resourceName(this.componentInstance.name));
    }

    //TODO:: move to Base class ???
    private updateNodeStyles(node,canvasWidth,imageBase64){
        this.img = imageBase64;
        node.style({
            'background-image': this.img,
            'background-width': canvasWidth,
            'background-height': canvasWidth,
            'background-position-x':0,
            'background-position-y':0
        });
    }

}
