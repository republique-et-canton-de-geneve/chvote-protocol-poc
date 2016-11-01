package ch.ge.ve.protopoc;

import ch.ge.ve.protopoc.model.entity.Account;
import ch.ge.ve.protopoc.model.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProtocolPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProtocolPocApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(AccountRepository accountRepository) {
        return args -> accountRepository.save(new Account("user", "password"));
    }
}
