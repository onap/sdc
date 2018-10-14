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

export class WORKFLOW_ASSOCIATION_OPTIONS {
    static NONE = 'NONE';
    static NEW = 'NEW';
    static EXISTING = 'EXISTING';
}

export class OperationModel {
    operationType: string;
    description: string;
    uniqueId: string;

    inputParams: IOperationParamsList;
    outputParams: IOperationParamsList;

    workflowAssociationType: string;
    workflowId: string;
    workflowVersionId: string;

    constructor(operation?: any) {
        if (operation) {
            this.operationType = operation.operationType;
            this.description = operation.description;
            this.uniqueId = operation.uniqueId;

            this.inputParams = operation.inputParams;
            this.outputParams = operation.outputParams;

            this.workflowAssociationType = operation.workflowAssociationType;
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
