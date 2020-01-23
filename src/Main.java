import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Main {
	
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		String filename = "";
		if (args.length == 1) {
			filename = args[0];
		} else {
			System.out.print("File location: ");
			filename = scan.nextLine();
		}
		
		File file = new File(filename);
		if (!file.exists()) {
			System.err.println("File '" + filename + "' does not exist.");
			scan.close();
			return;
		}
		
		Main main = new Main(file, scan);
		main.run();
		scan.close();
	}
	
	public void loadConfig() {
		String os = System.getProperty("os.name");
		String home = System.getProperty("user.home");
		File config;
		if (os.startsWith("Windows")) {
			config = new File(home + "\\.rssconfig");
		} else if (os.startsWith("Linux")) {
			config = new File(home + "/.rssconfig");
		} else {
			config = new File(home + "/.rssconfig");
		}
		
		System.out.println("OS: " + os);
		if (config.exists()) {
			try {
				FileReader fr = new FileReader(config);
				BufferedReader br = new BufferedReader(fr);
				String line;
				// format here should be like:
				/*
				 * host
				 * port
				 * usehttps
				 * filepath??
				 */
				if ((line = br.readLine()) != null) {
					cfg.setHost(line);
				}
				if ((line = br.readLine()) != null) {
					cfg.setPort(line);
				}
				if ((line = br.readLine()) != null) {
					cfg.setUseHttps(Boolean.valueOf(line));
					if (cfg.getUseHttps()) {
						try {
							SSLContext sc = SSLContext.getInstance("SSL");
						    sc.init(null, trustAllCerts, new java.security.SecureRandom());
						    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						} catch (KeyManagementException e) {
							e.printStackTrace();
						}
					}
				}
				/*if ((line = br.readLine()) != null) {
					file = new File(line);
				}*/
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			config.delete();
		} else {
			// Version 2
			if (os.startsWith("Windows")) {
				config = new File(home + "\\.rssconfig-v1");
			} else if (os.startsWith("Linux")) {
				config = new File(home + "/.rssconfig-v1");
			} else {
				config = new File(home + "/.rssconfig-v1");
			}
			
			if (config.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(config));
					cfg = (Configuration) ois.readObject();
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		printConfig();
	}
	
	private void printConfig() {
		System.out.println("config: { 'host': " + cfg.getHost() + ", 'port': " + cfg.getPort() + ", 'useHttps': " + cfg.getUseHttps() + " }");
	}
	
	private void saveConfig() {
		String os = System.getProperty("os.name");
		String home = System.getProperty("user.home");
		File config;
		if (os.startsWith("Windows")) {
			config = new File(home + "\\.rssconfig-v1");
		} else if (os.startsWith("Linux")) {
			config = new File(home + "/.rssconfig-v1");
		} else {
			config = new File(home + "/.rssconfig-v1");
		}
		
		System.out.println("path: " + config.getAbsolutePath());
		try {
			config.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(config));
			oos.writeObject(cfg);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Scanner scanner;
	private File file;
	static class Configuration implements Serializable {
		private static final long serialVersionUID = -1980730099965894892L;

		static class Information implements Serializable, Cloneable {
			private static final long serialVersionUID = 5616226641315445439L;
			String host = "www.google.com";
			String port = "443";
			boolean useHttps = true;
			@Override
			public Object clone() throws CloneNotSupportedException {
				return super.clone();
			}
		}
		Information i = new Information();
		HashMap<String, Information> m = new HashMap<String, Information>();
		String getHost() {
			return i.host;
		}
		
		void setHost(String s) {
			i.host = s;
		}
		
		String getPort() {
			return i.port;
		}
		
		void setPort(String s) {
			i.port = s;
		}
		
		boolean getUseHttps() {
			return i.useHttps;
		}
		
		void setUseHttps(boolean b) {
			i.useHttps = b;
		}
		
		void setConfiguration(String name) {
			try {
				m.put(name, (Information) i.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		void loadConfiguration(String name) {
			Information a = m.get(name);
			i = a;
		}
	}
	private Configuration cfg = new Configuration();
	public Main(File file, Scanner scan) {
		scanner = scan;
		this.file = file;
		loadConfig();
	}
	
	public void loadRequest(String uri) {
		String method = null;
		List<String> headers = new ArrayList<String>();
		List<String> body = new ArrayList<String>();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			if ((line = br.readLine()) != null) {
				method = line;
				// format here should be like:
				/*
				 * POST
				 * 
				 * Header: value
				 * Header: value
				 * 
				 * { 'body': 'value' }
				 */
				br.readLine();
			}
			// Read header
			while ((line = br.readLine()) != null) {
				if (line.equals("")) break;
				headers.add(line);
			}
			while ((line = br.readLine()) != null) {
				if (line.equals("")) break;
				body.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		loadRequest(uri, method, headers, body);
	}
	
	// Create a trust manager that does not validate certificate chains
	private TrustManager[] trustAllCerts = new TrustManager[]{
	    new X509TrustManager() {
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	        public void checkClientTrusted(
	            java.security.cert.X509Certificate[] certs, String authType) {
	        }
	        public void checkServerTrusted(
	            java.security.cert.X509Certificate[] certs, String authType) {
	        }
	    }
	};
	
	private void loadRequest(String uri, String method, List<String> headers, List<String> body) {
		try {
			String pre = cfg.getUseHttps() ? "https://" : "http://";
			String urlStr = pre + cfg.getHost() + ":" + cfg.getPort() + uri;
			System.out.println("urlStr: " + urlStr);
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(0);
			makeRequest(conn, method, headers, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeRequest(HttpURLConnection conn, String method, List<String> headers, List<String> body) throws Exception {
		conn.setRequestMethod(method);
		System.out.println("-----[REQUEST HEADERS]-----");
		for (String header : headers) {
			String[] h = header.split(":");
			if (h.length != 2) continue;
			conn.setRequestProperty(h[0], h[1]);
			System.out.println(h[0] + ": " + h[1]);
		}
		System.out.println("-----[REQUEST HEADERS]-----");
		
		if (body.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String b : body) {
				sb.append(b);
			}
			
			conn.setDoOutput(true);
			byte[] outputInBytes = sb.toString().getBytes("UTF-8");
			OutputStream os = conn.getOutputStream();
			os.write(outputInBytes);    
			os.close();
		}
		
		// Empty line between request and response
		System.out.println("Making request...");
		
		int statusCode = conn.getResponseCode();
		System.out.println("Status code: " + statusCode);
		System.out.println("-----[RESPONSE HEADERS]-----");
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().get(0));
		}
		System.out.println("-----[RESPONSE HEADERS]-----");
		
		// Body object included
		if (conn.getHeaderFieldInt("Content-Length", -1) > 0 && (statusCode == 200)) {
			InputStream is = conn.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    System.out.println(response);
		}
	}
		
	private void printHelp() {
		// Print possible commands
		System.out.println("change [host|port] (value)  - change hostname or port number");
		System.out.println("                              ex.: change host www.google.com");
		System.out.println("request (uri)               - Makes request with host and port number (based on body and headers)");
		System.out.println("                              ex.: request /index.html");
		System.out.println("protocol (value)            - change protcol used between https and http");
		System.out.println("                              ex.: protocol https");
		System.out.println("save (environment name)     - Save environment to shortname.");
		System.out.println("switch (environment name)   - Switch to environment with shortname.");
		System.out.println("                              ex.: switch test");
		System.out.println("replay                      - Recalls last command.");
		System.out.println("exit                        - Exits application, does not write to file.");
	}
	
	private String previousCommand = null;
	public void run() {
		printHelp();
		
		String command;
		while (true) {
			command = scanner.nextLine();
			if (processCommand(command) == false) {
				System.out.println("Exiting...");
				break;
			}
			// Prevents infinite looping
			if (!command.startsWith("replay")) {
				previousCommand = command;
			}
		}
		saveConfig();
	}
	
	private boolean processCommand(String command) {
		if (command.startsWith("request")) {
			String[] cmd = command.split(" ");
			if (cmd.length != 2) {
				System.out.println("Command format wrong, should be: request (uri)");
				return true;
			}
			loadRequest(cmd[1]);
			return true;
		} else if (command.startsWith("change")) {
			String[] cmd = command.split(" ");
			if (cmd.length != 3) {
				System.out.println("Command format wrong, should be: change [host|port] (value)");
				return true;
			}
			// TODO: save to some preference file
			if (cmd[1].equals("host")) {
				cfg.setHost(cmd[2]);
				printConfig();
				return true;
			} else if (cmd[1].equals("port")) {
				cfg.setPort(cmd[2]);
				printConfig();
				return true;
			}
		} else if (command.startsWith("protocol" )) {
			String[] cmd = command.split(" ");
			if (cmd.length != 2) {
				System.out.println("Command format wrong, should be: protocol (value)");
				return true;
			}
			if (cmd[1].equals("https")) {
				cfg.setUseHttps(true);
				try {
					SSLContext sc = SSLContext.getInstance("SSL");
				    sc.init(null, trustAllCerts, new java.security.SecureRandom());
				    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (KeyManagementException e) {
					e.printStackTrace();
				}
			} else {
				cfg.setUseHttps(false);
			}
			
			printConfig();
			return true;
		} else if (command.startsWith("exit")) {
			return false;
		} else if (command.startsWith("help")) {
			printHelp();
			return true;
		} else if (command.startsWith("save")) {
			int afterSpace = -1;
			for (int i = 0; i < command.length(); i++) {
				if (command.charAt(i) == ' ') {
					afterSpace = i;
					break;
				}
			}
			if (afterSpace == -1) return true;
			if (afterSpace + 1 == command.length()) return true;
			String name = command.substring(afterSpace);
			cfg.setConfiguration(name);
			return true;
		} else if (command.startsWith("switch")) {
			int afterSpace = -1;
			for (int i = 0; i < command.length(); i++) {
				if (command.charAt(i) == ' ') {
					afterSpace = i;
					break;
				}
			}
			if (afterSpace == -1) return true;
			if (afterSpace + 1 == command.length()) return true;
			String name = command.substring(afterSpace);
			cfg.loadConfiguration(name);
			printConfig();
			return true;
		} else if (command.startsWith("replay")) {
			if (previousCommand != null) return processCommand(previousCommand);
		}
		
		System.out.println("Command not recognized. enter help for list of commands");
		return true;
	}
}
