# OpenECOMP SDC Onboarding(back-end)

---
---

# Introduction

SDC is the component within the design time environment that provides multiple organizations the ability to create and manage ECOMP assets in terms of “models”. SDC asset models are generally categorized into four object types: Resource, Service, Product and Offer.

# Compiling Onboarding be war

###SDC can be compiled easily with a `mvn clean install`. Integration tests are started with the following profile
   `-P with-integration-tests`

###Location of war : \sdc\openecomp-be\api\openecomp-sdc-rest-webapp\onboarding-rest-war\target\onboarding-be-1.0-SNAPSHOT.war

# Starting SDC

Steps :

### Copy onboarding war on jetty server : onboarding-be.war

###open rpm
###install jetty
###run installJettyBase.sh
###copy jvm.properties to base
###export variables
###run startJetty.sh

# Accessing SDC

You can access SDC at the following link : http://<hostname>:<portname>/sdc1/proxy-designer1#/onboardVendor

# Logging

SDC Onboarding supports EELF Logger, which is of the following types :

### Error
### Debug
### Metrics
### Audit

