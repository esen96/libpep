# libpep
Policy enforcement point library from [NGAC SoS](https://github.com/esen96/sos-ngac). The library contains DTOs used in the NGAC SoS instead of resorting to the common files package in order to remove any project dependencies, which should make it compliant with any arrowhead java spring system. 

## Setup
In order to get the orchestration process validated, the arrowhead system containing this library needs to be authorized to consume the policy server query interface service. This is done by adding the corresponding authorization rules to the arrowhead database in your system, see the [arrowhead documentation](https://github.com/eclipse-arrowhead/core-java-spring) or [NGAC SoS documentation](https://github.com/esen96/sos-ngac) for instructions on how this is done.
