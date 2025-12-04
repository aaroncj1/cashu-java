package io.github.aaroncj1.cashu.wallet.persisence;

import jakarta.persistence.*;

@Entity
public class NutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mint_id", nullable = false)
    private MintEntity mint;
    private Boolean enabled;
    private Boolean supported;
    private String method;
    private String unit;
    private String min_amount;
    private String max_amount;
}
