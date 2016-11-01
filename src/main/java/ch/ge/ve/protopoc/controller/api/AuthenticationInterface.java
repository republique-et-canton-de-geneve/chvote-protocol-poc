package ch.ge.ve.protopoc.controller.api;

import ch.ge.ve.protopoc.jwt.JwtAuthenticationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface defines the contracts for an {@link ch.ge.ve.protopoc.controller.impl.AuthenticationController}
 */
public interface AuthenticationInterface {
    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest);

    @RequestMapping(value = "/auth/renew", method = RequestMethod.GET)
    @PreAuthorize(value = "authenticated")
    ResponseEntity<?> renewAuthenticationToken(HttpServletRequest request);
}
