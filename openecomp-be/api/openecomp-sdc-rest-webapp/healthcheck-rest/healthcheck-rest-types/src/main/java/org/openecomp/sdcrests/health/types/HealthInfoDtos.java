package org.openecomp.sdcrests.health.types;


import java.util.List;
import java.util.stream.Collectors;

public class HealthInfoDtos {
    private List<HealthInfoDto> healthInfos;

    public HealthInfoDtos() {
    }

    public HealthInfoDtos(List<HealthInfoDto> healthInfos) {
        this.healthInfos = healthInfos;
    }

    public List<HealthInfoDto> getHealthInfos() {
        return healthInfos;
    }

    public void setHealthInfos(List<HealthInfoDto> healthInfos) {
        this.healthInfos = healthInfos;
    }

    @Override
    public String toString() {
       return  healthInfos.stream().map(healthInfoDto -> healthInfoDto.toString())
               .collect(Collectors.joining(", "));

    }
}
