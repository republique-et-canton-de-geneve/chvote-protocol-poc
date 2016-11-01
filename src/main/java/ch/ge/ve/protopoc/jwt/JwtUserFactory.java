package ch.ge.ve.protopoc.jwt;

import ch.ge.ve.protopoc.model.entity.Account;
import ch.ge.ve.protopoc.model.entity.Authority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(Account account) {
        return new JwtUser(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                mapToGrantedAuthorities(account.getAuthorities())
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
                .collect(Collectors.toList());
    }
}
