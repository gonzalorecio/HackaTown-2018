package eu.h2020.symbiote.ele;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class HackaTownController {

    @Autowired
    ExampleLogic exampleLogic;

    @GetMapping("/getData")
    public List<List<String> > getData() {
        List<String> properties = Arrays.asList("nitrogen dioxide concentration",
                //"ozone concentration",
                "carbon monoxide concentration"
                // "Particulate matter <10um (aerosol) concentration"
        );
        List<List<String> > result = new ArrayList<>(new ArrayList<>());
        for (String prop : properties) {
            result.add(exampleLogic.queryAir(prop));
        }
        return result;
    }

}
