export enum DirectivesEnum {
    SELECT = 'select',
    SELECTABLE = 'selectable',
    SUBSTITUTE = 'substitute',
    SUBSTITUTABLE = 'substitutable',
}

export namespace DirectiveValue {

  export function values() {
    return Object.keys(DirectivesEnum).filter(
        (type) => isNaN(<any>type) && type !== 'values'
    );
  }
}
