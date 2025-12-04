package io.github.aaroncj1.cashu.mint.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeysetEntity {

    @Id
    private String keysetId;
    private String path;
    private int input_fee_ppk;
    private String unit;
    private boolean active;

    public String getKeysetId() {
        return keysetId;
    }

    public void setKeysetId(String keysetId) {
        this.keysetId = keysetId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getInput_fee_ppk() {
        return input_fee_ppk;
    }

    public void setInput_fee_ppk(int input_fee_ppk) {
        this.input_fee_ppk = input_fee_ppk;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
