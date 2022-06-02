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

    async read(serviceCsarBlob:Blob) {
        await this.readToscaMeta(serviceCsarBlob).then(() => {
            this.readEntryDefinitionFileName();
            this.readInterfaceDefinitionFileName();
        });
        await this.readEntryDefinitionFile(serviceCsarBlob).then(() => {
            this.readServiceMetadata();
        });
        await this.readInterfaceDefinitionFile(serviceCsarBlob).then(() => {
            this.readServiceSubstitutionNode();
        });
        return this.serviceCsar;
    }

    private readToscaMeta(serviceCsarBlob:Blob) {
        let JSZip = require("jszip");
        return JSZip.loadAsync(serviceCsarBlob).then(zip => {
            return zip.file("TOSCA-Metadata/TOSCA.meta").async("string");
        }).then((toscaMetaData: string) => {
            let fileEntities:Array<string> = toscaMetaData.replace("\r", "").split("\n");
            for(let entity of fileEntities.filter(e => e)) {
                let mapEntry:Array<string> = entity.split(":");
                let key:string = mapEntry[0].trim();
                let value:string = mapEntry[1].trim();
                this.serviceCsar.toscaMeta.dataMap.set(key, value);
            }
        });
    }

    private readEntryDefinitionFileName() {
        this.serviceCsar.entryDefinitionFileName = this.serviceCsar.toscaMeta.getEntry(ToscaMetaEntry.ENTRY_DEFINITIONS);
    }

    private readInterfaceDefinitionFileName() {
        let fileNameArray:Array<string> = this.serviceCsar.entryDefinitionFileName.split(".");
            fileNameArray.splice(fileNameArray.length - 1, 0, "-interface.");
            this.serviceCsar.interfaceDefinitionFileName = fileNameArray.join("");
    }

    private readEntryDefinitionFile(serviceCsarBlob:Blob) {
        let JSZip = require("jszip");
        return JSZip.loadAsync(serviceCsarBlob).then(zip => {
            return zip.file(this.serviceCsar.entryDefinitionFileName).async("string");
        }).then((content: string) => {
            this.serviceCsar.entryDefinitionFile = content;
        });
    }

    private readServiceMetadata() {
        const metadata = load(this.serviceCsar.entryDefinitionFile).metadata;
        this.setMetadata(metadata);
    }

    private readInterfaceDefinitionFile(serviceCsarBlob:Blob) {
        let JSZip = require("jszip");
        return JSZip.loadAsync(serviceCsarBlob).then(zip => {
            return zip.file(this.serviceCsar.interfaceDefinitionFileName).async("string");
        }).then((content: string) => {
            this.serviceCsar.interfaceDefinitionFile = content;
        });
    }

    private readServiceSubstitutionNode() {
        const nodeTypes = load(this.serviceCsar.interfaceDefinitionFile).node_types;
        let nodeType = Object.keys(nodeTypes).values().next().value;
        this.serviceCsar.substitutionNodeType = nodeTypes[nodeType]["derived_from"];
    }

    private setMetadata = (metadata:object) : void => {
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
            }
        });
    }
}