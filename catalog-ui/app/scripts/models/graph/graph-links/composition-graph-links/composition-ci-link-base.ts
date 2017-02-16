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
module Sdc.Models {

    export interface ICompositionCiLinkBase extends ICommonCiLinkBase{
        updateLinkDirection():void;
    }

    export class CompositionCiLinkBase extends CommonCiLinkBase implements ICompositionCiLinkBase {

        type:string;
        visible:boolean;

        constructor(relation?:RelationshipModel, singleRelationship?:Models.Relationship) {
            super(relation, singleRelationship);
            this.visible = true;
        }

        public setRelation = (relation: Models.RelationshipModel) => {
            this.relation = relation;
        };

        updateLinkDirection():void{
            this.source = this.relation.fromNode;
            this.target = this.relation.toNode;
        }
    }
}
