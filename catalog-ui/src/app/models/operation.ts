'use strict';

export class OperationParameter {
    name: string;
    type: String;
    inputId: string;
    required: boolean;

    constructor(param?: OperationParameter) {
        if (param) {
            this.name = param.name;
            this.type = param.type;
            this.inputId = param.inputId;
            this.required = param.required;
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

export class BEOperationModel {
    name: string;
    description: string;
    uniqueId: string;

    inputs: IOperationParamsList;
    outputs: IOperationParamsList;

    workflowAssociationType: string;
    workflowId: string;
    workflowVersionId: string;

    implementation?: { artifactUUID: string; };

    constructor(operation?: any) {
        if (operation) {
            this.name = operation.name;
            this.description = operation.description;
            this.uniqueId = operation.uniqueId;

            this.inputs = operation.inputs;
            this.outputs = operation.outputs;

            this.workflowAssociationType = operation.workflowAssociationType;
            this.workflowId = operation.workflowId;
            this.workflowVersionId = operation.workflowVersionId;
            this.implementation = operation.implementation;
        }
    }

    public createInputsList(inputs: Array<OperationParameter>): void {
        this.inputs = {
            listToscaDataDefinition: inputs
        };
    }

    public createOutputsList(outputs: Array<OperationParameter>): void {
        this.outputs = {
            listToscaDataDefinition: _.map(outputs, output => {
                delete output.inputId;
                return output;
            })
        };
    }
}

export class OperationModel extends BEOperationModel {
    interfaceType: string;
    interfaceId: string;

    constructor(operation?: any) {
        super(operation);
        if (operation) {
            this.interfaceId = operation.interfaceId;
            this.interfaceType = operation.interfaceType;
        }
    }

    public displayType(): string {
        const lastDot = this.interfaceType ? this.interfaceType.lastIndexOf('.') : -1;
        return lastDot === -1 ? this.interfaceType : this.interfaceType.substr(lastDot + 1);
    }
}

export class InterfaceModel {
    type: string;
    uniqueId: string;
    operations: Array<OperationModel>;

    constructor(interf?: any) {
        if (interf) {
            this.type = interf.type;
            this.uniqueId = interf.uniqueId;
            this.operations = interf.operations;
        }
    }

    public displayType(): string {
        const lastDot = this.type ? this.type.lastIndexOf('.') : -1;
        return lastDot === -1 ? this.type : this.type.substr(lastDot + 1);
    }
}
