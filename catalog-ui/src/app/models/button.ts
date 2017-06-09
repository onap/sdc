/**
 * Created by rc2122 on 5/10/2017.
 */
export class ButtonModel {
    text: string;
    cssClass: string;
    callback: Function;
    getDisabled:Function;
    constructor(text?:string, cssClass?:string, callback?:Function, getDisabled?:Function){
        this.text = text;
        this.cssClass = cssClass;
        this.callback = callback;
        this.getDisabled = getDisabled;

    }
}

export class ButtonsModelMap {
    [buttonName: string]: ButtonModel;
}
