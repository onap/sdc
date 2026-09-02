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
import {CompositionCiUcpeLink} from "./composition-ci-ucpe-link";
import {Relationship, RelationshipModel} from "../../relationship";
import {GraphColors} from "../../../../utils/constants";
/**
 * Created by obarda on 4/20/2016.
 */

export class CompositionCiVlUcpeLink extends CompositionCiUcpeLink {

    constructor(relation?:RelationshipModel, from?:boolean, singleRelation?:Relationship) {
        super(relation, from, singleRelation);
        this.color = GraphColors.VL_LINK;
    }
}
