package tech.geekcity.open.geek.tools.common;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ben.wangz
 */
public class SecretEncryptionTest {
    @Test
    public void test() throws IOException {
        String randomString = RandomStringUtils.randomAlphanumeric(64);
        try (SecretEncryption secretEncryption = new SecretEncryption.Builder().build()) {
            String encodedString = secretEncryption.setup()
                    .encrypt(randomString);
            String decodedString = secretEncryption.decrypt(encodedString);
            Assert.assertNotEquals(randomString, encodedString);
            Assert.assertEquals(randomString, decodedString);
        }
    }
}
