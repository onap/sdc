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
import { Component, OnInit, ViewContainerRef} from '@angular/core';
import { AuthenticationService } from './services/authentication.service';
import { RouteMetadataService } from './services/route-metadata.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  constructor(auth: AuthenticationService, public viewContainerRef: ViewContainerRef,
              private routeMetadataService: RouteMetadataService){

  }

  // Started here rather than from an APP_INITIALIZER because it only has to beat the FIRST
  // NavigationEnd, and the initial navigation is fired by hand from main.ts after
  // upgrade.bootstrap() — well after this component's ngOnInit, which runs inside bootstrapModule().
  ngOnInit(): void {
    this.routeMetadataService.start();
  }

}
