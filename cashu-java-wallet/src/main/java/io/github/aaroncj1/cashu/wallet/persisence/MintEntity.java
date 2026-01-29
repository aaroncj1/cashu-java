package io.github.aaroncj1.cashu.wallet.persisence;

import io.github.aaroncj1.cashu.core.model.api.mintInfo.v1.response.MintInfoResponse;
import io.github.aaroncj1.cashu.wallet.persisence.converters.ContactListConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class MintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String pubkey;
    private String version;
    private String description;
    @Column(length = 4096)
    private String description_long;
    
    @Convert(converter = ContactListConverter.class)
    @Column(length = 4096)
    private List<MintInfoResponse.Contact> contact;
    
    private String icon_url;
    
    @ElementCollection
    private List<String> urls;
    
    private Timestamp time;
    private String tos_url;

    @OneToMany(mappedBy = "mint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NutEntity> nuts = new HashSet<>();

}
