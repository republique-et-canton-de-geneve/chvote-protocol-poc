package ch.ge.ve.protopoc.model.repository;

import ch.ge.ve.protopoc.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Missing javadoc!
 */
public interface AccountRepository extends JpaRepository<Account, Long>, QueryDslPredicateExecutor<Account> {
    Account findByUsername(String username);
}
