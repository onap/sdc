'use strict';

export class OperationParameter {
    name: string;
    type: string;
    inputId?: string;
    required?: boolean;
    property?: string;
    mandatory?: boolean;

    constructor(param?: any) {
        if (param) {
            this.name = param.name;
            this.type = param.type;
            this.inputId = param.inputId ;
            this.required = param.required;
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
    static EXTERNAL = 'EXTERNAL';
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

    implementation?: {
        artifactName: string;
        artifactUUID: string;
    };

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
            this.implementation = operation.implementation || {};
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

export class OperationModel extends BEOperationModel{
    interfaceType: string;
    interfaceId: string;
    operationType: string;
    description: string;
    uniqueId: string;
    artifactFileName?: string;
    artifactData?: any;

    inputParams: IOperationParamsList;
    outputParams: IOperationParamsList;

    workflowId: string;
    workflowVersionId: string;

    protected OperationTypeEnum: Array<String> = [
        'Create',
        'Delete',
        'Instantiate',
        'Start',
        'Stop'
    ];

    constructor(operation?: any) {
        super(operation);
        if (operation) {
            this.interfaceId = operation.interfaceId;
            this.interfaceType = operation.interfaceType;
            this.description = operation.description;
            this.inputParams = operation.inputParams;
            this.operationType = operation.operationType;
            this.outputParams = operation.outputParams;
            this.uniqueId = operation.uniqueId;
            this.workflowId = operation.workflowId;
            this.workflowVersionId = operation.workflowVersionId;
            this.artifactFileName = operation.artifactFileName;
            this.artifactData = operation.artifactData;
        }
    }

    public displayType(): string {
        return displayType(this.interfaceType);
    }

    public createInputParamsList(inputParams: Array<OperationParameter>): void {
        this.inputParams = {
            listToscaDataDefinition: inputParams
        };
    }

    public createOutputParamsList(outputParams: Array<OperationParameter>): void {
        this.outputParams = {
            listToscaDataDefinition: outputParams
        };
    }
}

export interface CreateOperationResponse extends OperationModel {
    artifactUUID: string;
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
        return displayType(this.type);
    }
}

const displayType = (type:string) => type && type.substr(type.lastIndexOf('.') + 1);