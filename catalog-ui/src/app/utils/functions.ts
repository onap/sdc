export class QueueUtils {

    private executionQueue:any;

    constructor(private $q:ng.IQService) {
        this.executionQueue = this.getDummyPromise();
    }


    private getDummyPromise = ():ng.IPromise<boolean> => {
        let deferred:ng.IDeferred<boolean> = this.$q.defer();
        deferred.resolve(true);
        return deferred.promise;
    };


    private addMethodToQueue = (runMe:Function):void => {
        this.executionQueue = this.executionQueue.then(runMe, runMe);
    };

    addNonBlockingUIAction = (update:Function, releaseUIcallBack:Function):void => {
        releaseUIcallBack();
        this.addMethodToQueue(update);
    };

    // The Method call is responsible for releasing the UI
    addBlockingUIAction = (blockingServerRequest:Function):void => {
        this.addMethodToQueue(blockingServerRequest);
    };

    addBlockingUIActionWithReleaseCallback = (blockingServerRequest:Function, releaseUIcallBack:Function):void=> {
        this.addMethodToQueue(blockingServerRequest);
        this.addMethodToQueue(releaseUIcallBack);
    };
}
