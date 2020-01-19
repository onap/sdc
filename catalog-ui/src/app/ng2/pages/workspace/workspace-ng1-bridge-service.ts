/**
 * Created by ob0695 on 6/24/2018.
 */
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
import {Store} from "@ngxs/store";
import {Injectable} from "@angular/core";
import {UpdateIsViewOnly} from "../../store/actions/workspace.action";

@Injectable()
export class WorkspaceNg1BridgeService {

    constructor(private store: Store) {
    };

    public updateIsViewOnly = (isViewOnly: boolean):void => {
        this.store.dispatch(new UpdateIsViewOnly(isViewOnly));
    }

}
