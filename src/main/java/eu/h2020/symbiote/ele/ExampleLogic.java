package eu.h2020.symbiote.ele;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.enabler.messaging.model.*;
import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.ProcessingLogic;
import eu.h2020.symbiote.enablerlogic.messaging.RegistrationHandlerClientService;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.model.cim.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ExampleLogic implements ProcessingLogic {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleLogic.class);
    
    private EnablerLogic enablerLogic;
    
    @Autowired
    private EnablerLogicProperties props;
    
    @Autowired
    private RegistrationHandlerClientService rhClientService;


    @Value("${platform.id}")
    private String myPlatformId;

    @Override
    public void initialization(EnablerLogic enablerLogic) {
        this.enablerLogic = enablerLogic;
    }

    String queryAir() {
        Optional<PlatformProxyResourceInfo> airResources = getAirResources();
        final String[] value = new String[1];
        airResources.ifPresent(resource -> {
            PlatformProxyTaskInfo taskInfo = new PlatformProxyTaskInfo();
            taskInfo.setTaskId("ZagrebResource");
            List<PlatformProxyResourceInfo> myList = new ArrayList<>();
            myList.add(resource);
            taskInfo.setResources(myList);
            LOG.info("Getting resources from {}", resource.getResourceId());
            List<Observation> observations = enablerLogic.readResource(taskInfo).getObservations();
            System.out.println(observations.size());
            observations.forEach(observation -> System.out.println(observation.getObsValues()));
            value[0] = observations.get(observations.size() -1).getObsValues().get(0).getValue();
        });
        return value[0];
    }

    private Optional<PlatformProxyResourceInfo> getAirResources() {
        CoreQueryRequest coreQueryRequest = new CoreQueryRequest();
        coreQueryRequest.setLocation_name("*Zagreb*");
        coreQueryRequest.setResource_type("StationarySensor");

        ResourceManagerTaskInfoRequest request = new ResourceManagerTaskInfoRequest(
                "airCondition", 1, 1, coreQueryRequest,
                null, //"P0000-00-00T00:01:00",
                false, null, false, props.getEnablerName(), null);

        ResourceManagerAcquisitionStartResponse response = enablerLogic.queryResourceManager(request);

        try {
            LOG.info("Response JSON: {}", new ObjectMapper().writeValueAsString(response));
        } catch (JsonProcessingException e) {
            LOG.info("Response: {}", response);
        }
        if(response.getStatus() != ResourceManagerTasksStatus.SUCCESS) {
            LOG.warn("Did not found aircondition actuator!!!");
            return Optional.empty();
        }

        ResourceManagerTaskInfoResponse resourceManagerTaskInfoResponse = response.getTasks().get(0);
        String resourceId = resourceManagerTaskInfoResponse.getResourceDescriptions().get(0).getId();
        String accessURL = resourceManagerTaskInfoResponse.getResourceUrls().get(resourceId);

        PlatformProxyResourceInfo info = new PlatformProxyResourceInfo();
        info.setAccessURL(accessURL);
        info.setResourceId(resourceId);
        return Optional.of(info);

    }

    @Override
    public void measurementReceived(EnablerLogicDataAppearedMessage dataAppeared) {
    }

    @Override
    public void notEnoughResources(NotEnoughResourcesAvailable notEnoughResourcesAvailable) {
        LOG.debug("Not enough resources");        
    }

    @Override
    public void resourcesUpdated(ResourcesUpdated resourcesUpdated) {
        LOG.debug("Resources updated from Enabler Resource Manager");
    }
}
