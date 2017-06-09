'use strict';
import {SEVERITY} from "app/utils";

export interface IMessageModalModel {
    title:string;
    message:string;
    severity:SEVERITY;
}

export interface IMessageModalViewModelScope extends ng.IScope {
    footerButtons:Array<any>;
    messageModalModel:IMessageModalModel;
    modalInstanceError:ng.ui.bootstrap.IModalServiceInstance;
    ok():void;
}

export class MessageModalViewModel {

    constructor(private $baseScope:IMessageModalViewModelScope,
                private $baseModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private baseMessageModalModel:IMessageModalModel) {

        this.initScope(baseMessageModalModel);
    }

    private initScope = (messageModalViewModel:IMessageModalModel):void => {

        this.$baseScope.messageModalModel = messageModalViewModel;
        this.$baseScope.modalInstanceError = this.$baseModalInstance;

        this.$baseScope.ok = ():void => {
            this.$baseModalInstance.close();
        };

        this.$baseScope.footerButtons = [
            {
                'name': 'OK',
                'css': 'grey',
                'callback': this.$baseScope.ok
            }
        ];
    }
}
