import { NgModule, Component, Compiler, ViewContainerRef, ViewChild, Input, ComponentRef, ComponentFactoryResolver, ChangeDetectorRef } from '@angular/core';
import {Component as TopologyTemplate} from "app/models";
import { SdcUiServices } from "onap-ui-angular";

// Helper component to add dynamic tabs
@Component({
  selector: 'panel-tab',
  template: `<div #content></div>`
})
export class PanelTabComponent {
  @ViewChild('content', { read: ViewContainerRef }) content;
  @Input() isActive:boolean;
  @Input() panelTabType;
  @Input() input;
  @Input() isViewOnly:boolean;
  @Input() component:TopologyTemplate;
  @Input() componentType;
  cmpRef: ComponentRef<any>;
  private isViewInitialized: boolean = false;

  constructor(private componentFactoryResolver: ComponentFactoryResolver,
    private cdRef: ChangeDetectorRef) { }

  updateComponent() {
    if (!this.isViewInitialized || !this.isActive) {
      return;
    }
    if (this.cmpRef) {
      this.cmpRef.destroy();
    }

    let factory = this.componentFactoryResolver.resolveComponentFactory(this.panelTabType);
    this.cmpRef = this.content.createComponent(factory);
    this.cmpRef.instance.input = this.input;
    this.cmpRef.instance.isViewOnly = this.isViewOnly;
    this.cmpRef.instance.component = this.component;
    this.cmpRef.instance.componentType = this.componentType;
    this.cdRef.detectChanges();
  }

  ngOnChanges() {
    this.updateComponent();
  }

  ngAfterViewInit() {
    this.isViewInitialized = true;
    this.updateComponent();
  }

  ngOnDestroy() {
    if (this.cmpRef) {
      this.cmpRef.destroy();
    }
  }
}