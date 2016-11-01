package ch.ge.ve.protopoc.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This sample interface serves as example of the routing and access restriction mechanisms
 */
public interface HelloInterface {
    @RequestMapping("/")
    @PreAuthorize("authenticated")
    String index();
}
