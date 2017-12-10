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

import {RelationshipModel, Relationship} from "../../relationship";
import {CompositionCiLinkBase} from "./composition-ci-link-base";
import {GraphColors} from "../../../../utils/constants";
export class CompositionCiUcpeLink extends CompositionCiLinkBase {

    isFromUcpe:boolean;

    constructor(relation?:RelationshipModel, from?:boolean, singleRelation?:Relationship) {
        super(relation, singleRelation);
        this.isFromUcpe = from;
        this.target = relation.toNode;
        this.source = singleRelation.relation.requirementOwnerId;
        this.relation.relationships = [singleRelation];
        this.color = GraphColors.BASE_LINK;
    }

    updateLinkDirection():void {
    }
}
