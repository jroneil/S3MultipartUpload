import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SignData
{
	/**
	 * Computes RFC 2104-compliant HMAC signature. * @param data The data to be signed.
	 * 
	 * @param key The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws java.security.SignatureException when signature generation fails
	 */
	public static String sign(final String data, final String key) throws SignatureException

	{
		String result;
		try
		{
			final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
			// get an hmac_sha1 key from the raw key bytes
			final SecretKeySpec signingKey1 = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			final Mac mac1 = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac1.init(signingKey1);

			// compute the hmac on input data bytes
			final byte[] rawHmac = mac1.doFinal(data.getBytes());

			// base64-encode the hmac
			result = Base64.encodeBase64String(rawHmac);
			result = result.trim();

		}
		catch ( final Exception e )
		{
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}

}
