//package io.github.aaroncj1.cashu.core.mint.service;
//
//import io.github.aaroncj1.cashu.core.mint.Keys;
//import io.github.aaroncj1.cashu.core.mint.MintKeyManagement;
//import io.github.aaroncj1.cashu.core.mint.persistence.KeysetEntity;
//import io.github.aaroncj1.cashu.core.mint.persistence.KeysetRepository;
//
//import java.util.Map;
//
//public class KeyService {
//
//    private final MintKeyManagement mintKeyManagement;
//    private final KeysetRepository keysetRepository;
//    private final Map<String, Keys> keysetCache;
//
//
//    public KeyService(MintKeyManagement mintKeyManagement, KeysetRepository keysetRepository, Map<String, Keys> keysetCache) {
//        this.mintKeyManagement = mintKeyManagement;
//        this.keysetRepository = keysetRepository;
//        this.keysetCache = keysetCache;
//    }
//
//    public Keys getKeysetById(String keysetId) throws Exception {
//        Keys keys = keysetCache.get(keysetId);
//        if (keys == null)
//            keys = constructKeysetByKeysetId(keysetId);
//        return keys;
//    }
//
//    private Keys constructKeysetByKeysetId(String keysetId) throws Exception {
//        KeysetEntity keysetEntity = keysetRepository.findKeysetByID(keysetId);
//        return mintKeyManagement.constructKeysetByPath(keysetEntity.getPath());
//    }
//}