import { ButtonModel } from 'app/models';

export class ModalModel {
    size: string; 'xl|l|md|sm|xsm'
    title: string;
    content: any;
    buttons: Array<ButtonModel>;
    type: string; 'standard|error|alert'
    isMovable: boolean;

    constructor(size?: string, title?: string, content?: any, buttons?: Array<ButtonModel>, type?: string, isMovable?: boolean) {
        this.size = size;
        this.title = title;
        this.content = content;
        this.buttons = buttons;
        this.type = type || 'standard';
        this.isMovable = !!isMovable;
    }
}

