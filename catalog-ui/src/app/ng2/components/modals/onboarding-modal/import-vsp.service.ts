import { Injectable, Inject } from "@angular/core";
import { OnboardingModalComponent } from "./onboarding-modal.component";
import { SdcUiServices, SdcUiCommon } from "onap-ui-angular";
import { Observable, Subject } from "rxjs";
import { CHANGE_COMPONENT_CSAR_VERSION_FLAG } from "../../../../utils/constants";
import { CacheService } from "../../../services/cache.service";


@Injectable()
export class ImportVSPService {

    constructor(private modalService: SdcUiServices.ModalService,
                private cacheService:CacheService,
                @Inject("$state") private $state:ng.ui.IStateService){

    }
    
    openOnboardingModal(csarUUID?: string, csarVersion?: string): Observable<any> {
        var subject = new Subject<any>();
        const onboardingModalConfig = {
            size: SdcUiCommon.ModalSize.xlarge,
            title: 'Import VSP',      
            type: SdcUiCommon.ModalType.custom,
            testId: 'sampleTestIdModal1',
        } as SdcUiCommon.IModalConfig;
        const onboardingModalInstance = this.modalService.openCustomModal(onboardingModalConfig, OnboardingModalComponent, {currentCsarUUID: csarUUID, currentCsarVersion: csarVersion});
        onboardingModalInstance.innerModalContent.instance.closeModalEvent.subscribe(
            (result: any) => {
                subject.next(result);
                onboardingModalInstance.closeModal(); 
            }, (err) =>{}
        )
        return subject.asObservable();
    }
}
   
