package ch.ge.ve.protopoc.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Missing javadoc!
 */
public interface HelloInterface {
    @RequestMapping("/")
    @PreAuthorize("authenticated")
    String index();
}
