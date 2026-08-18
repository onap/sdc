/**
 * Created by rc2122 on 9/27/2017.
 */
import {Input} from "@angular/core";

// Deliberately NOT decorated: this is only ever extended, never instantiated or declared in an
// NgModule. An empty @Component({}) made AOT fail with "Cannot determine the module for class"
// (and it cannot be declared, since it has no template). The @Input below is still inherited by
// decorated subclasses, because AOT's StaticReflector walks the prototype chain for propMetadata
// just as the JIT reflector does — verified on the generated factory for the one template that
// binds it, connection-properties-view.component.html.
export class WizardHeaderBaseComponent {

    @Input() currentStepIndex:number;
}
