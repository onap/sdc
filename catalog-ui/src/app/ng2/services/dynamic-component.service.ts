import {
    Injectable, Type, ViewContainerRef, ApplicationRef, ComponentFactory, ComponentFactoryResolver, ComponentRef
} from '@angular/core';



@Injectable()
export class DynamicComponentService {

    constructor(private componentFactoryResolver: ComponentFactoryResolver, private applicationRef: ApplicationRef) { }

    //Creates a component dynamically (aka during runtime). If a view container is not specified, it will append the new component to the app root. 
    //To subscribe to an event from invoking component: componentRef.instance.clicked.subscribe((m) => console.log(m.name));
    public createDynamicComponent<T>(componentType: Type<T>, viewContainerRef?:ViewContainerRef): ComponentRef<T> {
        viewContainerRef = viewContainerRef || this.getRootViewContainerRef();
        viewContainerRef.clear();

        const factory: ComponentFactory<T> = this.componentFactoryResolver.resolveComponentFactory(componentType); //Ref: https://angular.io/guide/dynamic-component-loader
        return viewContainerRef.createComponent(factory);
    }

    
    private getRootViewContainerRef(): ViewContainerRef {
        return this.applicationRef.components[0].instance.viewContainerRef;
    }
};
