package eu.h2020.symbiote.ele;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
public class HackaTownController {

    @Autowired
    ExampleLogic exampleLogic;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/getData")
    public Map<String, List<Sensor>> getData() {
        Map<String, String> tags = new HashMap<>();
        // tags.put("carbon monoxide concentration","co2");
        tags.put("volatile PM10 concentration","PM10");
        tags.put("nitrogen dioxide concentration", "no2");

        Map<String, List<Sensor>> sensors = new HashMap<>();
        tags.forEach ((key, value) -> sensors.put(value, exampleLogic.queryAir(key)));
        return sensors;
    }

    @GetMapping("prueba")
    public List<Object> prueba() {
        List<Object> result = restTemplate.getForObject("http://localhost:8080", List.class, "42", "21");
        return result;
    }

}
