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

import {CompositionCiNodeCp, ComponentInstance} from "./../../../../models";
import {ImageCreatorService} from "app/ng2/pages/composition/graph/common/image-creator.service";
import { ImagesUrl} from "../../../../utils/constants";

export class CompositionCiNodeUcpeCp extends CompositionCiNodeCp {

    constructor(instance:ComponentInstance,
                imageCreator:ImageCreatorService) {
        super(instance, imageCreator);
        this.isUcpePart = true;
        this.classes = 'ucpe-cp'; // the css class for the node
        this.parent = instance.uniqueId;
        this.type = 'ucpe-cp-node'; //the type is for the handle (plus icon) extension
        this.isDraggable = false;
        this.imagesPath = this.imagesPath + ImagesUrl.RESOURCE_ICONS;
    }
}
