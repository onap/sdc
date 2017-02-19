/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
/// <reference path="../../references"/>
module Sdc.Directives {

    class ClickedOutsideModel{

      private clickedOutsideContainerSelector: string;
      private onClickedOutsideGetter: Function;
      private clickedOutsideEnableGetter: Function;

      constructor(clickedOutsideData: any) {
        this.clickedOutsideContainerSelector = clickedOutsideData.clickedOutsideContainerSelector;
        this.onClickedOutsideGetter = clickedOutsideData.onClickedOutsideGetter;
        this.clickedOutsideEnableGetter = clickedOutsideData.clickedOutsideEnableGetter;
      }

      public getClickedOutsideContainerSelector = (): string => {
        return this.clickedOutsideContainerSelector;
      }

      public getOnClickedOutsideGetter = (): Function => {
        return this.onClickedOutsideGetter;
      }

      public getClickedOutsideEnableGetter = (): Function => {
        return this.clickedOutsideEnableGetter;
      }
    }

    export interface IClickedOutsideDirectiveScope extends ng.IScope{}

    export class ClickedOutsideDirective implements ng.IDirective {

      constructor(private $document: JQuery, private $parse: ng.IParseService) {}

        restrict = 'A';

        link = (scope:IClickedOutsideDirectiveScope, element: JQuery, attrs) => {

          let container: HTMLElement;
          let attrsAfterEval = scope.$eval(attrs.clickedOutside);
          attrsAfterEval.onClickedOutsideGetter = this.$parse(attrsAfterEval.onClickedOutside);
          attrsAfterEval.clickedOutsideEnableGetter = this.$parse(attrsAfterEval.clickedOutsideEnable);

          let clickedOutsideModel: ClickedOutsideModel = new ClickedOutsideModel(attrsAfterEval);


          let getContainer: Function = ():HTMLElement => {
            if(!container){
              let clickedOutsideContainerSelector: string = clickedOutsideModel.getClickedOutsideContainerSelector();
              if(!angular.isUndefined(clickedOutsideContainerSelector) && clickedOutsideContainerSelector !== ''){
                container = element.parents(clickedOutsideContainerSelector+':first')[0];
                if(!container){
                  container = element[0];
                }
              }else{
                container = element[0];
              }
            }
            return container;
          };


          let onClickedOutside = (event: JQueryEventObject) => {
              let containerDomElement: HTMLElement = getContainer();
              let targetDomElementJq: JQuery = angular.element(event.target);
              if(targetDomElementJq.hasClass('tooltip') || targetDomElementJq.parents('.tooltip:first').length){
                  return;
              }
              let targetDomElement: HTMLElement = targetDomElementJq[0];
              if (!containerDomElement.contains(targetDomElement)){
                  scope.$apply(() => {
                    let onClickedOutsideGetter:Function = clickedOutsideModel.getOnClickedOutsideGetter();
                    onClickedOutsideGetter(scope);
                  });
              }
          };

          let attachDomEvents: Function = () => {
            this.$document.on('mousedown', onClickedOutside);
          };

          let detachDomEvents: Function = () => {
            this.$document.off('mousedown', onClickedOutside);
          };

          //
          scope.$on('$destroy', () => {
            detachDomEvents();
          });


          scope.$watch(() => {
            let clickedOutsideEnableGetter: Function = clickedOutsideModel.getClickedOutsideEnableGetter();
            return clickedOutsideEnableGetter(scope);
          }, (newValue: boolean) => {
                if(newValue){
                    attachDomEvents();
                    return;
                }
                detachDomEvents();
          });


        }

        public static factory = ($document: JQuery, $parse: ng.IParseService) => {
          return new ClickedOutsideDirective($document, $parse);
        }
    }

    ClickedOutsideDirective.factory.$inject = ['$document', '$parse'];
}
