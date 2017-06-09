export class HierarchyDisplayOptions {
    idProperty: string;
    valueProperty: string;
    childrenProperty: string;
    searchText:string;
    constructor(idProperty:string, valueProperty:string, childrenProperty?:string, searchText?:string) {
        this.idProperty = idProperty;
        this.valueProperty = valueProperty;
        this.childrenProperty = childrenProperty;
        this.searchText = searchText;
    }
}
