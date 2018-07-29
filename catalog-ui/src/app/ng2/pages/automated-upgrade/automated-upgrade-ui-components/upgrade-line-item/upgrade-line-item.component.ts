import {Component, Input} from "@angular/core";

@Component({
    selector: 'upgrade-line-item',
    templateUrl: './upgrade-line-item.component.html',
    styleUrls: ['./upgrade-line-item.component.less']
})

export class UpgradeLineItemComponent {

    @Input() arrowName:string;
    @Input() icon:string;
    @Input() prefix:string;
    @Input() text:string;
    
    constructor() {

    }
}
