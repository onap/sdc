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

'use strict';
export class ImageCreatorService {
    static '$inject' = ['$q'];
    private _canvas:HTMLCanvasElement;

    constructor(private $q:ng.IQService) {
        this._canvas = <HTMLCanvasElement>$('<canvas>')[0];
        this._canvas.setAttribute('style', 'display:none');

        let body = document.getElementsByTagName('body')[0];
        body.appendChild(this._canvas);
    }

    getImageBase64(imageBaseUri:string, imageLayerUri:string, nodeWidth:number, canvasWidth:number, handleSize:number):ng.IPromise<string> {
        let deferred = this.$q.defer();
        let imageBase = new Image();
        let imageLayer = new Image();
        let imagesLoaded = 0;
        let onImageLoaded = () => {
            imagesLoaded++;

            if (imagesLoaded < 2) {
                return;
            }
            this._canvas.setAttribute('width', canvasWidth.toString());
            this._canvas.setAttribute('height', canvasWidth.toString());

            let canvasCtx = this._canvas.getContext('2d');
            canvasCtx.clearRect(0, 0, this._canvas.width, this._canvas.height);

            //Note: params below are: image, x to start drawing at, y to start drawing at, num of x pixels to draw, num of y pixels to draw
            canvasCtx.drawImage(imageBase, 0, canvasWidth - nodeWidth, nodeWidth, nodeWidth); //Draw the node: When nodeWidth == canvasWidth, we'll start at point 0,0. Otherwise, x starts at 0 (but will end before end of canvas) and y starts low enough that node img ends at bottom of canvas.
            canvasCtx.drawImage(imageLayer, canvasWidth - handleSize, 0, handleSize, handleSize); //Draw the icon: icon should be drawn in top right corner

            let base64Image = this._canvas.toDataURL();
            deferred.resolve(base64Image);
        };

        imageBase.onload = onImageLoaded;
        imageLayer.onload = onImageLoaded;
        imageBase.src = imageBaseUri;
        imageLayer.src = imageLayerUri;

        return deferred.promise;
    }
}
