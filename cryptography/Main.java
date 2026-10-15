import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Main {

    private static final int KEY_BITS = 256;
    private static final int IV_BYTES = 12;     
    private static final int TAG_BITS = 128;

    public static SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(KEY_BITS, new SecureRandom());
        return kg.generateKey();
    }

    public static String encrypt(String plainText, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + шифртекст (с тегом аутентификации) в одном массиве
        ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
        buf.put(iv).put(cipherText);
        return Base64.getEncoder().encodeToString(buf.array());
    }

    public static String decrypt(String base64, SecretKey key) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64);
        ByteBuffer buf = ByteBuffer.wrap(data);

        byte[] iv = new byte[IV_BYTES];
        buf.get(iv);
        byte[] cipherText = new byte[buf.remaining()];
        buf.get(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        SecretKey key = generateKey();
        String encrypted = encrypt("Привет, AES-256!", key);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypt(encrypted, key));
    }
}
