import {Component, Input, Output, EventEmitter, SimpleChanges} from "@angular/core";

@Component({
    selector: 'file-opener',
    templateUrl: './file-opener.component.html',
    styleUrls: ['./file-opener.component.less']
})
export class FileOpenerComponent {
    @Input() public testsId: string;
    @Input() public extensions: string;
    @Output() public onFileUpload: EventEmitter<any>;

    public extensionsWithDot: string;

    constructor() {
        this.onFileUpload = new EventEmitter<any>();
    }

    public ngOnChanges(changes:SimpleChanges) {
        if (changes.extensions) {
            this.extensionsWithDot = this.getExtensionsWithDot(changes.extensions.currentValue);
        }
    }

    public onFileSelect(event) {
        const importFile:any = event.target.files[0];
        const reader = new FileReader();
        reader.readAsBinaryString(importFile);
        reader.onload = () => {
            this.onFileUpload.emit({
                filename: importFile.name,
                filetype: importFile.type,
                filesize: importFile.size,
                base64: btoa(reader.result)
            });
        };
    }

    public getExtensionsWithDot(extensions:string):string {
        extensions = extensions || this.extensions || '';
        return extensions.split(',')
            .map(ext => '.' + ext.toString())
            .join(',');
    }
}
