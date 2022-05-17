# libpep
Policy enforcement point library from [ngac sos](https://github.com/esen96/sos-ngac). The library serves to ease a clients interaction with the policy server by providing a single entry point for access control, which returns the server response upon calling the overloaded entry function. Contains policy DTO:s to remove project dependencies, making this library compatible with other Java Spring Arrowhead applications.

## Setup
To get the orchestration process validated, the arrowhead system containing this library needs to be authorized to consume the query interface service from the policy server provider. This is done by adding the corresponding authorization rules to your systems arrowhead database, see the [arrowhead](https://github.com/eclipse-arrowhead/core-java-spring)- or [ngac sos](https://github.com/esen96/sos-ngac) documentation for instructions on how this is done.

### Invocation
As this is a Spring Component, it can be autowired by the Arrowhead Java Spring system containing it

```java
@Autowired 
  private PolicyEnforcementPoint pep;
```
The library has one overloaded entry function that returns a policy response DTO from the NGAC policy server. 

Standard access control:

```java
  PolicyResponseDTO serverResponse = pep.accessControl("Username", "Operation", "Object");
```

Conditional access control:

```java
  PolicyResponseDTO serverResponse = pep.accessControl("Username", "Operation", "Object", "Condition");
```
