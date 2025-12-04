package io.github.aaroncj1.cashu.core.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import io.github.aaroncj1.cashu.core.model.serialization.v3.TokenV3;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4;
import io.github.aaroncj1.cashu.core.model.serialization.v4.ProofV4Group;
import io.github.aaroncj1.cashu.core.model.serialization.v4.TokenV4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class TokenCodec {
    private static final Base64.Encoder B64URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64URL_DECODER =
            Base64.getUrlDecoder();

    public static final String V3_PREFIX = "cashuA";
    public static final String V4_PREFIX = "cashuB";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static final ObjectMapper CBOR_MAPPER = new ObjectMapper(new CBORFactory().configure(CBORGenerator.Feature.LENIENT_UTF_ENCODING, true));

    static {
        // Allow unquoted field names etc. – defensive, not strictly needed.
        JSON_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private TokenCodec() {
        // static utility class, prevent instantiation
    }

    public static byte[] tokenV4ToCBOR(TokenV4 token) {
        CBORFactory cborFactory = new CBORFactory();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CBORGenerator gen = cborFactory.createGenerator(out)) {

            int fields = token.memo() == null ? 3 : 4;
            gen.writeStartObject(fields);
            gen.writeFieldName("t");
            int tokenSize = token.tokens().size();
            gen.writeStartArray(tokenSize);
            for (ProofV4Group pg : token.tokens()) {
                gen.writeStartObject(2);
                gen.writeBinaryField("i", pg.id());
                gen.writeFieldName("p");
                int proofSize = pg.proofs().size();
                gen.writeStartArray(proofSize);
                for (ProofV4 proof : pg.proofs()) {
                    int proofFields = 5;
                    proofFields = proof.witness() == null ? proofFields - 1 : proofFields;
                    proofFields = proof.dleqProof() == null ? proofFields - 1 : proofFields;
                    gen.writeStartObject(proofFields);
                    gen.writeObjectField("a", proof.amount());
                    gen.writeStringField("s", proof.secret());
                    gen.writeBinaryField("c", proof.C());
                    if (proof.dleqProof() != null) {
                        gen.writeFieldName("d");
                        gen.writeStartObject(3);
                        gen.writeBinaryField("e", proof.dleqProof().e()); //optional
                        gen.writeBinaryField("s", proof.dleqProof().s());
                        gen.writeBinaryField("r", proof.dleqProof().r());
                        gen.writeEndObject();
                    }
                    if (proof.witness() != null)
                        gen.writeStringField("w", proof.witness()); //optional
                    gen.writeEndObject();
                }
                gen.writeEndArray();
                gen.writeEndObject();
            }
            gen.writeEndArray();
            if (token.memo() != null)
                gen.writeStringField("d", token.memo()); //optional
            gen.writeStringField("m", token.mint());
            gen.writeStringField("u", token.unit());
            gen.flush();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeV4(TokenV4 token) throws IOException {
        byte[] cbor = tokenV4ToCBOR(token);

        String b64 = B64URL_ENCODER.encodeToString(cbor);
        return V4_PREFIX + b64;
    }

    public static TokenV4 deserializeV4(String cashuString) throws IOException {
        if (!cashuString.startsWith(V4_PREFIX)) {
            throw new IllegalArgumentException("Not a V4 token – missing '" + V4_PREFIX + "' prefix");
        }
        String b64 = cashuString.substring(V4_PREFIX.length());
        byte[] cbor = B64URL_DECODER.decode(b64);
        return CBOR_MAPPER.readValue(cbor, TokenV4.class);
    }

    public static String serializeV3(TokenV3 token) throws IOException {
        String json = JSON_MAPPER.writeValueAsString(token);

        String b64 = B64URL_ENCODER.encodeToString(json.getBytes());
        return V3_PREFIX + b64;
    }


    public static TokenV3 deserializeV3(String cashuString) throws IOException {
        if (!cashuString.startsWith(V3_PREFIX)) {
            throw new IllegalArgumentException("Not a V3 token – missing '" + V3_PREFIX + "' prefix");
        }
        String b64 = cashuString.substring(V3_PREFIX.length());
        byte[] jsonBytes = B64URL_DECODER.decode(b64);
        return JSON_MAPPER.readValue(jsonBytes, TokenV3.class);
    }
}
