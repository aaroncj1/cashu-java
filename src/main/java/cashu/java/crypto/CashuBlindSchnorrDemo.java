//package cashu.java.crypto;
//
///*====================================================================*
// *  3️⃣  Demo – a complete end‑to‑end run (Mint ↔ Wallet)                *
// *====================================================================*/
//public class CashuBlindSchnorrDemo {
//
//    private static String bytesToHex(byte[] b) {
//        StringBuilder sb = new StringBuilder(b.length * 2);
//        for (byte v : b) sb.append(String.format("%02x", v));
//        return sb.toString();
//    }
//
//    public static void main(String[] args) {
//        System.out.println("\n=== Cashu Blind‑Schnorr Demo (secp256k1) ===\n");
//
//        /* --------- 1️⃣  Create the Mint (three denominations) ---------- */
//        Mint mint = new Mint("USD-1", "USD-5", "USD-10");
//        System.out.println("[Mint] public keys published:");
//        mint.getPublicKeys().forEach((id, pk) ->
//                System.out.println("   " + id + " → " + bytesToHex(pk.normalize().getEncoded(false))));
//        System.out.println();
//
//        /* --------- 2️⃣  Create the Wallet -------------------------------- */
//        Wallet wallet = new Wallet(mint);
//
//        /* --------- 3️⃣  Request a token (e.g. a 5‑cent token) ------------ */
//        String keysetId = "USD-5";
//        byte[] tokenInfo = "5-cent-token".getBytes();   // in real Cashu this would include a random nonce
//        CashuToken token = wallet.requestToken(keysetId, tokenInfo);
//
//        System.out.println("[Wallet] obtained token:");
//        System.out.println("   " + token);
//        System.out.println();
//
//        /* --------- 4️⃣  Local verification (optional) -------------------- */
//        boolean ok = wallet.redeem(token, tokenInfo);
//        System.out.println("[Wallet] local verification of the token: " + (ok ? "VALID" : "INVALID"));
//        System.out.println();
//
//        /* --------- 5️⃣  Simulate a redemption at the Mint --------------- */
//        // In a real deployment the wallet would POST (token, tokenInfo) to the mint.
//        // The mint runs exactly the same verification we just performed.
//        boolean redeemOk = wallet.redeem(token, tokenInfo);
//        System.out.println("[Mint] token redemption result: " + (redeemOk ? "ACCEPTED" : "REJECTED"));
//    }
//}
