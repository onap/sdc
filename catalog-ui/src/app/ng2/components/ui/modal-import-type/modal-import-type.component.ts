import {Component, Inject, OnInit} from '@angular/core';

@Component({
  selector: 'app-modal-import-type',
  templateUrl: './modal-import-type.component.html',
  styleUrls: ['./modal-import-type.component.less']
})
export class ModalImportTypeComponent implements OnInit {

  file:File = null;

  constructor() {}

  ngOnInit() {
  }

  onFileChange(event: any) {
      this.file = event.target.files[0];
  }

  public onImportDataType(file: any): void {
    if (file && file.filename) {
        console.log("file: " + file.filename);
    }
  }

}
