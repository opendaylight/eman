package eman.httptest;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class EmanHttpTests {

	private static final Logger LOGGER = Logger.getLogger(EmanHttpTests.class.getName());
	protected static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";

	protected static final String USER = "admin";
	protected static final String PASSWORD = "admin";

	protected static final String HTTP_GET = "GET";
	protected static final String HTTP_POST = "POST";
	protected static final String HTTP_PUT = "PUT";

	public void EmanHttpGet(String authString) {
		String targetUrl = "http://localhost:8181/restconf/config/odl-eman-battery:batteryObjects/";
		String bodyString = "{\"eman:input\": { \"networkid\":\"123\"}}";
		CloseableHttpClient httpClient = HttpClients.createDefault();

		System.out.println("HTTP GET call to: " + targetUrl);

		try {
			HttpGet httpGet = new HttpGet(targetUrl);
			httpGet.addHeader("Content-Type", "application/yang.data+json");
			httpGet.addHeader("Authorization", "Basic " + authString);

            StringEntity inputBody = new StringEntity(bodyString);
            inputBody.setContentType(CONTENTTYPE_JSON);

			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity responseEntity = httpResponse.getEntity();
			System.out.println("Response Status: " + httpResponse.getStatusLine().toString());
			System.out.println("Response: " + responseEntity.toString());
		} catch (Exception ex) {
			LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
			}
		}
	}

	public void EmanHttpPut(String authString) {
		String targetUrl = "http://localhost:8181/restconf/config/odl-eman-battery:batteryObjects/";
		String bodyString = "{batteryObjects: {batteryEntry:[]}}";
		CloseableHttpClient httpClient = HttpClients.createDefault();

		System.out.println("HTTP PUT call to: " + targetUrl);

		try {
			HttpPut httpPut = new HttpPut(targetUrl);
			httpPut.addHeader("Content-Type", "application/yang.data+json");
			httpPut.addHeader("Authorization", "Basic " + authString);

            StringEntity inputBody = new StringEntity(bodyString);
            inputBody.setContentType(CONTENTTYPE_JSON);
            httpPut.setEntity(inputBody);

			CloseableHttpResponse httpResponse = httpClient.execute(httpPut);
			HttpEntity responseEntity = httpResponse.getEntity();
			System.out.println("Response Status: " + httpResponse.getStatusLine().toString());
			System.out.println("Response: " + responseEntity.toString());
		} catch (Exception ex) {
			LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
			}
		}
	}


	public void EmanHttpPost(String authString) {
		String targetUrl = "http://localhost:8181/restconf/operations/eman:change-power";
		String bodyString = "{\"eman:input\": { \"targetState\":\"high\"}}";
		CloseableHttpClient httpClient = HttpClients.createDefault();

		System.out.println("HTTP POST call to: " + targetUrl);

		try {
			HttpPost httpPost = new HttpPost(targetUrl);
			httpPost.addHeader("Content-Type", "application/yang.data+json");
			httpPost.addHeader("Authorization", "Basic " + authString);

            StringEntity inputBody = new StringEntity(bodyString);
            inputBody.setContentType(CONTENTTYPE_JSON);
            httpPost.setEntity(inputBody);

			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity responseEntity = httpResponse.getEntity();
			System.out.println("Response Status: " + httpResponse.getStatusLine().toString());
			System.out.println("Response: " + responseEntity.toString());
		} catch (Exception ex) {
			LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				LOGGER.log(Level.INFO, "Error: " + ex.getMessage(), ex);
			}
		}
	}



	public static void main(String[] args) {

        // Create authentication string and encode it to Base64
        String authStr = USER + ":" + PASSWORD;
        String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());

		// Create Options
		Options options = new Options();
		options.addOption("id", false, "Obj ID for an HTTP GET test");

		// Create a parser
	    CommandLineParser parser = new DefaultParser();

        EmanHttpTests test = new EmanHttpTests();

	    try {
	        // Parse our command line arguments
	        CommandLine line = parser.parse( options, args );

	        if (line != null && line.getArgs() != null) {
	        	String arg0 = args[0];
	        	if (HTTP_POST.equalsIgnoreCase(arg0)) {
	                test.EmanHttpPost(encodedAuthStr);
	        	} else if (HTTP_PUT.equalsIgnoreCase(arg0)) {
	        	    test.EmanHttpPut(encodedAuthStr);
	        	} else if (HTTP_GET.equalsIgnoreCase(arg0)) {
	        	    test.EmanHttpGet(encodedAuthStr);
	        	} else {
	        		// Error message
	        	}
	        } else {
	        	// Error message
	        }

	    } catch (ParseException exp) {
	    	LOGGER.log(Level.WARNING, "Parsing failed.  Reason: " + exp.getMessage());
	    }


	}
}
