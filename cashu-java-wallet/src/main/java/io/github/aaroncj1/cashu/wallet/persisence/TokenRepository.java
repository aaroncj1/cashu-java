package io.github.aaroncj1.cashu.wallet.persisence;

import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<TokenEntity, Long> {
}
