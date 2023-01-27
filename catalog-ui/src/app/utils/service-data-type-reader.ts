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

import {DataTypeModel, PropertyBEModel} from "../models";
import {load} from 'js-yaml';

export class ServiceDataTypeReader {

    private serviceDataType = new DataTypeModel();

    public read(dataTypeFile: File): Promise<DataTypeModel> {

        return new Promise<DataTypeModel>((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                try {
                    const result = <String>reader.result;
                    const loadedContent = load(result);
                    console.log("Readed content: " + loadedContent);
                    this.readName(this.getDataType(loadedContent));
                    this.readDerivedFrom(this.getDataType(loadedContent));
                    this.readDescription(this.getDataType(loadedContent));
                    this.readProperties(this.getDataType(loadedContent));
                    resolve(this.serviceDataType);
                } catch (error) {
                    reject(error);
                }
            }
            reader.readAsText(dataTypeFile);
        });
    }

    private getDataType(fileContent:any) {
        const index = Object.keys(fileContent).indexOf("data_types",0)
        if (index == -1){
            return fileContent;
        }
        return fileContent["data_types"];
    }

    private readName(fileContent: any) {
        this.serviceDataType.name = Object.keys(fileContent).values().next().value;
    }

    private readDerivedFrom(fileContent: any) {
        let dataType = Object.keys(fileContent).values().next().value;
        this.serviceDataType.derivedFromName = fileContent[dataType]["derived_from"];
    }

    private readDescription(fileContent: any) {
        let dataType = Object.keys(fileContent).values().next().value;
        this.serviceDataType.description = fileContent[dataType]["description"];
    }

    private readProperties(fileContent: any) {
        this.serviceDataType.properties = new Array<PropertyBEModel>();
        let dataType = Object.keys(fileContent).values().next().value;
        const properties = fileContent[dataType]["properties"];
        Object.keys(properties).forEach((key )=>
            {
                let property = new PropertyBEModel();
                property.name = key;
                property.description = properties[key]["description"];
                property.type = properties[key]["type"];
                property.schemaType = properties[key]["schema"];
                property.required = properties[key]["required"];
                this.serviceDataType.properties.push(property);
            }
        );
    }
}