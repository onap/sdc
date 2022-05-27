/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {ToscaGetFunctionType} from './tosca-get-function-type';
import {PropertySource} from './property-source';

export class ToscaGetFunctionDto {
    propertyUniqueId: string;
    propertyName: string;
    propertySource: PropertySource;
    sourceUniqueId: string;
    sourceName: string;
    functionType: ToscaGetFunctionType;
    propertyPathFromSource: Array<string>;
}

export class ToscaGetFunctionDtoBuilder {
    toscaGetFunctionDto: ToscaGetFunctionDto = new ToscaGetFunctionDto();

    withPropertyUniqueId(propertyUniqueId: string): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.propertyUniqueId = propertyUniqueId;
        return this;
    }

    withPropertyName(propertyName: string): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.propertyName = propertyName;
        return this;
    }

    withPropertySource(propertySource: PropertySource): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.propertySource = propertySource;
        return this;
    }

    withSourceUniqueId(sourceUniqueId: string): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.sourceUniqueId = sourceUniqueId;
        return this;
    }

    withSourceName(sourceName: string): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.sourceName = sourceName;
        return this;
    }

    withFunctionType(functionType: ToscaGetFunctionType): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.functionType = functionType;
        return this;
    }

    withPropertyPathFromSource(propertyPathFromSource: Array<string>): ToscaGetFunctionDtoBuilder {
        this.toscaGetFunctionDto.propertyPathFromSource = propertyPathFromSource;
        return this;
    }

    build(): ToscaGetFunctionDto {
        return this.toscaGetFunctionDto;
    }
}
