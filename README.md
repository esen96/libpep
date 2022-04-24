# libpep
Policy enforcement point library from [ngac sos](https://github.com/esen96/sos-ngac). Contains the DTOs from the ngac sos common files package in order to remove project dependencies, making this java spring component compatible with other arrowhead systems.

## Setup
To get the orchestration process validated, the arrowhead system containing this library needs to be authorized to consume the policy server query interface service. This is done by adding the corresponding authorization rules to your systems arrowhead database, see the [arrowhead](https://github.com/eclipse-arrowhead/core-java-spring)- or [ngac sos](https://github.com/esen96/sos-ngac) documentation for instructions on how this is done.
