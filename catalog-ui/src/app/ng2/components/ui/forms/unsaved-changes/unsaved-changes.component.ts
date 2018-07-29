import { Component, Input } from "@angular/core";

@Component({
    selector: 'unsaved-changes',
    templateUrl: './unsaved-changes.component.html',
    styleUrls: []
})
export class UnsavedChangesComponent {

    @Input() isValidChangedData:boolean;

    constructor(){
    }



}