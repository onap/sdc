import { Component, Input, Output, OnInit } from "@angular/core";
import { EventEmitter } from "@angular/core";
import { DisplayModule } from "../../../../../../models/modules/base-module";
import { ValidationConfiguration } from "../../../../../../models/validation-config";

@Component({
    selector: 'edit-module-name',
    templateUrl: './edit-module-name.component.html',
    styleUrls: ['edit-module-name.component.less']
})
export class EditModuleName implements OnInit{
    @Input() selectModule:DisplayModule;
    @Output() clickButtonEvent: EventEmitter<String> = new EventEmitter();
    public pattern = ValidationConfiguration.validation.validationPatterns.stringOrEmpty;
    public originalName: string;
    constructor(){}
    public ngOnInit(): void {
         this.originalName = this.selectModule.heatName;
    }

    public clickButton(saveOrCancel: boolean) : void {
        this.clickButtonEvent.emit(saveOrCancel ? this.selectModule.heatName : null);
    }
}