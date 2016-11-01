package ch.ge.ve.protopoc.controller.impl;

import ch.ge.ve.protopoc.controller.api.HelloInterface;
import org.springframework.web.bind.annotation.RestController;

/**
 * This sample controller shows an example of a simple REST method.
 *
 * Security is handled at the interface level
 */
@RestController
public class HelloController implements HelloInterface {

    @Override
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
