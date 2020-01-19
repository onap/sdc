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

package org.openecomp.sdc.be.datatypes.components;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFieldsExtractor;



public class ServiceMetadataDataDefinition extends ComponentMetadataDataDefinition {
    private static final String EMPTY_STR = "";

    private String distributionStatus;
    private String serviceType;
    private String serviceRole;
    private String instantiationType = EMPTY_STR;
	private String serviceFunction;

    private Boolean ecompGeneratedNaming = true;

    private String namingPolicy = EMPTY_STR;

    private String environmentContext;

    public ServiceMetadataDataDefinition() {
        super();
        serviceType = "";
        serviceRole = "";
		serviceFunction = "";
    }


    public ServiceMetadataDataDefinition(ServiceMetadataDataDefinition other) {
        super(other);
        serviceType = other.getServiceType();
        serviceRole = other.getServiceRole();
		serviceFunction = other.getServiceFunction();
	}

	public ServiceMetadataDataDefinition(JsonPresentationFieldsExtractor extractor){
		super(extractor);
		serviceType = extractor.getServiceType();
		serviceRole = extractor.getServiceRole();
		serviceFunction = extractor.getServiceFunction();
    }

    public String getDistributionStatus() {
        return distributionStatus;
    }

    public void setDistributionStatus(String distributionStatus) {
        this.distributionStatus = distributionStatus;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public String getInstantiationType() {
        return instantiationType;
    }

    public void setInstantiationType(String instantiationType) {
        this.instantiationType = instantiationType;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    public Boolean isEcompGeneratedNaming() {
        return ecompGeneratedNaming;
    }

	public Boolean getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

    public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
        this.ecompGeneratedNaming = ecompGeneratedNaming;
    }

    public String getNamingPolicy() {
        return namingPolicy;
    }

    public void setNamingPolicy(String namingPolicy) {
        this.namingPolicy = namingPolicy;
    }

    public String getEnvironmentContext() {
        return environmentContext;
    }

    public void setEnvironmentContext(String environmentContext) {
        this.environmentContext = environmentContext;
    }

	public String getServiceFunction() {
		return serviceFunction;
	}

	public void setServiceFunction(String serviceFunction) {
		this.serviceFunction = serviceFunction;
	}

	@Override
	public String toString() {
		return "ServiceMetadataDataDefinition{" +
				"distributionStatus='" + distributionStatus + '\'' +
				", serviceType='" + serviceType + '\'' +
				", serviceRole='" + serviceRole + '\'' +
				", serviceFunction='" + serviceFunction + '\'' +
				", ecompGeneratedNaming=" + ecompGeneratedNaming +
				", namingPolicy='" + namingPolicy + '\'' +
				", environmentContext='" + environmentContext + '\'' +
				", instantiationType='" + instantiationType + '\'' +
				'}';
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((distributionStatus == null) ? 0 : distributionStatus.hashCode());
        result = prime * result + ((ecompGeneratedNaming == null) ? 0 : ecompGeneratedNaming.hashCode());
        result = prime * result + ((namingPolicy == null) ? 0 : namingPolicy.hashCode());
        result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
        result = prime * result + ((serviceRole == null) ? 0 : serviceRole.hashCode());
		result = prime * result + ((serviceFunction == null) ? 0 : serviceFunction.hashCode());
        result = prime * result + ((environmentContext == null) ? 0 : environmentContext.hashCode());
        result = prime * result + ((instantiationType == null) ? 0 : instantiationType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ComponentMetadataDataDefinition)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServiceMetadataDataDefinition other = (ServiceMetadataDataDefinition) obj;
        if (distributionStatus == null) {
            if (other.distributionStatus != null) {
                return false;
            }
        } else if (!distributionStatus.equals(other.distributionStatus)) {
            return false;
        }
        if (ecompGeneratedNaming == null) {
            if (other.ecompGeneratedNaming != null) {
                return false;
            }
        } else if (!ecompGeneratedNaming.equals(other.ecompGeneratedNaming)) {
            return false;
        }
        if (namingPolicy == null) {
            if (other.namingPolicy != null) {
                return false;
            }
        } else if (!namingPolicy.equals(other.namingPolicy)) {
            return false;
        }
        if (serviceType == null) {
            if (other.serviceType != null) {
                return false;
            }
        } else if (!serviceType.equals(other.serviceType)) {
            return false;
        }
        if (serviceRole == null) {
            if (other.serviceRole != null) {
                return false;
            }
        } else if (!serviceRole.equals(other.serviceRole)) {
            return false;
        }
        if (environmentContext == null) {
            if (other.environmentContext != null) {
                return false;
            }
        } else if (!environmentContext.equals(other.environmentContext)) {
            return false;
        }
        if (instantiationType == null) {
            if (other.instantiationType != null) {
                return false;
            }
        } else if (!instantiationType.equals(other.instantiationType)) {
            return false;
        }
        if (serviceFunction == null){
            if (other.serviceFunction != null) {
                return false;
        } else if (!serviceFunction.equals(other.serviceFunction))
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String getActualComponentType() {

        return componentType != null ? componentType.getValue() : "";
    }

}
