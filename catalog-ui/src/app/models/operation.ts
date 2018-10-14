'use strict';

export class OperationParameter {
    name: string;
    type: String;
    property: string;
    mandatory: boolean;

    constructor(param?: OperationParameter) {
        if (param) {
            this.name = param.name;
            this.type = param.type;
            this.property = param.property;
            this.mandatory = param.mandatory;
        }
    }
}

export interface IOperationParamsList {
    listToscaDataDefinition: Array<OperationParameter>;
}

export class OperationModel {
    operationType: string;
    description: string;
    uniqueId: string;

    inputParams: IOperationParamsList;
    outputParams: IOperationParamsList;

    workflowId: string;
    workflowVersionId: string;

    constructor(operation?: any) {
        if (operation) {
            this.description = operation.description;
            this.inputParams = operation.inputParams;
            this.operationType = operation.operationType;
            this.outputParams = operation.outputParams;
            this.uniqueId = operation.uniqueId;
            this.workflowId = operation.workflowId;
            this.workflowVersionId = operation.workflowVersionId;
        }
    }

    public createInputParamsList(inputParams: Array<OperationParameter>): void {
        this.inputParams = {
            listToscaDataDefinition: inputParams
        };
    }

    public createOutputParamsList(outputParams: Array<OperationParameter>): void {
        this.outputParams = {
            listToscaDataDefinition: _.map(outputParams, output => {
                const newOutput = {...output};
                delete newOutput.property;
                return newOutput;
            })
        };
    }
}

export interface CreateOperationResponse extends OperationModel {
    artifactUUID: string;
}
