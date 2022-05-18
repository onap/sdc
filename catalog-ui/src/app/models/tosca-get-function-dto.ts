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

    buildGetFunctionValue(): string {
        const getPropertyFunctionValue = {};
        if (this.propertyPathFromSource && this.propertyPathFromSource.length) {
            getPropertyFunctionValue[this.functionType.toLowerCase()] = this.propertyPathFromSource;
        } else {
            getPropertyFunctionValue[this.functionType.toLowerCase()] = this.propertyName;
        }
        return JSON.stringify(getPropertyFunctionValue);
    }
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
