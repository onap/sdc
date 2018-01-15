import {Component, OnInit, Input} from "@angular/core";
import {Designer} from "app/models";

@Component({
    selector: 'designer-frame',
    templateUrl: './designer-frame.component.html',
    styleUrls:['designer-frame.component.less']
})

export class DesignerFrameComponent implements OnInit {

    @Input() designer: Designer;
    designerUrl: string;

    constructor() {
    }

    ngOnInit(): void {

        this.designerUrl = this.designer.designerProtocol + "://" +
            this.designer.designerHost + ":" +
            this.designer.designerPort +
            this.designer.designerPath;
    }
}
