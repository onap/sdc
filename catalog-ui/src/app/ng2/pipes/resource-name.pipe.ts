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
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
 */


import { Pipe, PipeTransform } from '@angular/core';
import * as _ from 'lodash';

/**
 * Namespace prefixes stripped from a TOSCA type/resource name to build its short display name.
 * The name is split on each token in turn, keeping the LAST segment every time, so a name
 * matching several tokens is reduced by the last-matching token in this ordered chain.
 *
 * Ported verbatim from the AngularJS `resourceName` filter (SDC-4829 Phase 12).
 */
const STRIPPED_PREFIXES: string[] = [
    'tosca.nodes.',
    'network.',
    'relationships.',
    'org.openecomp.',
    'resource.nfv.',
    'nodes.module.',
    'cp.',
    'vl.'
];

@Pipe({name: 'resourceName'})
export class ResourceNamePipe implements PipeTransform {

    /**
     * Falsy input is returned unchanged, as the AngularJS `resourceName` filter did. Callers reach
     * this helper directly (MenuHandler builds a breadcrumb label for a component that has no name
     * yet in create mode), so the guard cannot live in `transform` alone.
     */
    public static getDisplayName (value:string): string {
        if (!value) {
            return value;
        }
        const stripped: string = STRIPPED_PREFIXES.reduce(
            (name: string, prefix: string) => _.last(name.split(prefix)), value);
        const shortName: string = (stripped) ? stripped : value;
        return shortName.charAt(0).toUpperCase() + shortName.slice(1);
    }

    transform(value) : any {
        if (value) {
           return ResourceNamePipe.getDisplayName(value);
        }
    }
}
