package cashu.java.common.api.response;

import cashu.java.common.BlindSignature;

import java.util.List;

public class MeltResponse {
    public boolean paid;
    public List<BlindSignature> change;

}
