import {FilterConstraint} from "../filter-constraint";

export class ConstraintObjectUI extends FilterConstraint {
    isValidValue: boolean;

    constructor(input?: any) {
        super(input);
        if (input) {
            this.isValidValue = input.isValidValue ? input.isValidValue : input.value !== '';
        }
    }

    public updateValidity(isValidValue: boolean) {
        this.isValidValue = isValidValue;
    }

    public isValidRule() {
        const isValidValue = this.isStatic() ? this.isValidValue : true;
        return this.servicePropertyName != null && this.servicePropertyName !== ''
            && this.value != null && this.value !== '' && isValidValue;
    }

    private isStatic() {
        return this.sourceName === 'static';
    }
}