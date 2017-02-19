package org.openecomp.test;

//import java.util.concurrent.Callable;
//
//import org.openecomp.sdc.api.IDistributionClient;
//import org.openecomp.sdc.api.results.IDistributionClientResult;
//import org.openecomp.sdc.impl.DistributionClientFactory;
//import org.openecomp.sdc.be.model.Service;
//import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;

public class CallableTask  {

//	NotificationCallback notifCallback = null;
//	//private ServiceUtils serviceUtils;
//	private Service service;
//	
//	//public CallableTask(ServiceUtils serviceUtils, Service service) {
//	public CallableTask(Service service) {
//		//this.serviceUtils = serviceUtils;
//		this.service = service;
//	}
//	
//	public NotificationCallback getNotifCallback() {
//		return notifCallback;
//	}
//	
//	@Override
//	public Boolean call() throws Exception {
//		
//		IDistributionClient client = DistributionClientFactory.createDistributionClient();
//		
//		notifCallback = new NotificationCallback(client);
//		IDistributionClientResult result =  client.init(new SimpleConfiguration(),  notifCallback);
//		
//		System.out.println("result.getDistributionMessageResult: " + result.getDistributionMessageResult());
//			
//		System.out.println("Starting client...");
//		IDistributionClientResult start = client.start();
//		System.out.println(start.getDistributionMessageResult());
//		
////		RestResponse changeStateToDISTRIBUTED = serviceUtils.changeStateToDISTRIBUTED(service, UserUtils.getGovernorDetails1());
//		ServiceReqDetails serviceDetails = new ServiceReqDetails();
//		serviceDetails.setUniqueId(service.getUniqueId());
//		//RestResponse changeStateToDISTRIBUTED = serviceUtils.changeDistributionStatus(serviceDetails, "1.0", UserUtils.getGovernorDetails1(), "change", DistributionStatusEnum.DISTRIBUTED);
//		//assertTrue("response code is not 200, returned :" + changeStateToDISTRIBUTED.getErrorCode(), changeStateToDISTRIBUTED.getErrorCode() == 200);
//		
//		
////		while (simpleCallback.getSimpleCallbackResults().size()<3){
//			System.err.println("Sleeping...");
//			Thread.sleep(5000);
//			System.err.println("Finished Sleeping...");
////		}
//		
//		return true;
//	}

}
