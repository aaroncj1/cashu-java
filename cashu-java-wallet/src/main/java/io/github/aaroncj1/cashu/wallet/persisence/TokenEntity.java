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

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getMint() { return mint; }
    public void setMint(String mint) { this.mint = mint; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getKeysetId() { return keysetId; }
    public void setKeysetId(String keysetId) { this.keysetId = keysetId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getC() { return C; }
    public void setC(String C) { this.C = C; }
    public String getWitness() { return witness; }
    public void setWitness(String witness) { this.witness = witness; }
    public String getE() { return e; }
    public void setE(String e) { this.e = e; }
    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public String getR() { return r; }
    public void setR(String r) { this.r = r; }
}
