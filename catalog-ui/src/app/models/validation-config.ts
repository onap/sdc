class PropertyValue {
    min: number;
    max: number;
}

class validationPatterns {
    string: string;
    comment:string;
    integer: string;
}

export class Validations {
    propertyValue: PropertyValue;
    validationPatterns: validationPatterns;
}

export class ValidationConfiguration {
    static validation: Validations;

}
