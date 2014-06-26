package main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import querygeneration.QueryGenerator;
import questionanalysis.KeywordExtractor;
import questionanalysis.QuestionAnalyzer;
import questionanalysis.QuestionClassifier;
import questionanalysis.SynonymExpander;
import search.ThreadingSearcher;
import answerextraction.AnswerExtractor;
import content.AnalyzedQuestion;
import content.Query;
import content.Result;
import edu.hit.ir.ltp4j.NER;
import edu.hit.ir.ltp4j.Postagger;
import edu.hit.ir.ltp4j.Segmentor;

public class QAMain {
	
	public static void main(String[] args) {
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			.format(Calendar.getInstance().getTime());
		System.out.println("------- Initializing " + now + " --------");
		
		if (!initializeAll()) {
			System.out.println("Failed to initialize MyQA");
			return;
		} else {
			System.out.println("Done.");
		}

		Scanner in = new Scanner(System.in);
		while (true) {
			String line = in.next();
			if (line.equals("exit")) {
				break;
			}
			
			ThreadingSearcher threadingSearcher = new ThreadingSearcher();
			
			// Question Analysis
			String question = line;
			AnalyzedQuestion analyzedQuestion = QuestionAnalyzer.analyze(question);
			System.out.println(analyzedQuestion);
			
			// Query Generation
			List<Query> queries = QueryGenerator.generateQueries(analyzedQuestion);
			System.out.println("Generated Queries: ");
			System.out.println(queries);
			
			// Search Bing
			List<Result> res;
			try {
				threadingSearcher.search(queries);
				res = threadingSearcher.getResult();
			} finally {
				threadingSearcher.cleanup();
			}
			
			// Answer Extraction
			List<Result> answers = AnswerExtractor.extractTopN(res, 5, 0.0);
			System.out.println("Final Result: ");
			int i = 1;
			for (Result r : answers) {
				System.out.println("[" + i++ + "]");
				System.out.println(r);
			}
			
			// Test fetchSnippets()
//			test(line);
			
			
		}
		cleanupAll();
	}
	
	public static boolean initializeAll() {
		System.out.println("Initializing Segmentor...");
		if (Segmentor.create("data/models/cws.model") < 0) {
			System.out.println("Failed to initialize Segmentor");
			return false;
		}
		System.out.println("Initializing PosTagger...");
		if (Postagger.create("data/models/pos.model") < 0) {
			System.out.println("Failed to initialize PosTagger");
			return false;
		}
		System.out.println("Initializing NERecognizer...");
		if (NER.create("data/models/ner.model") < 0) {
			System.out.println("Failed to initialize NERecognizer");
			return false;
		}
		System.out.println("Initializing KeywordExtractor...");
		if (!KeywordExtractor.initialize()) {
			System.out.println("Failed to initialize KeywordExtractor");
			return false;
		}
		System.out.println("Initializing SynonymExpander...");
		if (!SynonymExpander.initialize()) {
			System.out.println("Failed to initialize SynonymExpander");
			return false;
		}
		System.out.println("Initializing QuestionClassifier...");
		if (!QuestionClassifier.initialize()) {
			System.out.println("Failed to initialize QuestionClassifier");
			return false;
		}
		System.out.println("Initializing AnswerExtractor...");
		if (!AnswerExtractor.initialize()) {
			System.out.println("Failed to initialize AnswerExtractor");
			return false;
		}
		return true;
	}
	
	public static void cleanupAll() {
		Segmentor.release();
		Postagger.release();
		NER.release();
	}
	
