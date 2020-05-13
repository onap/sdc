export enum DirectivesEnum {
    SELECT = 'select',
    SUBSTITUTE = 'substitute',
}

export namespace DirectiveValue {

  export function values() {
    return Object.keys(DirectivesEnum).filter(
        (type) => isNaN(<any>type) && type !== 'values'
    );
  }
}
