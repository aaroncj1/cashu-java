package io.github.aaroncj1.cashu.wallet.service;

import io.github.aaroncj1.cashu.wallet.persisence.MintRepository;

public class MintRegistry {

    private final MintRepository mintRepository;

    public MintRegistry(MintRepository mintRepository) {
        this.mintRepository = mintRepository;
    }

    public void addMint(String mintUrl) {

    }

}
