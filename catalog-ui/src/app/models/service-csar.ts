/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import {ComponentMetadata} from "./component-metadata";

export enum ToscaMetaEntry {
    ENTRY_DEFINITIONS = "Entry-Definitions"
}

export class ServiceCsar {

    entryDefinitionFileName: string;
    interfaceDefinitionFileName: string;
    entryDefinitionFile: string;
    interfaceDefinitionFile:string;
    substitutionNodeType:string;
    toscaMeta: ToscaMeta = new ToscaMeta();
    serviceMetadata: ComponentMetadata = new ComponentMetadata();

}

export class ToscaMeta {

    dataMap: Map<string, string> = new Map<string, string>()

    getEntry(toscaMetaEntry: ToscaMetaEntry): string {
        return this.dataMap.get(toscaMetaEntry.valueOf());
    }

}
