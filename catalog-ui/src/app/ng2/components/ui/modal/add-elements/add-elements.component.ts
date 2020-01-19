/**
 * Created by ob0695 on 11.04.2018.
 */
import { Component, Input } from "@angular/core";
import { UiBaseObject } from "../../../../../models/ui-models/ui-base-object";
import { IDropDownOption } from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";

@Component({
    selector: 'add-elements',
    templateUrl: './add-elements.component.html',
    styleUrls: ['./add-elements.component.less']
})

export class AddElementsComponent {

    @Input() elementsToAdd:Array<UiBaseObject>;
    @Input() elementName: string;

    private existingElements:Array<UiBaseObject>;
    private dropdownOptions:Array<IDropDownOption>;
    private selectedElement:IDropDownOption;

    ngOnInit() {
        this.existingElements = [];
        this.convertElementToDropdownDisplay();

    }

    private convertElementToDropdownDisplay = () => {
        this.dropdownOptions = [];
        _.forEach(this.elementsToAdd, (elementToAdd:UiBaseObject) =>{
           this.dropdownOptions.push({label:elementToAdd.name, value: elementToAdd.uniqueId })
        });
    }

    onElementSelected(selectedElement:IDropDownOption):void {
        this.selectedElement = selectedElement
    }

    addElement():void {

        if(this.selectedElement){
            this.dropdownOptions = _.reject(this.dropdownOptions, (option: IDropDownOption) => { // remove from dropDown
                return option.value === this.selectedElement.value;
            });

            let selected = _.find(this.elementsToAdd, (element:UiBaseObject) => {
                return this.selectedElement.value === element.uniqueId;
            });

            this.elementsToAdd =_.without(this.elementsToAdd, selected); // remove from optional elements to add
            this.existingElements.push(selected); // add to existing element list
            this.selectedElement = undefined;
        } else {
            console.log("no element selected"); //TODO:show error?
        }
    }

    removeElement(element:UiBaseObject):void {

        this.existingElements =_.without(this.existingElements, element); // remove from optional elements to add
        this.dropdownOptions.push({label:element.name, value: element.uniqueId });
        this.elementsToAdd.push(element);
    }
}
