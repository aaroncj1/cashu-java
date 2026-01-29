package io.github.aaroncj1.cashu.wallet.persisence;

import io.github.aaroncj1.cashu.wallet.persisence.TokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TokenRepository extends CrudRepository<TokenEntity, Long> {
    List<TokenEntity> findByMintAndUnit(String mint, String unit);
}
