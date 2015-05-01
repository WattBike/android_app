package nl.hva.wattbike.wattbike;

import gnu.crypto.Registry;
import gnu.crypto.hash.HashFactory;
import gnu.crypto.hash.IMessageDigest;
import gnu.crypto.util.Util;

/**
 * @author Sean Molenaar
 * @version 0.0.0.1
 * @since 1-5-15
 */
public class Hash {
    /**
     * @param pass  the password to hash
     * @param email the username to hash with
     * @return a hash
     */
    public static String hash(String pass, String email) {
        String hashedPass;
        IMessageDigest md = HashFactory.getInstance(Registry.WHIRLPOOL_HASH);
        String preSalt = "k4wQ|lh|jh~^ztYxy|HFHp_AEkYxXoDjoCu3pLF5wNred-yS0ggSrqf^9NC";
        String preHash = preSalt + pass;
        md.reset();
        md.update(preHash.getBytes(), 0, preHash.getBytes().length);
        byte[] digest = md.digest();
        for (int i = 0; i < 5; i++) {
            String str = Util.toString(digest).toLowerCase();
            str = preSalt + str + email;
            md.reset();
            md.update(str.getBytes(), 0, str.getBytes().length);
            digest = md.digest();
        }
        hashedPass = Util.toString(digest).toLowerCase();
        return hashedPass;
    }
}
