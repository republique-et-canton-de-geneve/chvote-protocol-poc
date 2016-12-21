package ch.ge.ve.protopoc.controller.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This sample interface serves as example of the routing and access restriction mechanisms
 */
public interface HomeInterface {
    @RequestMapping("/")
    @PreAuthorize("authenticated")
    String index();
}
