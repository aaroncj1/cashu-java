package io.github.aaroncj1.cashu.wallet.persisence;

import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class MintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String pubkey;
    private String version;
    private String description;
    private String description_long;
    private List<MintInfoResponse.Contact> contact;
    private String icon_url;
    private List<String> urls;
    private Timestamp time;
    private String tos_url;

    @OneToMany(mappedBy = "mint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NutEntity> nuts = new HashSet<>();

}
