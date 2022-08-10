export class FilterConstraint {
    servicePropertyName: string;
    constraintOperator: string;
    sourceType: string;
    sourceName: string;
    value: any;

    constructor(input?: any) {
        if (input) {
            this.servicePropertyName = input.servicePropertyName;
            this.constraintOperator = input.constraintOperator;
            this.sourceType = input.sourceType;
            this.sourceName = input.sourceName;
            this.value = input.value;
        }
    }
}
