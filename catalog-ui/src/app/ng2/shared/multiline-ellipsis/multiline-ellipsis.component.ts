import {Component, OnChanges, AfterContentInit, ViewChild, ElementRef, Input, Output, SimpleChanges, EventEmitter} from "@angular/core";
import {WindowRef} from "../../services/window.service";

@Component({
	selector: 'multiline-ellipsis',
	templateUrl: 'multiline-ellipsis.component.html',
	styleUrls: ['multiline-ellipsis.component.less']
})
export class MultilineEllipsisComponent implements OnChanges, AfterContentInit {

	@Input() public lines: number;
	@Input() public lineHeight: string;
	@Input() public className: string;
	@Output() public hasEllipsisChanged: EventEmitter<boolean>;

	@ViewChild('multilineEllipsisContainer') public elmContainer: ElementRef;
	@ViewChild('multilineEllipsisContent') public elmContent: ElementRef;

	public stylesContainer: {[key: string]: string};
	public stylesContent: {[key: string]: string};
	public stylesDots: {[key: string]: string};

	private hasEllipsis: boolean;

	public constructor(private windowRef: WindowRef) {
		this.hasEllipsisChanged = new EventEmitter<boolean>();
	}

	public ngOnChanges(changes: SimpleChanges) {
		this.prepareStyles()
	}

	public ngAfterContentInit() {
		const hasEllipsis = (this.elmContainer.nativeElement.offsetHeight < this.elmContent.nativeElement.offsetHeight);
		if (hasEllipsis !== this.hasEllipsis) {
			this.hasEllipsis = hasEllipsis;
			this.hasEllipsisChanged.emit(this.hasEllipsis);
		}
	}

	private prepareStyles() {
		const lineHeight = this.lineHeight || this.getLineHeight();
		this.stylesContainer = {
            'max-height': `calc(${this.lines} * ${lineHeight})`
        };
		this.stylesContent = {
        	'max-height': `calc(${this.lines + 1} * ${lineHeight})`
		};
		this.stylesDots = {
        	'top': `calc(${2 * this.lines} * ${lineHeight} - 100%)`
		};
	}

	private getLineHeight() {
		return this.windowRef.nativeWindow.getComputedStyle(this.elmContainer.nativeElement)['line-height'];
	}

}
