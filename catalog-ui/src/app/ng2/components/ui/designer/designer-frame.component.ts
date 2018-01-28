import {Component, OnInit, Input} from "@angular/core";
import { URLSearchParams } from '@angular/http';
import {Designer} from "app/models";

@Component({
    selector: 'designer-frame',
    templateUrl: './designer-frame.component.html',
    styleUrls:['designer-frame.component.less']
})

export class DesignerFrameComponent implements OnInit {

    @Input() designer: Designer;
    @Input() queryParams: Object;
    designerUrl: string;
    private urlSearchParams: URLSearchParams;

    constructor() {
        this.urlSearchParams = new URLSearchParams();
    }

    ngOnInit(): void {

        this.designerUrl = this.designer.designerProtocol + "://" +
            this.designer.designerHost + ":" +
            this.designer.designerPort +
            this.designer.designerPath;

        if (this.queryParams && !_.isEmpty(this.queryParams)) {
            _.forOwn(this.queryParams, (value, key) => {
                this.urlSearchParams.set(key, value);
            });

            this.designerUrl += '?';
            this.designerUrl += this.urlSearchParams.toString();
        }
    }
}
