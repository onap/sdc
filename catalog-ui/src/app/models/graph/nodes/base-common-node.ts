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
/**
 * Created by obarda on 6/29/2016.
 */
'use strict';
import {AngularJSBridge} from "../../../services/angular-js-bridge-service";

export abstract class CommonNodeBase {

    public displayName:string;
    public name:string;
    public img: string;
    public imgWidth: number;
    public certified:boolean;
    public isGroup:boolean;
    public imagesPath:string;
    public isDraggable:boolean; //we need to to manage manually the dragging on the graph inside groups (ucpe-cp is not draggable)

    //cytoscape fields
    public id:string;
    public type:string; //type is for the edge edition extension, by type we put the green plus icon in position
    public isSdcElement:boolean; //this fields is in order to filter sdc elements from all extensions elements
    public classes:string;
    public parent:string;
    public allowConnection:boolean; //this is for egeEdition extension in order to decide if connection to a node is available

    constructor() {

        this.imagesPath = AngularJSBridge.getAngularConfig().imagesPath;
        this.type = "basic-node";
        this.isSdcElement = true;
        this.isDraggable = true;
        this.allowConnection = true;
    }

    public updateNameForDisplay = () => {
        let context = document.createElement("canvas").getContext("2d");
        context.font = "13px Arial";

        if (63 < context.measureText(this.name).width) {
            let newLen = this.name.length - 3;
            let newName = this.name.substring(0, newLen);

            while (60 < (context.measureText(newName).width)) {
                newName = newName.substring(0, (--newLen));
            }
            this.displayName = newName + '...';
            return;
        }

        this.displayName = this.name;
    };
}
