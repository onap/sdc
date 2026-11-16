import {
    Injectable, Type, ViewContainerRef, ApplicationRef, ComponentFactory, ComponentFactoryResolver, ComponentRef
} from '@angular/core';



@Injectable({providedIn: 'root'})
export class DynamicComponentService {

    constructor(private componentFactoryResolver: ComponentFactoryResolver, private applicationRef: ApplicationRef) { }

    // Creates a component dynamically (aka during runtime). If a view container is not specified, it will append the new component to the app root.
    // To subscribe to an event from invoking component: componentRef.instance.clicked.subscribe((m) => console.log(m.name));
    /**
     * `resolver` must be passed for a component declared in a lazy-loaded module. This service is
     * root-provided, so its own resolver is the root one, and under View Engine the root resolver
     * cannot see a lazy module's entryComponents ("No component factory found"). A factory from the
     * lazy module's resolver is also bound to that module, so the new component gets its injector.
     */
    public createDynamicComponent<T>(componentType: Type<T>, viewContainerRef?: ViewContainerRef,
                                     resolver: ComponentFactoryResolver = this.componentFactoryResolver): ComponentRef<T> {
        viewContainerRef = viewContainerRef || this.getRootViewContainerRef();
        viewContainerRef.clear();

        const factory: ComponentFactory<T> = resolver.resolveComponentFactory(componentType); // Ref: https://angular.io/guide/dynamic-component-loader
        return viewContainerRef.createComponent(factory);
    }

    
    private getRootViewContainerRef(): ViewContainerRef {
        return this.applicationRef.components[0].instance.viewContainerRef;
    }
};
