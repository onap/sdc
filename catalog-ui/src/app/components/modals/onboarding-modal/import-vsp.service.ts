import { Injectable } from "@angular/core";
import {ImportVSPdata, OnboardingModalComponent} from "./onboarding-modal.component";
import { SdcUiServices, SdcUiCommon } from "onap-ui-angular";
import { Observable, Subject } from "rxjs";
import { CacheService } from "../../../services/cache.service";


@Injectable()
export class ImportVSPService {

    constructor(private modalService: SdcUiServices.ModalService,
                private cacheService:CacheService){

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
            (result: ImportVSPdata) => {
                subject.next(result);
                // Explicit undefined only to satisfy onap-ui-angular's over-strict typing
                // (closeModal: (btnName: string) => void); btnName is just the onClose.emit
                // payload, and every other call site in catalog-ui also passes nothing.
                onboardingModalInstance.closeModal(undefined);
            }, (err) =>{}
        )
        return subject.asObservable();
    }
}
   
