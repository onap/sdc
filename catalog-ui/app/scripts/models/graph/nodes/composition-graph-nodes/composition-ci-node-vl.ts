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
/// <reference path="../../../../references"/>

module Sdc.Models.Graph {

    export class CompositionCiNodeVl extends CompositionCiNodeBase {
        private toolTipText:string;
      
        constructor(instance:Models.ComponentsInstances.ComponentInstance, imageCreator: Utils.ImageCreatorService) {
            super(instance, imageCreator);
            this.initVl();

        }

        private initVl():void {
            this.type = "basic-small-node";
            this.toolTipText = 'Point to point';
            let key:string = _.find(Object.keys(this.componentInstance.capabilities), (key)=> {
                return _.includes(key.toLowerCase(), 'linkable');
            });
            let linkable = this.componentInstance.capabilities[key];
            if (linkable) {
                if ('UNBOUNDED' == linkable[0].maxOccurrences) {
                    this.toolTipText = 'Multi point';
                }
            }
            this.img = this.imagesPath + Utils.Constants.ImagesUrl.RESOURCE_ICONS + 'vl.png';

            this.classes = 'vl-node';
            if(!this.certified) {
                this.classes = this.classes + ' not-certified';
            }
        }

    }
}
