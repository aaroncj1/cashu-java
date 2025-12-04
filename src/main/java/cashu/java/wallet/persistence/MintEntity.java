package cashu.java.wallet.persistence;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "mint")
public class MintEntity {
    @Id
    @GeneratedValue
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    public MintEntity(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public MintEntity() {

    }
}
