package eu.h2020.symbiote.ele;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.enabler.messaging.model.*;
import eu.h2020.symbiote.enablerlogic.EnablerLogic;
import eu.h2020.symbiote.enablerlogic.ProcessingLogic;
import eu.h2020.symbiote.enablerlogic.messaging.RegistrationHandlerClientService;
import eu.h2020.symbiote.enablerlogic.messaging.properties.EnablerLogicProperties;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.model.cim.ObservationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
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

    List<String> queryAir(String prop) {
        List<Optional<PlatformProxyResourceInfo>> airResources = getAirResources(prop);
        List<String> observations = new ArrayList<>();
        for (Optional<PlatformProxyResourceInfo> airResource : airResources) {
            observations.add(getObservation(airResource));
        }
        return observations;
    }

    private String getObservation(Optional<PlatformProxyResourceInfo> airResource) {
        final String[] value = new String[1];

        airResource.ifPresent(resource -> {
            PlatformProxyTaskInfo taskInfo = new PlatformProxyTaskInfo();
            taskInfo.setTaskId("ZagrebResource");
            List<PlatformProxyResourceInfo> myList = new ArrayList<>();
            myList.add(resource);
            taskInfo.setResources(myList);
            LOG.info("Getting resources from {}", resource.getResourceId());
            EnablerLogicDataAppearedMessage enablerLogicDataAppearedMessage = enablerLogic.readResource(taskInfo);
            if (enablerLogicDataAppearedMessage != null) {
                List<Observation> observations = enablerLogicDataAppearedMessage.getObservations();
                System.out.println(observations.size());
                observations.forEach(observation -> System.out.println(observation.getObsValues()));
                if (!observations.isEmpty()) {
                    List<ObservationValue> obsValues = observations.get(observations.size() - 1).getObsValues();
                    if (!obsValues.isEmpty())
                        value[0] = obsValues.get(0).getValue();
                }
            }
        });
        return value[0];
    }

    private List<Optional<PlatformProxyResourceInfo> > getAirResources(String prop) {
        //List<String> properties = Arrays.asList("nitrogen dioxide concentration", "ozone concentration", "carbon monoxide concentration", "Particulate matter <10um (aerosol) concentration");
        //List<ResourceManagerTaskInfoResponse> tasks = new ArrayList<>();
        //for (String prop : properties) {
        //    tasks.add(getResourceManagerTaskInfoResponses(prop).get(0));
        //}
        List<Optional<PlatformProxyResourceInfo> > resources = new ArrayList<>();
        ResourceManagerTaskInfoResponse task = getResourceManagerTaskInfoResponses(prop).get(0);
        // for (ResourceManagerTaskInfoResponse task : tasks) {
            List<QueryResourceResult> resourceDescriptions = task.getResourceDescriptions();
            for (QueryResourceResult resource : resourceDescriptions) {
                String resourceId = resource.getId();
                String accessURL = task.getResourceUrls().get(resourceId);

                PlatformProxyResourceInfo info = new PlatformProxyResourceInfo();
                info.setAccessURL(accessURL);
                info.setResourceId(resourceId);
                resources.add(Optional.of(info));
            }
        //}
        return resources;

    }

    private List<ResourceManagerTaskInfoResponse> getResourceManagerTaskInfoResponses(String prop) {
        CoreQueryRequest coreQueryRequest = new CoreQueryRequest();
        coreQueryRequest.setLocation_name("*Zagreb*");
        coreQueryRequest.setResource_type("StationarySensor");;
        coreQueryRequest.setObserved_property(Arrays.asList(prop));

        ResourceManagerTaskInfoRequest request = new ResourceManagerTaskInfoRequest(
                "airCondition", 1, 10, coreQueryRequest,
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
        }

        return response.getTasks();
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
