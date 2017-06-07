'use strict';
import {IAppMenu, Component, IAppConfigurtaion} from "app/models";
import {UrlToBase64Service} from "app/services";

export interface IPrintGraphScreenScope extends ng.IScope {
    entity:Component;
}


export class PrintGraphScreenDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService,
                private  sdcMenu:IAppMenu,
                private sdcConfig:IAppConfigurtaion,
                private urlToBase64Service:UrlToBase64Service) {
    }

    scope = {
        entity: '='
    };
    restrict = 'A';
    link = (scope:IPrintGraphScreenScope, element:any) => {


        element.bind('click', function () {
            printScreen();
        });


        let printScreen = ():void => {
            //
            //     let pdf :any = new jsPDF('landscape', 'mm', 'a4');
            //     pdf.setProperties({
            //         title: scope.entity.name,
            //         subject: 'Design Snapshot for ' + scope.entity.name,
            //         author: scope.entity.creatorFullName,
            //         keywords: scope.entity.tags.join(', '),
            //         creator: scope.entity.creatorFullName
            //     });
            //
            //     // A4 measures is 210 × 297 millimeters
            //     let pdfWidth :number = 297,
            //         pdfHeight :number = 210,
            //         leftColumnWidth :number = 80;
            //
            //     //left bar background
            //     pdf.setDrawColor(0);
            //     pdf.setFillColor(248, 249, 251);
            //     pdf.rect(0, 0, leftColumnWidth, pdfHeight, 'F');
            //
            //     //entity name
            //     pdf.setFontSize(12);
            //     pdf.setTextColor(38, 61, 77);
            //     let splitTitle :any = pdf.splitTextToSize(scope.entity.name, 50);
            //     pdf.text(22, 15 - (splitTitle.length - 1) * 2, splitTitle);
            //
            //     //line
            //     pdf.setLineWidth(0.2);
            //     pdf.setDrawColor(208, 209, 213);
            //     pdf.line(0, 28, leftColumnWidth, 28);
            //
            //
            //     pdf.setFontSize(10);
            //     let properties :any = getPdfProperties();
            //
            //     let topOffset :number = 39, lines;
            //     properties.forEach( (item:any) => {
            //         if (!item.value) {
            //             return;
            //         }
            //         if (item.title === 'Description:') {
            //             topOffset += 5;
            //         }
            //
            //         pdf.setTextColor(38, 61, 77);
            //         pdf.text(5, topOffset, item.title);
            //         pdf.setTextColor(102, 102, 102);
            //         lines = pdf.splitTextToSize(item.value, 49);
            //         pdf.text(5 + item.offset, topOffset, lines[0]);
            //         if (lines.length > 1) {
            //             lines = pdf.splitTextToSize(item.value.substring(lines[0].length + 1), 65);
            //             if (lines.length > 8) {
            //                 lines = lines.slice(0, 7);
            //                 lines[lines.length - 1] += '...';
            //             }
            //             pdf.text(5, topOffset + 4, lines);
            //             topOffset += 4 * (lines.length);
            //         }
            //
            //         topOffset += 6;
            //     });
            //
            //
            //     //another background in case the text was too long
            //     let declarationLineOffset :number = 176;
            //     pdf.setDrawColor(0);
            //     pdf.setFillColor(248, 249, 251);
            //     pdf.rect(0, declarationLineOffset, leftColumnWidth, pdfHeight - declarationLineOffset, 'F');
            //     //line
            //     pdf.setLineWidth(0.2);
            //     pdf.setDrawColor(208, 209, 213);
            //     pdf.line(0, declarationLineOffset, leftColumnWidth, declarationLineOffset);
            //
            //     //declaration
            //     pdf.setFontSize(10.5);
            //     pdf.setTextColor(38, 61, 77);
            //     pdf.text(5, 185, 'Declaration');
            //     pdf.setFontSize(9);
            //     pdf.setTextColor(102, 102, 102);
            //     pdf.setFontType('bold');
            //     pdf.text(5, 190, this.$filter('translate')('PDF_FILE_DECLARATION_BOLD'));
            //     pdf.setFontType('normal');
            //     pdf.text(5, 194, pdf.splitTextToSize(this.$filter('translate')('PDF_FILE_DECLARATION'), 65));
            //
            //     //entity icon
            //     let self = this;
            //     let addEntityIcon:Function = () => {
            //         let iconPath:string = self.sdcConfig.imagesPath + '/styles/images/';
            //         if (scope.entity.isService()) {
            //             iconPath += 'service-icons/' + scope.entity.icon + '.png';
            //         } else {
            //             iconPath += 'resource-icons/' + scope.entity.icon + '.png';
            //         }
            //         self.urlToBase64Service.downloadUrl(iconPath, (base64string:string):void => {
            //             if (base64string) {
            //                 pdf.addImage(base64string, 'JPEG', 5, 7, 15, 15);
            //             }
            //             pdf.save(scope.entity.name + '.pdf');
            //         });
            //     };
            //
            //     //actual snapshop of canvas
            //     let diagramDiv :any = document.getElementById('myDiagram');
            //     let diagram :go.Diagram = go.Diagram.fromDiv(diagramDiv), canvasImg = new Image();
            //     diagram.startTransaction('print screen');
            //     let canvasImgBase64:any = diagram.makeImageData({
            //         //scale: 1,
            //         size: new go.Size(pdfHeight * 5, NaN),
            //         background: 'white',
            //         type: 'image/jpeg'
            //     });
            //     diagramDiv.firstElementChild.toDataURL();
            //     diagram.commitTransaction('print screen');
            //
            //     canvasImg.onload = () => {
            //         if (canvasImg.height > 0) {
            //             let canvasImgRatio:number = Math.min((pdfWidth - leftColumnWidth - 15) / canvasImg.width, pdfHeight / canvasImg.height);
            //             let canvasImgWidth:number = canvasImg.width * canvasImgRatio,
            //                 canvasImgHeight:number = canvasImg.height * canvasImgRatio;
            //             let canvasImgOffset:number = (pdfHeight - canvasImgHeight) / 2;
            //             pdf.addImage(canvasImg, 'JPEG', leftColumnWidth, canvasImgOffset, canvasImgWidth, canvasImgHeight);
            //
            //             addEntityIcon();
            //         }
            //     };
            //
            //     if(canvasImg.src === 'data:,') { //empty canvas
            //         addEntityIcon();
            //     } else {
            //         canvasImg.src = canvasImgBase64;
            //     }
        };


        let getPdfProperties = ():Array<any> => {
            // return [
            //     {title: this.$filter('translate')('GENERAL_LABEL_TYPE'), value: scope.entity.getComponentSubType(), offset: 10},
            //     {title: this.$filter('translate')('GENERAL_LABEL_VERSION'), value: scope.entity.version, offset: 15},
            //     {title: this.$filter('translate')('GENERAL_LABEL_CATEGORY'), value: scope.entity.categories.length ? scope.entity.categories[0].name : '', offset: 16},
            //     {title: this.$filter('translate')('GENERAL_LABEL_CREATION_DATE'), value: this.$filter('date')(scope.entity.creationDate, 'MM/dd/yyyy'), offset: 24},
            //     {title: this.$filter('translate')('GENERAL_LABEL_AUTHOR'), value: scope.entity.creatorFullName, offset: 13},
            //     {title: this.$filter('translate')('GENERAL_LABEL_CONTACT_ID'), value: scope.entity.contactId, offset: 41},
            //     {title: this.$filter('translate')('GENERAL_LABEL_STATUS'), value: (<any>this.sdcMenu).LifeCycleStatuses[scope.entity.lifecycleState].text, offset: 13},
            //     {title: this.$filter('translate')('GENERAL_LABEL_PROJECT_CODE'), value: scope.entity.projectCode, offset: 15},
            //     {title: this.$filter('translate')('GENERAL_LABEL_DESCRIPTION'), value: scope.entity.description, offset: 20},
            //     {title: this.$filter('translate')('GENERAL_LABEL_TAGS'), value: scope.entity.tags.join(', '), offset: 10}
            // ];
            return null;
        };


    };

    public static factory = ($filter:ng.IFilterService, sdcMenu:IAppMenu, sdcConfig:IAppConfigurtaion, urlToBase64Service:UrlToBase64Service)=> {
        return new PrintGraphScreenDirective($filter, sdcMenu, sdcConfig, urlToBase64Service);
    };

}

PrintGraphScreenDirective.factory.$inject = ['$filter', 'sdcMenu', 'sdcConfig', 'Sdc.Services.UrlToBase64Service'];
