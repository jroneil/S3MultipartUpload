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
			FileInputStream fis = new FileInputStream(localFilePath);
			System.out.println("file.length()="+file.length());
			
			final Multipart mp = new Multipart(key,file.getName(),tempDirPath, bucket, awsAccessKeyId, secretKey,fis,file.length());
			System.out.println("create temporary file");
			File tempFile=mp.InputStreamToFile(fis);
			System.out.println("temporary file done");
			System.out.println("Begin Upload");
			mp.upload();
			System.out.println("Upload done");
			System.out.println("start delete");
			boolean success = tempFile.delete();
			         if (success) {
			            System.out.println("The file has been successfully deleted"); 
			         }else{
			        	   System.out.println("The file delete failed"); 
			         }
		}
	}
}
