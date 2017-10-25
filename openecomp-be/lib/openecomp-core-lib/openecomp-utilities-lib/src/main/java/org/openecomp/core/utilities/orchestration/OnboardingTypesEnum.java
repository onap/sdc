package org.openecomp.core.utilities.orchestration;

import java.util.Optional;

import static java.util.Arrays.asList;
public enum OnboardingTypesEnum {
    CSAR("csar"), ZIP("zip"), NONE("none");
    private String type;

    OnboardingTypesEnum(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static final OnboardingTypesEnum getOnboardingTypesEnum(final String inStr) {
        if (inStr == null) {
            return null;
        }

        Optional<OnboardingTypesEnum>  onboardingTypesOptional =  asList(OnboardingTypesEnum.values()).stream()
                .filter(onboardingTypesEnum -> onboardingTypesEnum.toString().equals(inStr.toLowerCase()))
            .findAny();
      return onboardingTypesOptional.orElse(null);
    }

}
