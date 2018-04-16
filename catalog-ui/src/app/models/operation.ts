'use strict';

export class OperationParam {
    paramName: string = '';
    paramId: string = '';

    constructor(param?: OperationParam) {
        if (param) {
            this.paramId = param.paramId;
            this.paramName = param.paramName;
        }
    }
}

export interface IOperationParamsList {
    listToscaDataDefinition: Array<OperationParam>;
}

export class OperationModel {
    description: string;
    inputParams: IOperationParamsList;
    operationType: string;
    outputParams: IOperationParamsList;
    uniqueId: string;

    constructor(operation?: any) {
        if (operation) {
            this.description = operation.description;
            this.inputParams = operation.inputParams;
            this.operationType = operation.operationType;
            this.outputParams = operation.outputParams;
            this.uniqueId = operation.uniqueId;
        }
    }

    public createInputParamsList(inputParams: Array<OperationParam>): void {
        this.inputParams = {
            listToscaDataDefinition: inputParams
        };
    }

    public createOutputParamsList(outputParams: Array<OperationParam>): void {
        this.outputParams = {
            listToscaDataDefinition: outputParams
        };
    }
}

export interface CreateOperationResponse extends OperationModel {
    artifactUUID: string;
}
