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

import { GraphColors, GraphUIObjects} from "app/utils/constants";
import constant = require("lodash/constant");
import {ImagesUrl} from "app/utils/constants";
import {AngularJSBridge} from "app/services/angular-js-bridge-service";
/**
 * Created by obarda on 12/18/2016.
 */
export class ComponentInstanceNodesStyle {

    public static getCompositionGraphStyle = ():Array<Cy.Stylesheet>  => {
        return [
            {
                selector: 'core',
                css: {
                    'shape': 'rectangle',
                    'active-bg-size': 0,
                    'selection-box-color': 'rgb(0, 159, 219)',
                    'selection-box-opacity': 0.2,
                    'selection-box-border-color': '#009fdb',
                    'selection-box-border-width': 1

                }
            },
            {
                selector: 'node',
                css: {
                    'font-family': 'OpenSans-Regular,sans-serif',

                    'font-size': 14,
                    'events': 'yes',
                    'text-events': 'yes',
                    'text-border-width': 15,
                    'text-border-color': GraphColors.NODE_UCPE,
                    'text-margin-y': 5
                }
            },
            {
                selector: '.vf-node',
                css: {
                    'background-color': 'transparent',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'width': GraphUIObjects.DEFAULT_RESOURCE_WIDTH,
                    'height': GraphUIObjects.DEFAULT_RESOURCE_WIDTH,
                    'background-opacity': 0,
                    "background-width": GraphUIObjects.DEFAULT_RESOURCE_WIDTH,
                    "background-height": GraphUIObjects.DEFAULT_RESOURCE_WIDTH,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-fit': 'cover',
                    'background-clip': 'node',
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },

            {
                selector: '.service-node',
                css: {
                    'background-color': 'transparent',
                    'label': 'data(displayName)',
                    'events': 'yes',
                    'text-events': 'yes',
                    'background-image': 'data(img)',
                    'width': 64,
                    'height': 64,
                    "border-width": 0,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.cp-node',
                css: {
                    'background-color': 'rgb(255,255,255)',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'background-width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'background-height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.vl-node',
                css: {
                    'background-color': 'rgb(255,255,255)',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'background-width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'background-height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.ucpe-cp',
                css: {
                    'background-color': GraphColors.NODE_UCPE_CP,
                    'background-width': 15,
                    'background-height': 15,
                    'width': 15,
                    'height': 15,
                    'text-halign': 'center',
                    'overlay-opacity': 0,
                    'label': 'data(displayName)',
                    'text-valign': 'data(textPosition)',
                    'text-margin-y': (ele:Cy.Collection) => {
                        return (ele.data('textPosition') == 'top') ? -5 : 5;
                    },
                    'font-size': 12
                }
            },
            {
                selector: '.ucpe-node',
                css: {
                    'background-fit': 'cover',
                    'padding-bottom': 0,
                    'padding-top': 0
                }
            },
            {
                selector: '.simple-link',
                css: {
                    'width': 1,
                    'line-color': GraphColors.BASE_LINK,
                    'target-arrow-color': '#3b7b9b',
                    'target-arrow-shape': 'triangle',
                    'curve-style': 'bezier',
                    'control-point-step-size': 30
                }
            },
            {
                selector: '.vl-link',
                css: {
                    'width': 3,
                    'line-color': GraphColors.VL_LINK,
                    'curve-style': 'bezier',
                    'control-point-step-size': 30
                }
            },
            {
                selector: '.ucpe-host-link',
                css: {
                    'width': 0
                }
            },
            {
                selector: '.not-certified-link',
                css: {
                    'width': 1,
                    'line-color': GraphColors.NOT_CERTIFIED_LINK,
                    'curve-style': 'bezier',
                    'control-point-step-size': 30,
                    'line-style': 'dashed',
                    'target-arrow-color': '#3b7b9b',
                    'target-arrow-shape': 'triangle'

                }
            },

            {
                selector: '.not-certified',
                css: {
                    'shape': 'rectangle',
                    'background-image': (ele:Cy.Collection) => {
                        return ele.data().initUncertifiedImage(ele, GraphUIObjects.NODE_OVERLAP_MIN_SIZE)
                    },
                    "border-width": 0
                }
            },
            {
                selector: 'node:selected',
                css: {
                    "border-width": 2,
                    "border-color": GraphColors.NODE_SELECTED_BORDER_COLOR,
                    'shape': 'rectangle'
                }
            },
            {
                selector: 'edge:selected',
                css: {
                    'line-color': GraphColors.ACTIVE_LINK

                }
            },
            {
                selector: 'edge:active',
                css: {
                    'overlay-opacity': 0
                }
            }, {
                selector: '.configuration-node',
                css: {
                    'background-color': 'rgb(255,255,255)',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'background-width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'background-height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'width': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'height': GraphUIObjects.SMALL_RESOURCE_WIDTH,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            }
        ]
    }

    public static getBasicNodeHanlde = () => {
        return {
            positionX: "right",
            positionY: "top",
            offsetX: 15,
            offsetY: -20,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["basic-node"],
            imageUrl: AngularJSBridge.getAngularConfig().imagesPath + ImagesUrl.CANVAS_PLUS_ICON,
            lineWidth: 2,
            lineStyle: 'dashed'

        }
    }

    public static getBasicSmallNodeHandle = () => {
        return {
            positionX: "right",
            positionY: "top",
            offsetX: 3,
            offsetY: -25,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["basic-small-node"],
            imageUrl: AngularJSBridge.getAngularConfig().imagesPath + ImagesUrl.CANVAS_PLUS_ICON,
            lineWidth: 2,
            lineStyle: 'dashed'
        }
    }

    public static getUcpeCpNodeHandle = () => {
        return {
            positionX: "center",
            positionY: "center",
            offsetX: -8,
            offsetY: -10,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["ucpe-cp-node"],
            imageUrl: AngularJSBridge.getAngularConfig().imagesPath + ImagesUrl.CANVAS_PLUS_ICON,
            lineWidth: 2,
            lineStyle: 'dashed'
        }
    }
}
