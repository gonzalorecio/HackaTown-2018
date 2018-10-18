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
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

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

    List<Sensor> queryAir(String prop) {
        Map<Optional<PlatformProxyResourceInfo>, Pair<Double, Double>> airResources = getAirResources(prop);
        List<Sensor> observations = new ArrayList<>();
        airResources.forEach((airResource, location) -> {
            Sensor sensor = getSensors(airResource);
            if (sensor.value != null){
                sensor.setLatitude(location.getKey());
                sensor.setLongitude(location.getValue());
                observations.add(sensor);
            }
        });
        return observations;
    }

    private Sensor getSensors(Optional<PlatformProxyResourceInfo> airResource) {
        final Sensor sensor = new Sensor();

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
                    int index = observations.size() - 1;
                    List<ObservationValue> obsValues = observations.get(index).getObsValues();
                    if (!obsValues.isEmpty() &&
                            obsValues.get(0).getValue() !=null &&
                            obsValues.get(0).getUom().getSymbol().contains("g/m") &&
                            !obsValues.get(0).getUom().getSymbol().contains("mg")) {
                        sensor.value = obsValues.get(0).getValue();
                        sensor.uom = obsValues.get(0).getUom();
                    }
                }
            }
        });
        return sensor;
    }

    private Map<Optional<PlatformProxyResourceInfo>, Pair<Double, Double>> getAirResources(String prop) {
        HashMap<Optional<PlatformProxyResourceInfo>, Pair<Double, Double>> map = new HashMap<>();
        ResourceManagerTaskInfoResponse task = getResourceManagerTaskInfoResponses(prop).get(0);
        List<QueryResourceResult> resourceDescriptions = task.getResourceDescriptions();
        for (QueryResourceResult resource : resourceDescriptions) {
            String resourceId = resource.getId();
            String accessURL = task.getResourceUrls().get(resourceId);

            PlatformProxyResourceInfo info = new PlatformProxyResourceInfo();
            info.setAccessURL(accessURL);
            info.setResourceId(resourceId);
            map.put(Optional.of(info), new Pair<>(resource.getLocationLatitude(), resource.getLocationLongitude()));
        }
        return map;

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
