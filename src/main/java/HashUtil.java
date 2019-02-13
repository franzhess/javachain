import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class HashUtil {
    public static String calculateSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte hash_byte : hash) {
                String hex = Integer.toHexString(0xff & hash_byte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringFromKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static byte[] calculateECDSASignature(PrivateKey privateKey, String input) {
        byte[] output;

        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(input.getBytes());
            output = dsa.sign();
        } catch (InvalidKeyException | SignatureException | NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not create signature", e);
        }

        return output;
    }

    public static boolean verifyECDSASignature(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaSignature = Signature.getInstance("ECDSA", "BC");
            ecdsaSignature.initVerify(publicKey);
            ecdsaSignature.update(data.getBytes());
            return ecdsaSignature.verify(signature);
        } catch (InvalidKeyException | NoSuchProviderException | SignatureException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to verify signature", e);
        }
    }

    //not the actual merkle root, but good enough
    public static String calculateMerkleRoot(ArrayList<Transaction> transactions) {
        StringBuilder concat = new StringBuilder();
        for (Transaction transaction : transactions) {
            concat.append(transaction.getId());
        }
        return calculateSHA256(concat.toString());
    }
}
