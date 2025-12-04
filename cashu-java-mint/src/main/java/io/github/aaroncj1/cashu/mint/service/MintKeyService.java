package io.github.aaroncj1.cashu.mint.service;

import io.github.aaroncj1.cashu.core.crypto.impl.CryptoUtils;
import io.github.aaroncj1.cashu.core.mint.Keys;
import io.github.aaroncj1.cashu.core.model.KeysetFullDetails;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDPath;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class MintKeyService {

    private static final String UNIT = "sat";
    private static final Integer input_fee_ppk = 100;
    private final DeterministicSeed MASTER_SEED;
    private final String ACTIVE_PATH;

    public MintKeyService(@Value("${cashu.masterSeed}") String masterSeed, @Value("${cashu.activePath}") String activePath) throws UnreadableWalletException {
        MASTER_SEED = initializeSeedFromMnemonic(masterSeed);
        ACTIVE_PATH = activePath;
    }

    private static DeterministicSeed initializeSeedFromMnemonic(String masterSeed) throws UnreadableWalletException {
        return new DeterministicSeed(masterSeed, null, "", 1);
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            // SHA‑256 is guaranteed to exist on every standard Java platform
            throw new AssertionError(e);
        }
    }

    private void createNewPrivateKey() {
        // 128‑bit entropy → 12‑word mnemonic (you can also ask for 256‑bit → 24 words)
        SecureRandom random = new SecureRandom();
        byte[] entropy = new byte[16]; // 16 * 8 = 128 bits
        random.nextBytes(entropy);
        System.out.println(new DeterministicSeed(entropy, "", System.currentTimeMillis() / 1000L));
    }

    private DeterministicKey deriveKeyFromPath(String path) {
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder()
                .seed(MASTER_SEED)
                .build();
        DeterministicKey child = keyChain.getRootKey();

        for (ChildNumber cn : HDPath.parsePath(path)) {
            assert child != null;
            child = HDKeyDerivation.deriveChildKey(child, cn);
        }
        return child;
    }

    public KeysetFullDetails constructActiveKeyset() {
        return constructKeysetByPath(ACTIVE_PATH);
    }

    public KeysetFullDetails constructKeysetByPath(String path) {
        String[] keys = new String[64];
        for (int i = 0; i < 64; i++)
            keys[i] = deriveKeyFromPath(path + "/" + i).getPublicKeyAsHex();
        String keysetId = calculateKeysetId(keys);
        KeysetFullDetails keysetFullDetails = new KeysetFullDetails(keysetId,
                input_fee_ppk, UNIT, true, new Keys(keys, keysetId), ACTIVE_PATH);
        return keysetFullDetails;
    }

    public String calculateKeysetId(String[] keys) {
        int FINGERPRINT_HEX_LEN = 14;     // 14 hex chars == 7 bytes
        String VERSION = "00";     // 14 hex chars == 7 bytes

        int totalLen = 0;
        for (String hex : keys) {
            totalLen += CryptoUtils.hexToBytes(hex).length;
        }

        byte[] concatenated = new byte[totalLen];
        int pos = 0;
        for (String hex : keys) {
            byte[] keyBytes = CryptoUtils.hexToBytes(hex);
            System.arraycopy(keyBytes, 0, concatenated, pos, keyBytes.length);
            pos += keyBytes.length;
        }

        byte[] hash = sha256(concatenated);

        String fullHex = CryptoUtils.bytesToHex(hash);          // 64 hex chars (32 bytes)
        String fingerprintHex = fullHex.substring(0, FINGERPRINT_HEX_LEN);
        return VERSION + fingerprintHex;
    }

}
