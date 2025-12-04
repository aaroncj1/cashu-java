package io.github.aaroncj1.cashu.mint.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeysetRepository extends CrudRepository<KeysetEntity, String> {

    KeysetEntity findKeysetByKeysetId(String keysetId);

//    Object save(KeysetEntity keyset);
}