	private static void test(String url) {
		
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, 
					HttpContext ctx) {
				if (executionCount > 3) {
					System.err.println("Maximum retries exceeded");
					return false;
				} else if (exception instanceof NoHttpResponseException) {
					System.err.println("NoHttpResponseException");
					return true;
				} else if (exception instanceof SocketTimeoutException) {
					System.err.println("SocketTimeoutException");
					return true;
				} else if (exception instanceof HttpHostConnectException) {
					System.err.println("HttpHostConnectionException");
					return true;
				} else {
					return false;
				}
			}
		};
		
		 CloseableHttpClient mHttpClient = HttpClients.custom()
				.setRetryHandler(retryHandler)
				.build();
		
		 List<String> urls = new ArrayList<String>();
		 urls.add("");
		 urls.set(0, url);
		 
		 List<String> res = fetchSnippets(mHttpClient, urls);
		 System.out.println(res);

		try {
			mHttpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<String> fetchSnippets(CloseableHttpClient httpclient, List<String> urls) {
    	List<String> res = new ArrayList<String>();
    	
  		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response)
					throws HttpResponseException, IOException {
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getReasonPhrase());
				} else if (entity == null) {
					throw new ClientProtocolException("Response does not contain content");
				}
				
				String charset = "";
				
				// Check if charset is specified in response header 
				Header contentType = entity.getContentType();
				if (contentType != null) {
					Matcher m = Pattern.compile("charset=([\\w-]+)").matcher(
							contentType.getValue());
					if (m.find()) {
						charset = m.group(1).toUpperCase();
					}
				}
				
				InputStream is = null;
				is = entity.getContent();
				is = prepareStream(is);
				
				// Charset not found in response header, continue to check if it is
				// specified in the entity
				if (charset.equals("")) {
					byte[] buffer = new byte[1024];
					
					for(;;) {
						int numRead = is.read(buffer);
						Matcher m =  Pattern.compile("charset=([\\w-\"]+)").matcher(
								new String(buffer, 0, numRead, "UTF-8"));
						if (m.find()) {
							charset = m.group(1).replaceAll("\"", "").toUpperCase();
							break;
						}
					}
					// Reset stream so that we can read again 
					resetStream(is);
						
					System.out.println("Charset: " + charset);
					if (charset.equals("")) {
						System.out.println("No charset is found, default will be used");
					} 						
				} 

				BufferedReader reader = null;
				String line = "";
				StringBuilder sb = new StringBuilder();
				if (charset.equals("")) {
					reader = new BufferedReader(new InputStreamReader(is));
				} else {
					reader = new BufferedReader(new InputStreamReader(is, charset));
				}
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				
				return sb.toString();
			}
  		};
		
		
    	for (String url : urls) {
    		System.out.println("Connecting with " + url);
    		HttpGet get = new HttpGet(url);
    		String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114";
    		get.setHeader("User-Agent", USER_AGENT);
    		
  
    		try {
				String html = httpclient.execute(get, responseHandler);
				if (html == null) {
					continue;
				}
				String content = "";
/*				int index = html.indexOf("<h2>Ä¿Â¼</h2>");
				if (index != -1) {
					content = html.substring(0, index);
				} else {
					content = html;
				}*/
				Document doc = Jsoup.parse(html);
				Elements paras = doc.select("p");
				Elements divs = doc.select("div");
				StringBuilder sb = new StringBuilder();
				Pattern pattern = Pattern.compile("[¡££¡£»]+");
				for (Element p : paras) {
					if (pattern.matcher(p.text()).find()) {
						sb.append(p.text());
					}
				}
				for (Element div : divs) {
					if (pattern.matcher(div.text()).find()) {
						sb.append(div.text());
					}
				}
				content = sb.toString();
				
/*				System.out.println("URL: " + url);
				System.out.println(content);*/
				if (!content.equals("")) {
					res.add(content);
				}
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("***************IOException in fetchSnippets()");
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("***************UnknownException in fetchSnippets()");
				e.printStackTrace();
			}
    	}
    	return res;
    }
	
	private static InputStream prepareStream(InputStream is) {
		BufferedInputStream buffered = new BufferedInputStream(is);
		buffered.mark(Integer.MAX_VALUE);
		return buffered;
	}
	
	private static void resetStream(InputStream is) throws IOException {
		is.reset();
		is.mark(Integer.MAX_VALUE);
	}
}
