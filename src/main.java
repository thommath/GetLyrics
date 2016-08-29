import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import org.apache.commons.lang3.StringEscapeUtils;

public class main {
	public static void main(String[] args) throws FileNotFoundException{
		boolean fromFile = false;
		
		if(!fromFile){
			Scanner in = new Scanner(new File("songs.txt"));
			
			while(in.hasNextLine()){
				try{
					String url = in.nextLine();
				
					if(!addSong(url, false)){
						String[] song = getNameAndAuthor(url);
						PrintWriter writer = new PrintWriter(new FileOutputStream("failed.txt", true));
						writer.append(song[0] + ";" + song[1] + "\n");
						writer.close();
						System.out.println(song[0] + " finnes ikke");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}else{
			Scanner in = new Scanner(new File("failed.txt"));
			
			while(in.hasNextLine()){
				try{
					String url = in.nextLine();
				
					if(!addSong(url, false)){
						PrintWriter writer = new PrintWriter(new FileOutputStream("failed2.txt", true));
						writer.append(url + "\n");
						writer.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public static boolean addSong(String inp, boolean retry) throws Exception{
		String url = inp;
		
		String name, author;
		
		String[] song;
		
		if(inp.startsWith("http")){
			song = getNameAndAuthor(url);
		}else{
			song = inp.split(";");
		}

		name = song[0];
		author = song[1];
		String src = "";
		int index = 0;
		int end = 0;
		
		//Sok hos musixmatch
		if(!retry){
			url = "http://www.mldb.org/search?mq=" + name.replace(" ", "+") + "+" + author.replace(" ", "+") + "&si=0&mm=0&ob=1";
		}else{
			url = "http://www.mldb.org/search?mq=" + name.replace(" ", "+") + "&si=0&mm=0&ob=1";
		}
		src = load(url);

		try{
		index = src.indexOf("song-");
		end = src.indexOf("\"", index);
		
		
		//Sangens side musixmatch
		url = "http://www.mldb.org/" + StringEscapeUtils.unescapeHtml4(src.substring(index, end));
		src = load(url);
		}catch(Exception e){
			
		}
		try{
		index = src.indexOf("<p class=\"songtext\" lang=\"EN\">") + 30;
		end = src.indexOf("</p>", index);
		
		
		String lyrics = StringEscapeUtils.unescapeHtml4(src.substring(index, end)).replace('"', ' ').replace("<br />", "\n");
		
			
		System.out.println("\n" + name + "\n");
		PrintWriter writer;
		if(retry){
			writer = new PrintWriter(author + " - " + name + " usikker.txt", "UTF-8");			
		}else{
			writer = new PrintWriter(author + " - " + name + ".txt", "UTF-8");						
		}
		writer.println(lyrics);
		writer.close();
		
		return true;
		
		}catch(Exception e){
			if(retry){
				return false;
			}else if(addSong(inp, true)){
				return true;
			}else{
				return false;
			}
		}
	}
	
	public static String[] getNameAndAuthor(String url) throws Exception{
		String src = load(url);
		
		int index = src.indexOf("track-name\">");
		int end = src.indexOf("</span>", index);
		
		String name = StringEscapeUtils.unescapeHtml4(src.substring(index+12, end));
		
		index = src.indexOf("creator-name\">");
		end = src.indexOf("</span>", index);

		String author = StringEscapeUtils.unescapeHtml4(src.substring(index+14, end));
		
		return new String[]{name, author};
	}

	public static String load(String inp) throws IOException{
		URL url = new URL(inp);
		
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("89.108.77.131", 80));
		
		URLConnection spoof = url.openConnection();

		//Spoof the connection so we look like a web browser
		spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0;    H010818)" );
		BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
		String strLine = "";
		String finalHTML = "";
		//Loop through every line in the source
		while ((strLine = in.readLine()) != null){
		   finalHTML += strLine;
		}
		return finalHTML;
	}
}
