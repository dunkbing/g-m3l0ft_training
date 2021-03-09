package APP_PACKAGE;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.ArrayList;
/**
 * Save remoteHTML to welcome.html Download remote resources Replace remote
 * resources references to local references
 * */
 public class HTMLResourceDownloader {
	private String fileName;
	private String url;
	private String localDirectory;
	private boolean anyProblem = true;
	public ArrayList<String> listaPNGS;

	public HTMLResourceDownloader(String url, String localDirectory,
			String fileName) {

		this.url = url;
		this.localDirectory = localDirectory;
		this.fileName = fileName;
		listaPNGS = new ArrayList<String>();
	}
	boolean readSomething = false;
		
	public void parseAndDownload() {
		URL myUrl;
		File fDirectory = new File(localDirectory);		
		fDirectory.mkdirs();
		try {
			if (fDirectory.isDirectory()) {
		        String[] children = fDirectory.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(fDirectory, children[i]).delete();
		        }
		    }

			PrintWriter out = new PrintWriter(new FileWriter(localDirectory+"/"+fileName));
			BufferedReader in = null;
			myUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
			int response = conn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK)
			{
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					inputLine=replaceForLocalResource(inputLine);
					out.println(inputLine);					
				}
			}
			else
			{
				DBG("SPLASH_SCREEN", "+++++++++++++++++ response code received: " + response);
				anyProblem = true;
			}
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if(readSomething)
				anyProblem = false;
		} catch (Exception e) {
			anyProblem = true;
			e.printStackTrace();
			DBG("SPLASH_SCREEN", "+++++++++++++++++ problem occured while downloading: " + e.getMessage());
		}
	
	}
	
	private String replaceForLocalResource(String inputLine) {
		if (!inputLine.contains("<img") || !inputLine.contains("usemap"))
		{
			return inputLine;
		}
		else
		{		
			int i,indexPNG = inputLine.indexOf("http");
			String remoteImgURL;
			if(indexPNG==-1)
			{
				return inputLine;
			}
			
			i = indexPNG;
			while (inputLine.charAt(i) != '"')
				i++;
			
			remoteImgURL = inputLine.substring(indexPNG, i);
			
			downloadFile(remoteImgURL,localDirectory+"/"+toLocal(remoteImgURL));
			return inputLine.replace(remoteImgURL,toLocal(remoteImgURL));
		}
	}


	public boolean anyProblem() {
		return anyProblem;
	}
	public String toLocal(String remote) {
		return remote.substring(remote.lastIndexOf('/') + 1);
	}

	public void downloadFile(String from, String to) {
		
		try {
			URL url = new URL(from);
			URLConnection urlConnection = url.openConnection();
			
			BufferedInputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(to));
			
			int i;
			while ((i = in.read()) != -1) {
				out.write(i);
			readSomething = true;
			}
			out.flush();
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			anyProblem = true;
			e.printStackTrace();

		}

	}

}




