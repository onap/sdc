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

import {GraphColors} from "app/utils";
export class ModulesNodesStyle {

    public static getModuleGraphStyle = ():Array<Cy.Stylesheet> => {

        return [
            {
                selector: '.cy-expand-collapse-collapsed-node',
                css: {
                    'background-image': 'data(img)',
                    'width': 34,
                    'height': 32,
                    'background-opacity': 0,
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'events': 'yes',
                    'text-events': 'yes',
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'text-margin-y': 5,
                    'border-opacity': 0
                }
            },
            {
                selector: '.module-node',
                css: {
                    'background-color': 'transparent',
                    'background-opacity': 0,
                    "border-width": 2,
                    "border-color": GraphColors.NODE_SELECTED_BORDER_COLOR,
                    'border-style': 'dashed',
                    'label': 'data(displayName)',
                    'events': 'yes',
                    'text-events': 'yes',
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'text-margin-y': 8
                }
            },
            {
                selector: 'node:selected',
                css: {
                    "border-opacity": 0
                }
            },
            {
                selector: '.simple-link:selected',
                css: {
                    'line-color': GraphColors.BASE_LINK,
                }
            },
            {
                selector: '.vl-link:selected',
                css: {
                    'line-color': GraphColors.VL_LINK,
                }
            },
            {
                selector: '.cy-expand-collapse-collapsed-node:selected',
                css: {
                    "border-color": GraphColors.NODE_SELECTED_BORDER_COLOR,
                    'border-opacity': 1,
                    'border-style': 'solid',
                    'border-width': 2
                }
            },
            {
                selector: '.module-node:selected',
                css: {
                    "border-color": GraphColors.NODE_SELECTED_BORDER_COLOR,
                    'border-opacity': 1
                }
            },
            {
                selector: '.dummy-node',
                css: {
                    'width': 20,
                    'height': 20
                }
            },
        ]
    }
}
