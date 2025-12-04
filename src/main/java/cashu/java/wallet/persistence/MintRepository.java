package cashu.java.wallet.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MintRepository extends JpaRepository<MintEntity, Long> {
}
