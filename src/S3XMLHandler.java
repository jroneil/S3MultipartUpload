import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class S3XMLHandler extends DefaultHandler
{

	private final StringBuffer UploadId = new StringBuffer();
	private boolean append = false;


	@Override
	public void startElement(final String uri, final String ln, final String qn, final Attributes atts)
	{
		if ( qn.equalsIgnoreCase("UploadId") )
		{
			append = true;
		}
	}


	@Override
	public void endElement(final String url, final String ln, final String qn)
	{
		if ( qn.equalsIgnoreCase("UploadId") )
		{
			append = false;
		}
	}


	@Override
	public void characters(final char[] ch, final int s, final int length)
	{
		if ( append )
		{
			UploadId.append(new String(ch, s, length));
		}
	}


	public String getUploadId()
	{
		return UploadId.toString();
	}

}
