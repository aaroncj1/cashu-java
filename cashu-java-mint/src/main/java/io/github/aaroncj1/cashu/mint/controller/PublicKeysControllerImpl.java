package io.github.aaroncj1.cashu.mint.controller;

import io.github.aaroncj1.cashu.core.mint.Keys;
import io.github.aaroncj1.cashu.core.mint.controller.KeysController;
import io.github.aaroncj1.cashu.core.model.KeysetKeys;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.ActiveKeysetsResponse;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.KeysResponse;
import io.github.aaroncj1.cashu.core.model.api.keys.v1.response.KeysetsResponse;
import io.github.aaroncj1.cashu.mint.service.PublicKeysService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PublicKeysControllerImpl implements KeysController {

    private final PublicKeysService publicKeysService;

    public PublicKeysControllerImpl(PublicKeysService publicKeysService) {
        this.publicKeysService = publicKeysService;
    }


    @GetMapping("v1/keys")
    public ResponseEntity<ActiveKeysetsResponse> getActiveKeys() throws Exception {
        List<KeysetKeys> keys = publicKeysService.getActiveKeysets();
        return new ResponseEntity<>(new ActiveKeysetsResponse(keys), HttpStatusCode.valueOf(200));
    }


    //    @GetMapping("v1/keys")
//    public KeysResponse getKeysById(String keysetId) throws Exception {
//        Keys keys = publicKeysService.getKeysetById(keysetId);
//        return new KeysResponse(keys.getKeysetStringMap());
//    }


    @GetMapping("v1/keysets")
    public ResponseEntity<KeysetsResponse> getKeysets() throws Exception {
        return new ResponseEntity<>(new KeysetsResponse(publicKeysService.getAllKeysets()), HttpStatusCode.valueOf(200));
    }
}
