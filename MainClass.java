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
		final String awsAccessKeyId = "AKIAJ5XSHUIXYS23QZ2Q";
		final String secretKey = "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";
		final String bucket = "volpe-v";
		final String key = "/test2/Wildlife.wmv";
		final String localFilePath = "/tmp/Wildlife.wmv";
		final String tempDirPath="/tmpconv/";

		final File file = new File(localFilePath);
		if ( file.exists() )
		{
			//Real life fis comes from another program
			FileInputStream fis = new FileInputStream(localFilePath);
			System.out.println("file.length()="+file.length());
			
			final Multipart mp = new Multipart(key,file.getName(),tempDirPath, bucket, awsAccessKeyId, secretKey,fis,file.length());
			System.out.println("create temporary file");
			File tempFile=mp.upload(fis);
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
