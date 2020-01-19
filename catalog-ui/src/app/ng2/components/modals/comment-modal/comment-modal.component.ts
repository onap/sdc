/**
 * Created by rc2122 on 5/31/2018.
 */
import { Component, Input } from "@angular/core";
import { ValidationConfiguration } from "app/models";
import { Subject } from "rxjs/Subject";

@Component({
    selector: 'comment-modal',
    templateUrl: './comment-modal.component.html',
    styleUrls: ['./comment-modal.less']
})

export class CommentModalComponent {

    @Input() message:string;
    onValidationChange: Subject<boolean> = new Subject();
    //@Input() showComment:boolean;
    private comment = {"text": ''};
    private commentValidationPattern = ValidationConfiguration.validation.validationPatterns.comment;

    private onValidityChange = (isValid: boolean):void => {
        this.onValidationChange.next(isValid);
    }
}