package eu.h2020.symbiote.ele;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class HackaTownController {

    @Autowired
    ExampleLogic exampleLogic;

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

}
