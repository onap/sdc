/**
 * Created by ob0695 on 10.04.2018.
 */

export interface IUiBaseObject {
    name:string;
    uniqueId:string;
    type:any;
}
export class UiBaseObject implements IUiBaseObject{
    name:string;
    uniqueId:string;
    type:any;

    constructor(uniqueId: string,  type?: any, name?:string) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.type = type;
    }
}