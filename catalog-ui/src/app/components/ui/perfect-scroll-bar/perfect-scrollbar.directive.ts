import {Directive, Input, ElementRef} from '@angular/core';
import * as PerfectScrollbar from 'perfect-scrollbar';

interface IPerfectScrollbarOptions {
    wheelSpeed?: number;
    wheelPropagation?: boolean;
    minScrollbarLength?: number;
    useBothWheelAxes?: boolean;
    useKeyboard?: boolean;
    suppressScrollX?: boolean;
    suppressScrollY?: boolean;
    scrollXMarginOffset?: number;
    scrollYMarginOffset?: number;
    includePadding?: boolean;
}

@Directive({
    selector: '[perfectScrollbar]'
})
export class PerfectScrollbarDirective {
    @Input() public perfectScrollbarOptions: IPerfectScrollbarOptions;

    private psOptions: IPerfectScrollbarOptions;
    private updatingPS: boolean;

    constructor(public elemRef:ElementRef) {
        console.log('PSbar: Constructor');
        this.psOptions = Object.assign({}, this.perfectScrollbarOptions);
        this.updatingPS = false;
    }

    public ngOnInit() {
        console.log('PSbar: Initializing');
        PerfectScrollbar.initialize(this.elemRef.nativeElement, this.psOptions);
        // perfect-scrollbar 0.6 makes each thumb a tab stop, unnamed and present even when nothing
        // scrolls. Tabbing through the content already scrolls it.
        const thumbs = this.elemRef.nativeElement.querySelectorAll('.ps-scrollbar-x, .ps-scrollbar-y');
        Array.prototype.forEach.call(thumbs, (thumb: HTMLElement) => thumb.setAttribute('tabindex', '-1'));
    }

    public ngAfterContentChecked() {
        // update perfect-scrollbar after content is checked (updated) - bounced
        if (!this.updatingPS) {
            this.updatingPS = true;
            setTimeout(() => {
                this.updatingPS = false;
                PerfectScrollbar.update(this.elemRef.nativeElement);
            }, 100);
        }
    }

    public ngOnDestroy() {
        PerfectScrollbar.destroy(this.elemRef.nativeElement);
    }
}
