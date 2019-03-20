package com.amazonaws.samples;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

public class MainClass
{

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		final String awsAccessKeyId = "";
		final String secretKey = "";
		final String bucket = "";
		final String key = "/test2/Wildlife.wmv";
		final String localFilePath = "/tmp/Wildlife.wmv";
		final String tempDirPath="/tmpconv/";

		final File file = new File(localFilePath);
		if ( file.exists() )
		{
			//Real life fis comes from another program and file name along with file size will be supplied
			FileInputStream fis = new FileInputStream(localFilePath);
			System.out.println("file.length()="+file.length());
			
			final Multipart mp = new Multipart(key,file.getName(),tempDirPath, bucket, awsAccessKeyId, secretKey,fis,file.length());
			mp.upload();
			
		}
	}
}
