package io.github.aaroncj1.cashu.mint.service;


import io.github.aaroncj1.cashu.core.mint.Keys;
import io.github.aaroncj1.cashu.core.model.KeysetFullDetails;
import io.github.aaroncj1.cashu.core.model.KeysetKeys;
import io.github.aaroncj1.cashu.core.model.KeysetSummary;
import io.github.aaroncj1.cashu.mint.persistence.KeysetEntity;
import io.github.aaroncj1.cashu.mint.persistence.KeysetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicKeysService {

    private final MintKeyService mintKeyManagement;
    private final KeysetRepository keysetRepository;
    private final Map<String, KeysetFullDetails> activeKeysets;
    private final Map<String, KeysetFullDetails> keysetCache;


    public PublicKeysService(MintKeyService mintKeyManagement, KeysetRepository keysetRepository) {
        this.mintKeyManagement = mintKeyManagement;
        this.keysetRepository = keysetRepository;
        this.activeKeysets = new HashMap<>();
        this.keysetCache = new HashMap<>();
        KeysetFullDetails keys = mintKeyManagement.constructActiveKeyset();
        KeysetEntity entity = new KeysetEntity(keys.id(), keys.path(), keys.input_fee_ppk(), keys.unit(), true);
        keysetRepository.save(entity);
        activeKeysets.put(keys.id(), keys);
        keysetCache.put(keys.id(), keys);
    }

    public List<KeysetKeys> getActiveKeysets() throws Exception {
        List<KeysetKeys> activeKeysetList = new ArrayList<>();

        if (activeKeysets.isEmpty()) {
            Iterable<KeysetEntity> keysetEntityIterable = keysetRepository.findAll();
            refreshCache(keysetEntityIterable);
        }
        for (Map.Entry<String, KeysetFullDetails> entry : activeKeysets.entrySet()) {
            KeysetKeys keysetKeys = new KeysetKeys(entry.getValue());
            activeKeysetList.add(keysetKeys);
        }
        return activeKeysetList; // TODO all units
    }

    public KeysetFullDetails getKeysetById(String keysetId) throws Exception {
        KeysetFullDetails keysetFullDetails = keysetCache.get(keysetId);
        if (keysetFullDetails == null)
            keysetFullDetails = constructKeysetByKeysetId(keysetId);
        return keysetFullDetails;
    }

    public List<KeysetSummary> getAllKeysets() throws Exception {
        Iterable<KeysetEntity> keysetEntityIterable = keysetRepository.findAll();
        refreshCache(keysetEntityIterable);
        return keysetEntityIterableToKeysetList(keysetEntityIterable);
    }

    private List<KeysetSummary> keysetEntityIterableToKeysetList(Iterable<KeysetEntity> keysetEntityIterable) throws Exception {
        List<KeysetSummary> keysetList = new ArrayList<>();
        keysetEntityIterable.forEach(keysetEntity -> {
            KeysetSummary keyset = new KeysetSummary(keysetEntity.getKeysetId(),
                    keysetEntity.getInput_fee_ppk(), keysetEntity.getUnit(), keysetEntity.isActive());
            keysetList.add(keyset);
        });
        return keysetList;
    }

    private void refreshCache(Iterable<KeysetEntity> keysetEntityIterable) throws Exception {
        for (KeysetEntity entity : keysetEntityIterable) {
            KeysetFullDetails keysetFullDetails = constructKeysetByKeysetId(entity.getKeysetId());
            keysetCache.put(entity.getKeysetId(), keysetFullDetails);
        }
    }

    private KeysetFullDetails constructKeysetByKeysetId(String keysetId) throws Exception {
        KeysetEntity keysetEntity = keysetRepository.findKeysetByKeysetId(keysetId);
        return mintKeyManagement.constructKeysetByPath(keysetEntity.getPath());
    }
}