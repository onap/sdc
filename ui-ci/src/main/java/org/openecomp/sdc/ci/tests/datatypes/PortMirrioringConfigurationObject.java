package org.openecomp.sdc.ci.tests.datatypes;

import org.openecomp.sdc.be.model.Service;

public class PortMirrioringConfigurationObject {

    private ServiceReqDetails serviceReqDetails;
    private String vmmeSourceName;
    private String vprobeSourceName;
    private CanvasManager canvasManager;
    private CanvasElement serviceElementVmmeSourceName;
    private CanvasElement serviceElementVprobeCollector;
    private  CanvasElement portMirroringConfigurationElement;
    private Service service;
    private Service serviceContainerVmme_Source;
    private Service serviceContainerVprobe_Collector;

    public PortMirrioringConfigurationObject(ServiceReqDetails serviceReqDetails, String vmmeSourceName, String vprobeSourceName,
                                             CanvasManager canvasManager, CanvasElement serviceElementVmmeSourceName,
                                             CanvasElement serviceElementVprobeCollector, Service service, CanvasElement portMirroringConfigurationElement,
                                             Service serviceContainerVmme_Source, Service serviceContainerVprobe_Collector )
    {
        this.serviceReqDetails = serviceReqDetails;
        this.vmmeSourceName = vmmeSourceName;
        this.vprobeSourceName = vprobeSourceName;
        this.canvasManager = canvasManager;
        this.serviceElementVmmeSourceName = serviceElementVmmeSourceName;
        this.serviceElementVprobeCollector = serviceElementVprobeCollector;
        this.service = service;
        this.portMirroringConfigurationElement = portMirroringConfigurationElement;
        this.serviceContainerVmme_Source = serviceContainerVmme_Source;
        this.serviceContainerVprobe_Collector = serviceContainerVprobe_Collector;
    }

    public Service getServiceContainerVmme_Source() {
        return serviceContainerVmme_Source;
    }

    public Service getServiceContainerVprobe_Collector() {
        return serviceContainerVprobe_Collector;
    }

    public CanvasElement getPortMirroringConfigurationElement() {
        return portMirroringConfigurationElement;
    }

    public ServiceReqDetails getServiceReqDetails() {
        return serviceReqDetails;
    }

    public String getVmmeSourceName() {
        return vmmeSourceName;
    }

    public String getVprobeSourceName() {
        return vprobeSourceName;
    }

    public CanvasManager getCanvasManager() {
        return canvasManager;
    }

    public CanvasElement getServiceElementVmmeSourceName() {
        return serviceElementVmmeSourceName;
    }

    public CanvasElement getServiceElementVprobeCollector() {
        return serviceElementVprobeCollector;
    }

    public Service getService() {
        return service;
    }
}
