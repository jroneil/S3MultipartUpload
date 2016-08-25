package com.amazonaws.samples;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Multipart {

	String bucket;
	String awsAccessKeyId;
	String secretKey;
	String key;
	InputStream inStream;
	String fileName;
	Long fileSize;
	String tempDirPath;
	public static int MEGABYTE = 1024 * 1024;
	private final int chunkSize = 5 * MEGABYTE;

	Multipart(final String key, String fileName,String tempDirPath,final String bucket, final String awsAccessKeyId, final String secretKey,
			final  InputStream inStream,final Long fileSize) {
		this.bucket = bucket;
		this.key = key;
		this.fileName=fileName;
		this.awsAccessKeyId = awsAccessKeyId;
		this.secretKey = secretKey;
		this.inStream = inStream;
		this.fileSize=fileSize;
		this.tempDirPath=tempDirPath;
	}

	/**
	 * Upload file to S3 server in multipal parts
	 * 
	 * @throws Exception
	 */
	public File upload(FileInputStream fis) throws Exception {
		File tempFile=InputStreamToFile(fis);
		final String uploadid = initateMultipartUpload();
		final Map<Integer, String> eTggMap = creatFileChunks(5, uploadid);
		completeNotify(eTggMap, uploadid);
		return tempFile;
	}

	/**
	 * Initiate multipart upload
	 * 
	 * @throws Exception
	 */
	private String initateMultipartUpload() throws Exception {

		final String date = getDate();
		final String data = getInitiateMultiPartSignString(date);

		final String signature = SignData.sign(data, secretKey);

		final PostMethod filePost1 = new PostMethod("http://" + bucket + "." + "s3.amazonaws.com");
		filePost1.setPath(key + "?uploads");
		filePost1.addRequestHeader("Authorization", "AWS " + awsAccessKeyId + ":" + signature);
		filePost1.addRequestHeader("Date", date);

		String uploadID = null;
		final HttpClient client1 = new HttpClient();
		try {
			final int status1 = client1.executeMethod(filePost1);
			uploadID = getUploadID(filePost1.getResponseBodyAsStream());
		} catch (final HttpException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		} finally {
			filePost1.releaseConnection();
		}
		System.out.println("Multipart upload initilization done, UploadID :" + uploadID);
		return uploadID;
	}

	private String getDate() {
		final String fmt = "EEE, dd MMM yyyy HH:mm:ss ";
		final SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String date = df.format(new Date()) + "GMT";
		return date;
	}

	/**
	 * parse response and get uploadID out of it
	 * 
	 * @param doc
	 * @return string uploadid
	 * @throws Exception
	 */
	private String getUploadID(final InputStream doc) throws Exception {
		String uploadID = null;
		final SAXParserFactory parserfactory = SAXParserFactory.newInstance();
		parserfactory.setNamespaceAware(false);
		parserfactory.setValidating(false);
		final SAXParser xmlparser = parserfactory.newSAXParser();
		final S3XMLHandler handler = new S3XMLHandler();
		xmlparser.parse(doc, handler);
		uploadID = handler.getUploadId();
		return uploadID;
	}


/**
 * Split file into chunks
 * @param workerCnt
 * @param uploadID
 * @param fileSize
 * @return
 * @throws SignatureException
 * @throws IOException
 */
	private Map<Integer, String> creatFileChunks(final int workerCnt, final String uploadID)
			throws SignatureException, IOException {
		final int ctr = 0;
		final Map<Integer, String> uptags = new HashMap<Integer, String>();
		final Long totalUploads = (fileSize / chunkSize) + 1;
		System.out.println("totalUploads"+totalUploads);
		// Apply ur threading logic here to upload each part independently.
		for (int i = 1; i <= totalUploads; i++) {
			final String etag = uploadPartToS3(uploadID, i);
			if (null != etag) {
				uptags.put(i, etag);
			} else {
				i--;
			}
		}
		return uptags;
	}

	
	public File InputStreamToFile( InputStream initialStream) 
			  throws IOException {
			  System.out.println("startCopy");
			    File targetFile = new File(tempDirPath+fileName);
			 
			    FileUtils.copyInputStreamToFile(initialStream, targetFile);
			    initialStream.close();
			   
			    System.out.println("copydone");
			    
			    return targetFile;
	}
	
	
	/**
	 * Upload part to s3 server
	 * 
	 * @param uploadId
	 *            upload id
	 * @param partNumber
	 *            part number
	 * @return eTag for the uploaded part
	 * @throws SignatureException
	 * @throws IOException
	 */
	@SuppressWarnings("null")
	private String uploadPartToS3(final String uploadId, final int partNumber) throws SignatureException, IOException {
		final int upLoadlimit = chunkSize;
		final String date = getDate();
		String upTag = null;
		final String data = getMultipartUploadSignString(date, partNumber, uploadId);
		// Encode data
		final String signature = SignData.sign(data, secretKey);

		// Setting header
		final PutMethod filePut = new PutMethod("http://" + bucket + "." + "s3.amazonaws.com");
		filePut.setPath(key + "?partNumber=" + String.valueOf(partNumber) + "&uploadId=" + uploadId);
		filePut.addRequestHeader("Authorization", "AWS " + awsAccessKeyId + ":" + signature);
		filePut.addRequestHeader("Date", date);

		final int startLoc = (partNumber - 1) * upLoadlimit;

		byte[] buffer = null;
		buffer = new byte[upLoadlimit];
		final RandomAccessFile randomAccess = new RandomAccessFile("/tmpconv/"+fileName, "r");
		randomAccess.seek(startLoc);
		final int dateRead = randomAccess.read(buffer, 0, upLoadlimit);

		// Need to set content length in header
		filePut.addRequestHeader("Content-Length", String.valueOf(dateRead));
		final ByteArrayInputStream bArryInStream = new ByteArrayInputStream(buffer, 0, dateRead);
		final InputStreamRequestEntity inRE = new InputStreamRequestEntity(bArryInStream);
		filePut.setRequestEntity(inRE);

		final HttpClient client1 = new HttpClient();
		try {
			final int status1 = client1.executeMethod(filePut);
			final String response = filePut.getResponseBodyAsString();
			final Header etag = filePut.getResponseHeader("ETag");
			if (null != etag) {
				upTag = etag.getValue();
			}
		} catch (final HttpException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		} finally {
			filePut.releaseConnection();
			randomAccess.close();
			bArryInStream.close();
		}
		System.out.println("Part : " + partNumber + " Tag " + upTag);
		return upTag;
	}

	/**
	 * Tell S3 server that done uploading all parts
	 * 
	 * @param eTggMap
	 *            map of part number vs etag
	 * @throws SignatureException
	 * @throws ParserConfigurationException
	 */
	private void completeNotify(final Map<Integer, String> eTggMap, final String uploadID)
			throws SignatureException, ParserConfigurationException {
		final String postData = BuildXMLData(eTggMap);
		final String date = getDate();
		final String data = getMultipartCompleteSignString(date, uploadID);

		// Encode data with secret key
		final String signature = SignData.sign(data, secretKey);

		// Set header
		final PostMethod filePost1 = new PostMethod("http://" + bucket + "." + "s3.amazonaws.com");
		filePost1.setPath(key + "?uploadId=" + uploadID);
		filePost1.addRequestHeader("Authorization", "AWS " + awsAccessKeyId + ":" + signature);
		filePost1.addRequestHeader("Date", date);
		filePost1.addRequestHeader("Content-Length", String.valueOf(postData.length()));

		filePost1.setRequestEntity(new ByteArrayRequestEntity(postData.getBytes()));
		final HttpClient client1 = new HttpClient();
		try {
			final int status1 = client1.executeMethod(filePost1);
			final String response = filePost1.getResponseBodyAsString();
			System.out.println("Status :" + status1 + " Response:" + response);
		} catch (final HttpException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		} finally {
			filePost1.releaseConnection();
		}

	}

	/**
	 * @param eTggMap
	 * @return
	 * @throws ParserConfigurationException
	 */
	private String BuildXMLData(final Map<Integer, String> eTggMap) throws ParserConfigurationException {
		try {
			// ///////////////////////////
			// Creating an empty XML Document

			// We need a Document
			final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();

			// //////////////////////
			// Creating the XML tree

			// create the root element and add it to the document
			final Element root = doc.createElement("CompleteMultipartUpload");
			doc.appendChild(root);

			final int len = eTggMap.size();
			for (int i = 1; i <= len; i++) {

				// create child element, add an attribute, and add to root
				final Element part = doc.createElement("Part");
				root.appendChild(part);

				final Element partNumber = doc.createElement("PartNumber");
				final Text text = doc.createTextNode(String.valueOf(i));
				partNumber.appendChild(text);
				part.appendChild(partNumber);

				final Element eTag = doc.createElement("ETag");
				final Text text1 = doc.createTextNode(eTggMap.get(i));
				eTag.appendChild(text1);
				part.appendChild(eTag);

			}
			// ///////////////
			// Output the XML

			// set up a transformer
			final TransformerFactory transfac = TransformerFactory.newInstance();
			final Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			final StringWriter sw = new StringWriter();
			final StreamResult result = new StreamResult(sw);
			final DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			final String xmlString = sw.toString();

			// print xml
			System.out.println("Here's the xml:\n\n" + xmlString);

			return xmlString;
		} catch (final Exception e) {
			System.out.println(e);
		}
		return null;
	}

	private String getInitiateMultiPartSignString(final String date) {

		final StringBuffer buf = new StringBuffer();
		buf.append("POST").append("\n");
		buf.append("\n");
		buf.append("\n");
		buf.append(date).append("\n");
		buf.append("/" + bucket + key + "?uploads");
		return buf.toString();
	}

	private String getMultipartUploadSignString(final String date, final int partnumber, final String uploadID) {
		final StringBuffer buf = new StringBuffer();
		buf.append("PUT").append("\n");
		buf.append("\n");
		buf.append("\n");
		buf.append(date).append("\n");
		buf.append("/" + bucket + key + "?partNumber=" + String.valueOf(partnumber) + "&uploadId=" + uploadID);
		return buf.toString();
	}

	private String getMultipartCompleteSignString(final String date, final String uploadID) {
		final StringBuffer buf = new StringBuffer();
		buf.append("POST").append("\n");
		buf.append("\n");
		buf.append("\n");
		buf.append(date).append("\n");
		buf.append("/" + bucket + key + "?uploadId=" + uploadID);
		return buf.toString();
	}
}
