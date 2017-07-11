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

    getImageBase64(imageBaseUri:string, imageLayerUri:string):ng.IPromise<string> {
        let deferred = this.$q.defer();
        let imageBase = new Image();
        let imageLayer = new Image();
        let imagesLoaded = 0;
        let onImageLoaded = () => {
            imagesLoaded++;

            if (imagesLoaded < 2) {
                return;
            }
            this._canvas.setAttribute('width', imageBase.width.toString());
            this._canvas.setAttribute('height', imageBase.height.toString());

            let canvasCtx = this._canvas.getContext('2d');
            canvasCtx.clearRect(0, 0, this._canvas.width, this._canvas.height);

            canvasCtx.drawImage(imageBase, 0, 0, imageBase.width, imageBase.height);
            canvasCtx.drawImage(imageLayer, imageBase.width - imageLayer.width, 0, imageLayer.width, imageLayer.height);

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
