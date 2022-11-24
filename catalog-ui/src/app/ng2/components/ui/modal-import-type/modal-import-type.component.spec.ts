import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModalImportTypeComponent } from './modal-import-type.component';

describe('ModalImportTypeComponent', () => {
  let component: ModalImportTypeComponent;
  let fixture: ComponentFixture<ModalImportTypeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModalImportTypeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModalImportTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
