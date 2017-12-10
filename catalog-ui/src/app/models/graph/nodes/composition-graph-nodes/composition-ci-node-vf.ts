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
import {CompositionCiNodeBase} from "./composition-ci-node-base";
import {ComponentInstance} from "../../../componentsInstances/componentInstance";
import {ImageCreatorService} from "../../../../directives/graphs-v2/image-creator/image-creator.service";
import { ImagesUrl, GraphUIObjects} from "../../../../utils/constants";

export class CompositionCiNodeVf extends CompositionCiNodeBase {

    constructor(instance:ComponentInstance,
                imageCreator:ImageCreatorService) {
        super(instance, imageCreator);
        this.initVf();
    }

    private initVf():void {
        this.imagesPath = this.imagesPath + ImagesUrl.RESOURCE_ICONS;
        this.img = this.imagesPath + this.componentInstance.icon + '.png';
        this.imgWidth = GraphUIObjects.DEFAULT_RESOURCE_WIDTH;
        this.classes = 'vf-node';
        if (!this.certified) {
            this.classes = this.classes + ' not-certified';
        }
    }

}
