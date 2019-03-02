package tech.geekcity.open.geek.tools.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.inferred.freebuilder.FreeBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Properties;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = SecretEncryption.Builder.class)
public abstract class SecretEncryption implements Serializable, Closeable {
    private static final String ENCRYPTION_TYPE = "AES";
    private static final String TRANSFORM = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_KEY_STR = "mQPygujXh0pWc7Cv";
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private transient SecretKeySpec secretKeySpec;
    private transient IvParameterSpec ivParameterSpec;
    private transient ByteBuffer inputBuffer;
    private transient ByteBuffer outputBuffer;

    public abstract int bufferSize();

    public abstract String key();

    public abstract Properties properties();

    /**
     * Returns a new {@link Builder} with the same property values as this SecretEncryption
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SecretEncryption} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SecretEncryption_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder() {
            bufferSize(DEFAULT_BUFFER_SIZE);
            key(DEFAULT_KEY_STR);
            properties(new Properties());
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public SecretEncryption parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SecretEncryption.class);
        }
    }

    public SecretEncryption setup() {
        final String key = key();
        final int bufferSize = bufferSize();
        secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ENCRYPTION_TYPE);
        ivParameterSpec = new IvParameterSpec((key.getBytes(StandardCharsets.UTF_8)));
        inputBuffer = ByteBuffer.allocateDirect(bufferSize);
        outputBuffer = ByteBuffer.allocateDirect(bufferSize);
        return this;
    }

    @Override
    public void close() {
        secretKeySpec = null;
        ivParameterSpec = null;
        inputBuffer = null;
        outputBuffer = null;
    }

    public String encrypt(String strToEncrypt) throws IOException {
        Preconditions.checkArgument(null != strToEncrypt,
                "strToEncrypt(%s) cannot be null",
                strToEncrypt);
        try (CryptoCipher encipher = Utils.getCipherInstance(TRANSFORM, properties())) {
            encipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            inputBuffer.clear();
            outputBuffer.clear();

            inputBuffer.put(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            inputBuffer.flip();
            final int numberOfBytesStoredAfterUpdate = encipher.update(inputBuffer, outputBuffer);
            final int numberOfBytesStoredAfterDoFinal = encipher.doFinal(inputBuffer, outputBuffer);
            byte[] encoded = new byte[numberOfBytesStoredAfterUpdate + numberOfBytesStoredAfterDoFinal];
            outputBuffer.flip();
            outputBuffer.duplicate().get(encoded);
            return Base64.encodeBase64String(encoded);
        } catch (IOException | InvalidKeyException | ShortBufferException | BadPaddingException
                | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new IOException(String.format("exception caught while encrypt string(%s)", strToEncrypt), e);
        }
    }

    public String decrypt(String strToDecrypt) throws IOException {
        Preconditions.checkArgument(null != strToDecrypt,
                "strToDecrypt(%s) cannot be null",
                strToDecrypt);
        try (CryptoCipher decipher = Utils.getCipherInstance(TRANSFORM, properties())) {
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            inputBuffer.clear();
            outputBuffer.clear();

            inputBuffer.put(Base64.decodeBase64(strToDecrypt));
            inputBuffer.flip();
            decipher.update(inputBuffer, outputBuffer);
            decipher.doFinal(inputBuffer, outputBuffer);
            outputBuffer.flip();
            return copy(outputBuffer);
        } catch (IOException | InvalidKeyException | ShortBufferException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IOException(String.format("exception caught while decrypt string(%s)", strToDecrypt), e);
        }
    }

    private static String copy(ByteBuffer buffer) {
        final ByteBuffer copy = buffer.duplicate();
        final byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
