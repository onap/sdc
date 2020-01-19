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

import {Component, Input } from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import { setTimeout } from 'core-js/library/web/timers';
import { EventListenerService } from 'app/services';
import { GRAPH_EVENTS } from 'app/utils';
import { Point } from 'app/models';
import { ZoneInstanceType, ZoneInstance } from 'app/models/graph/zones/zone-instance';
 


@Component({
  selector: 'palette-animation',
  templateUrl: './palette-animation.component.html',
  styleUrls:['./palette-animation.component.less'],
})

export class PaletteAnimationComponent  {
  
  @Input() from : Point;
  @Input() to : Point;
  @Input() type: ZoneInstanceType;
  @Input() iconName : string;
  @Input() zoneInstance : ZoneInstance;

  public  animation;
  private visible:boolean = false;
  private transformStyle:string = "";


  constructor(private eventListenerService:EventListenerService) {}
  

  ngOnDestroy(){
    this.zoneInstance.hidden = false; //if animation component is destroyed before animation is complete
  }

  public runAnimation() {
    this.visible = true;
    let positionDiff:Point = new Point(this.to.x - this.from.x, this.to.y - this.from.y);
    setTimeout(()=>{
     this.transformStyle = 'translate('+ positionDiff.x + 'px,' + positionDiff.y +'px)';
    }, 0);
  };

  public animationComplete = (e) => {
    this.visible = false;
    this.zoneInstance.hidden = false;
  };


}
