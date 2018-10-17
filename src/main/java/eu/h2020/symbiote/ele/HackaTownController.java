package eu.h2020.symbiote.ele;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HackaTownController {

    @Autowired
    ExampleLogic exampleLogic;

    @GetMapping("/sex")
    public List<String> getSex() {
        List<String> sexs = new ArrayList<>();
        sexs.add(exampleLogic.queryAir());
        return sexs;
    }

}
