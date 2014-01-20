import java.io.File;

public class MainClass
{

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		final String awsAccessKeyId = "ADFASFASWERWERASFSD";
		final String secretKey = "WERWEREWQSASEWWRQWERCADSDSDSDSDSD";
		final String bucket = "myTestBucket";
		final String key = "/test/log2.txt";
		final String localFilePath = "/Users/abc/Desktop/log2.txt";

		final File file = new File(localFilePath);
		if ( file.exists() )
		{
			final Multipart mp = new Multipart(key, bucket, awsAccessKeyId, secretKey, localFilePath);
			mp.upload();
		}
	}
}
