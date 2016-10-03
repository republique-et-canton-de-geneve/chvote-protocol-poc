package ch.ge.ve.protopoc.controller;

import org.springframework.web.bind.annotation.RestController;

/**
 * Missing javadoc!
 */
@RestController
public class HelloController implements ch.ge.ve.protopoc.services.HelloInterface {

    @Override
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
