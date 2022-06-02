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

import {ServiceCsar, ToscaMetaEntry} from "../models";
import {load} from 'js-yaml';
import { ComponentType } from "./constants";

export class ServiceCsarReader {

    private serviceCsar = new ServiceCsar();

    public read(serviceCsarBlob:Blob): Promise<ServiceCsar> {
        const jsZip = require("jszip");
        return new Promise<ServiceCsar>((resolve) => {
            jsZip.loadAsync(serviceCsarBlob).then(async zip => {
                const toscaMetaFileContent = await zip.file("TOSCA-Metadata/TOSCA.meta").async("string");
                this.readToscaMeta(toscaMetaFileContent);
                const entryDefinitionFileContent = await zip.file(this.serviceCsar.entryDefinitionFileName).async("string");
                this.readServiceMetadata(entryDefinitionFileContent);
                const interfaceDefinitionFileContent = await zip.file(this.serviceCsar.interfaceDefinitionFileName).async("string");
                this.readServiceSubstitutionNode(interfaceDefinitionFileContent);
                resolve(this.serviceCsar);
            });
        });
    }

    private readToscaMeta(toscaMetaFileContent:string) {
        let fileEntities:Array<string> = toscaMetaFileContent.replace("\r", "").split("\n");
        for(let entity of fileEntities.filter(e => e)) {
            let mapEntry:Array<string> = entity.split(":");
            let key:string = mapEntry[0].trim();
            let value:string = mapEntry[1].trim();
            this.serviceCsar.toscaMeta.dataMap.set(key, value);
        }
        this.readEntryDefinitionFileName();
        this.readInterfaceDefinitionFileName();
    }

    private readEntryDefinitionFileName() {
        this.serviceCsar.entryDefinitionFileName = this.serviceCsar.toscaMeta.getEntry(ToscaMetaEntry.ENTRY_DEFINITIONS);
    }

    private readInterfaceDefinitionFileName() {
        let fileNameArray:Array<string> = this.serviceCsar.entryDefinitionFileName.split(".");
        fileNameArray.splice(fileNameArray.length - 1, 0, "-interface.");
        this.serviceCsar.interfaceDefinitionFileName = fileNameArray.join("");
    }

    private readServiceMetadata(entryDefinitionFileContent) {
        const metadata = load(entryDefinitionFileContent).metadata;
        this.setMetadata(metadata);
    }

    private readServiceSubstitutionNode(interfaceDefinitionFileContent) {
        const nodeTypes = load(interfaceDefinitionFileContent).node_types;
        let nodeType = Object.keys(nodeTypes).values().next().value;
        this.serviceCsar.substitutionNodeType = nodeTypes[nodeType]["derived_from"];
    }

    private setMetadata = (metadata:object) : void => {
        let extraServiceMetadata: Map<string, string> = new Map<string, string>();
        this.serviceCsar.serviceMetadata.componentType = ComponentType.SERVICE;
        this.serviceCsar.serviceMetadata.serviceType = "Service";
        Object.keys(metadata).forEach(variable => {
            switch(variable) {
                case "description": {
                    this.serviceCsar.serviceMetadata.description = metadata[variable];
                    break;
                }
                case "name": {
                    this.serviceCsar.serviceMetadata.name = metadata[variable];
                    break;
                }
                case "model": {
                    this.serviceCsar.serviceMetadata.model = metadata[variable];
                    break;
                }
                case "category": {
                    this.serviceCsar.serviceMetadata.selectedCategory = metadata[variable];
                    break;
                }
                case "serviceRole": {
                    this.serviceCsar.serviceMetadata.serviceRole = metadata[variable];
                    break;
                }
                case "serviceFunction": {
                    this.serviceCsar.serviceMetadata.serviceFunction = metadata[variable];
                    break;
                }
                case "environmentContext": {
                    if (metadata[variable] != null) {
                        this.serviceCsar.serviceMetadata.environmentContext = metadata[variable];
                    }
                    break;
                }
                case "instantiationType": {
                    if (metadata[variable] != null) {
                        this.serviceCsar.serviceMetadata.instantiationType = metadata[variable];
                    }
                    break;
                }
                case "ecompGeneratedNaming": {
                    if (metadata[variable] != null) {
                        this.serviceCsar.serviceMetadata.ecompGeneratedNaming = metadata[variable] == "false" ? false : true;
                    }
                    break;
                }
                case "namingPolicy": {
                    if (metadata["ecompGeneratedNaming"] != "false") {
                        this.serviceCsar.serviceMetadata.namingPolicy = metadata[variable];
                    }
                    break;
                }
                default: {
                    extraServiceMetadata.set(variable, metadata[variable])
                    break;
                }
            }
        });
        this.serviceCsar.extraServiceMetadata = extraServiceMetadata;
    }
}