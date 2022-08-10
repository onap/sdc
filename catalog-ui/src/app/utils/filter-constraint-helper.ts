import {FilterConstraint} from "../models/filter-constraint";
import {ToscaFunctionType} from "../models/tosca-function-type.enum";
import {ToscaConcatFunction} from "../models/tosca-concat-function";
import {ToscaGetFunction} from "../models/tosca-get-function";
import {YamlFunction} from "../models/yaml-function";
import {CapabilityFilterConstraint} from "../models/capability-filter-constraint";

export class FilterConstraintHelper {

    public static buildFilterConstraintLabel(constraint: FilterConstraint | CapabilityFilterConstraint): string {
        let value;
        if (this.isValueToscaFunction(constraint.value)) {
            switch (constraint.value.type) {
                case ToscaFunctionType.CONCAT:
                    value = new ToscaConcatFunction(constraint.value).buildValueString();
                    break;
                case ToscaFunctionType.GET_PROPERTY:
                case ToscaFunctionType.GET_INPUT:
                case ToscaFunctionType.GET_ATTRIBUTE:
                    value = new ToscaGetFunction(constraint.value).buildValueString();
                    break;
                case ToscaFunctionType.YAML:
                    value = new YamlFunction(constraint.value).buildValueString();
                    break;
                case ToscaFunctionType.STRING:
                    value = constraint.value.value;
                    break;
                default:
                    value = JSON.stringify(constraint.value, null, 4);
            }
        } else {
            value = JSON.stringify(constraint.value, null, 4);
        }
        if (constraint instanceof CapabilityFilterConstraint) {
            return `${constraint.capabilityName}: ${constraint.servicePropertyName} ${this.convertToSymbol(constraint.constraintOperator)} ${value}`;
        }

        return `${constraint.servicePropertyName} ${this.convertToSymbol(constraint.constraintOperator)} ${value}`;
    }

    public static convertToSymbol(constraintOperator: string) {
        switch (constraintOperator) {
            case OPERATOR_TYPES.LESS_THAN: return '<';
            case OPERATOR_TYPES.EQUAL: return '=';
            case OPERATOR_TYPES.GREATER_THAN: return '>';
            case OPERATOR_TYPES.GREATER_OR_EQUAL: return '>=';
            case OPERATOR_TYPES.LESS_OR_EQUAL: return '<=';
        }
    }

    private static isValueToscaFunction(value: any): boolean {
        return value instanceof Object && 'type' in value && (<any>Object).values(ToscaFunctionType).includes(value.type);
    }

}

export const OPERATOR_TYPES = {
    EQUAL: 'equal',
    GREATER_THAN: 'greater_than',
    LESS_THAN: 'less_than',
    GREATER_OR_EQUAL: 'greater_or_equal',
    LESS_OR_EQUAL: 'less_or_equal'
};

