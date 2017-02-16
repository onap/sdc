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
/// <reference path="../../../references"/>
module Sdc.Models {

    export interface ICommonCiLinkBase {

    }

    export class CommonCiLinkBase extends CommonLinkBase implements ICommonCiLinkBase {

        relation:RelationshipModel;


        constructor(relation?:RelationshipModel, singleRelationship?:Models.Relationship) {
            super();
            if (relation) {
                if(singleRelationship){
                    this.relation = new Models.RelationshipModel(relation, singleRelationship);
                }else{
                    this.relation = new Models.RelationshipModel(relation);
                }
                this.source = relation.fromNode;
                this.target = relation.toNode;
            } else {
                this.relation = new RelationshipModel();
            }
         }
    }
}
