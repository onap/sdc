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
 * Created by obarda on 5/1/2016.
 */
'use strict';
import * as _ from "lodash";
import {CompositionCiLinkBase} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-link-base';
import {CompositionCiSimpleLink} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-simple-link';
import {LinkUcpeHost} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-ucpe-host-link';
import {CompositionCiUcpeLink} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-ucpe-link';
import {CompositionCiVLink} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-vl-link';
import {CompositionCiVlUcpeLink} from 'app/models/graph/graph-links/composition-graph-links/composition-ci-vl-ucpe-link';
import {ModuleCiLinkBase} from 'app/models/graph/graph-links/module-graph-links/module-ci-link-base';
import {ModuleCiVlLink} from 'app/models/graph/graph-links/module-graph-links/module-ci-vl-link';
import {CompositionCiNodeBase} from 'app/models/graph/nodes/composition-graph-nodes/composition-ci-node-base';
import {Relationship, RelationshipModel} from 'app/models/graph/relationship';
import {Injectable} from "@angular/core";

@Injectable()
export class LinksFactory {

    constructor() {
    }

    public createGraphLink = (cy:Cy.Instance, relation:RelationshipModel, singleRelation:Relationship):CompositionCiLinkBase => {

        let newRelation:CompositionCiLinkBase;
        if (singleRelation.relation.relationship.type && _.includes(singleRelation.relation.relationship.type.toLowerCase(), 'link')) {
            newRelation = new CompositionCiVLink(relation, singleRelation);
        } else {
            newRelation = new CompositionCiSimpleLink(relation, singleRelation);
        }

        return newRelation;
    };

    public createUcpeHostLink = (relation:RelationshipModel):LinkUcpeHost => {
        return new LinkUcpeHost(relation);
    };

    public createVLLink = (relation:RelationshipModel):CompositionCiVLink => {
        return new CompositionCiVLink(relation);
    }

    public createModuleGraphLinks = (relation:RelationshipModel, singleRelation:Relationship):ModuleCiLinkBase => {

        let newRelation:ModuleCiLinkBase;

        if (_.includes(singleRelation.relation.relationship.type.toLowerCase(), 'link')) {
            newRelation = new ModuleCiVlLink(relation, singleRelation);
        } else {
            newRelation = new ModuleCiLinkBase(relation, singleRelation);
        }

        return newRelation;
    };

}
