package io.github.aaroncj1.cashu.wallet.persisence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String mint;
    private String unit;
    private String memo;
    private String keysetId;
    private long amount;
    private String secret;
    private String C;
    private String witness;
    private String e;
    private String s;
    private String r;
}
