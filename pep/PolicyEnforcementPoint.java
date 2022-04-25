package ai.aitia.sos_ngac.resource_system.pep;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import ai.aitia.arrowhead.application.library.ArrowheadService;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

/* 
 * Policy Enforcement Point Spring Component for consuming the query interface service
 * from the policy server provider
 */

@Component
public class PolicyEnforcementPoint {
	
	// PEP constants
	private final String QUERY_INTERFACE_SERVICE_DEFINITION = "query-interface";
	private final String HTTP_METHOD = "http-method";
	private final String INTERFACE_SECURE = "HTTP-SECURE-JSON";
	private final String INTERFACE_INSECURE = "HTTP-INSECURE-JSON";
	private final String ACCESS_QUERY = "access?";
	private final String CONDITIONAL_ACCESS_QUERY = "caccess?";
	
	// Components
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	protected SSLProperties sslProperties;
	
	private final Logger logger = LogManager.getLogger(PolicyEnforcementPoint.class);
	

	/* 
	 * Entry function for the policy enforcement point. Takes a resource 
	 * request DTO sent by a consumer and returns the policy response DTO 
	 * generated by the policy server
	 */
	public PolicyResponseDTO queryPolicyServer(ResourceRequestDTO requestDTO) {
		
		// Orchestrate the arrowhead system/policy server interaction
		final OrchestrationResultDTO orchestrationResult = orchestrate(QUERY_INTERFACE_SERVICE_DEFINITION);
		
		// Consume policy server query interface service and get the server response
		PolicyResponseDTO policyServerResponse = consumePQI(orchestrationResult, requestDTO);
		
		return policyServerResponse;
	}
	
	
	// Arrowhead orchestration function. Returns an arrowhead OrchestrationResultDTO
    private OrchestrationResultDTO orchestrate(final String serviceDefinition) {
    	final ServiceQueryFormDTO serviceQueryForm = new ServiceQueryFormDTO.Builder(serviceDefinition)
    			.interfaces(getInterface())
    			.build();
    	
    	final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
    	final OrchestrationFormRequestDTO orchestrationFormRequest = orchestrationFormBuilder.requestedService(serviceQueryForm)
    			.flag(Flag.MATCHMAKING, true)
    			.flag(Flag.OVERRIDE_STORE, true)
    			.build();
    	
    	final OrchestrationResponseDTO orchestrationResponse = arrowheadService.proceedOrchestration(orchestrationFormRequest);
    	
    	if (orchestrationResponse == null) {
    		logger.info("No orchestration response received");
    	} else if (orchestrationResponse.getResponse().isEmpty()) {
    		logger.info("No provider found during the orchestration");
    	} else {
    		final OrchestrationResultDTO orchestrationResult = orchestrationResponse.getResponse().get(0);
    		validateOrchestrationResult(orchestrationResult, serviceDefinition);
    		return orchestrationResult;
    	}
    	throw new ArrowheadException("Unsuccessful orchestration: " + serviceDefinition);
    }
    
    // Consume the defined arrowhead service 
    private PolicyResponseDTO consumePQI(final OrchestrationResultDTO orchestrationResult, ResourceRequestDTO requestDTO) {
    	final String token = orchestrationResult.getAuthorizationTokens() == null ? null : orchestrationResult.getAuthorizationTokens().get(getInterface());
		
    	// Generate a PolicyRequestDTO from the given ResourceRequestDTO
    	final PolicyRequestDTO policyRequestDTO;
    	
    	if (requestDTO.conditionIsSet()) {
    		policyRequestDTO = new PolicyRequestDTO(CONDITIONAL_ACCESS_QUERY, new String[] {
    				requestDTO.getUser(),
    				requestDTO.getOperation(),
    				requestDTO.getObject(),
    				requestDTO.getCondition()
    		});
    	} else {
    		policyRequestDTO = new PolicyRequestDTO(ACCESS_QUERY, new String[] {
    				requestDTO.getUser(),
    				requestDTO.getOperation(),
    				requestDTO.getObject()
    		});
    	}
		
    	// Consume the service and return the server response
		return arrowheadService.consumeServiceHTTP(PolicyResponseDTO.class, HttpMethod.valueOf(orchestrationResult.getMetadata().get(HTTP_METHOD)),
				orchestrationResult.getProvider().getAddress(), orchestrationResult.getProvider().getPort(), orchestrationResult.getServiceUri(),
				getInterface(), token, policyRequestDTO, new String[0]);
    }
    

    // Arrowhead helper function. Gets the right interface depending on the SSL properties of the service
    private String getInterface() {
    	return sslProperties.isSslEnabled() ? INTERFACE_SECURE : INTERFACE_INSECURE;
    }
    
    // Arrowhead helper function. Validates the orchestration process
    private void validateOrchestrationResult(final OrchestrationResultDTO orchestrationResult, final String serviceDefinitin) {
    	if (!orchestrationResult.getService().getServiceDefinition().equalsIgnoreCase(serviceDefinitin)) {
			throw new InvalidParameterException("Requested and orchestrated service definition do not match");
		}
    	
    	boolean hasValidInterface = false;
    	for (final ServiceInterfaceResponseDTO serviceInterface : orchestrationResult.getInterfaces()) {
			if (serviceInterface.getInterfaceName().equalsIgnoreCase(getInterface())) {
				hasValidInterface = true;
				break;
			}
		}
    	if (!hasValidInterface) {
    		throw new InvalidParameterException("Requested and orchestrated interface do not match");
		}
    }
    
}
