import { ButtonModel } from 'app/models';

export class ModalModel {
    size: string; 'xl|l|md|sm|xsm'
    title: string;
    content: any;
    buttons: Array<ButtonModel>;
    type: string; 'standard|error|alert'

    constructor(size?: string, title?: string, content?: any, buttons?: Array<ButtonModel>, type?:string) {
        this.size = size;
        this.title = title;
        this.content = content;
        this.buttons = buttons;
        this.type = type || 'standard';
    }
}

