/*-------------------------------------------------------------------------------------------------
 - #%L                                                                                            -
 - chvote-protocol-poc                                                                            -
 - %%                                                                                             -
 - Copyright (C) 2016 - 2017 République et Canton de Genève                                       -
 - %%                                                                                             -
 - This program is free software: you can redistribute it and/or modify                           -
 - it under the terms of the GNU Affero General Public License as published by                    -
 - the Free Software Foundation, either version 3 of the License, or                              -
 - (at your option) any later version.                                                            -
 -                                                                                                -
 - This program is distributed in the hope that it will be useful,                                -
 - but WITHOUT ANY WARRANTY; without even the implied warranty of                                 -
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the                                   -
 - GNU General Public License for more details.                                                   -
 -                                                                                                -
 - You should have received a copy of the GNU Affero General Public License                       -
 - along with this program. If not, see <http://www.gnu.org/licenses/>.                           -
 - #L%                                                                                            -
 -------------------------------------------------------------------------------------------------*/

package ch.ge.ve.protopoc.config;

import ch.ge.ve.protopoc.model.entity.Account;
import ch.ge.ve.protopoc.model.repository.AccountRepository;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This configuration class manages the authentication service.
 *
 * It defines the userDetailService used by the default {@link AuthenticationManagerBuilder}, so that the
 * underlying authentication is performed using the {@link AccountRepository}
 */
@Configuration
public class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfiguration.class);
    private AccountRepository accountRepository;

    @Autowired
    public WebSecurityConfiguration(AccountRepository accountRepository) {
        Preconditions.checkNotNull(accountRepository, "AccountRepository is required");
        this.accountRepository = accountRepository;
    }

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            LOGGER.debug(String.format("Looking for user [%s]", username));
            Account account = accountRepository.findByUsername(username);
            if (account != null) {
                LOGGER.info(String.format("Found user [%s]", username));
                return new User(account.getUsername(), account.getPassword(),
                        true, true, true, true,
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                LOGGER.info(String.format("Couldn't find user [%s]", username));
                throw new UsernameNotFoundException(String.format("couldn't find the user '%s'", username));
            }
        };
    }
}
