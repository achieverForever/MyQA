package search;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import content.Query;
import content.Result;

public class ThreadingSearcher {
	
	private static final String BASE_URL = "https://api.datamarket.azure.com/Bing/SearchWeb/Web";
	private static final String ACCOUNT_KEY = "1KffzUvuIa+dAoa4p41r3Wx+sXog6Aa83sx86NBX6jM";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114";
	private static final int MAX_SEARCH_RESULTS = 5;	// 每次搜索只返回5个结果
	private static String ACCOUNT_KEY_ENC;
	
	private ConcurrentMap<Long, Result> mSharedMap;
	private Set<String> mUrlSet;
	private CloseableHttpClient mHttpClient;

	public ThreadingSearcher() {
		// 用于存放各WorkerThread获取回来的Result
		mSharedMap = new ConcurrentHashMap<Long, Result>();
		// 避免重复获取相同的URL
		mUrlSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		PoolingHttpClientConnectionManager connMgr =
				new PoolingHttpClientConnectionManager();
		connMgr.setMaxTotal(200);
		connMgr.setDefaultMaxPerRoute(20);
		
		mHttpClient = HttpClients.custom()
				.setConnectionManager(connMgr)
				.setRetryHandler(retryHandler)
				.build();
	}
	
	/*
	 * 启动queries.size()个线程进行搜索，搜索结果存放在mSharedMap中
	 */
	public void search(List<Query> queries) {
		List<WorkerThread> threads = new ArrayList<WorkerThread>();
		for (Query q : queries) {
			threads.add(new WorkerThread(q, mSharedMap, mHttpClient));
		}
		for (WorkerThread t : threads) {
			t.start();
		}
		for (WorkerThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 每一次ThreadingSearcher使用完以后记得释放资源!
	 */
    public void cleanup() {
    	try {
			mHttpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public List<Result> getResult()	{
    	List<Result> res = new ArrayList<Result>();
    	for (Long k : mSharedMap.keySet()) {
    		res.add(mSharedMap.get(k));
    	}
    	return res;
    }

	static {
		byte[] accountKeyBytes = 
        		Base64.encodeBase64((ACCOUNT_KEY + ":" + ACCOUNT_KEY).getBytes());
		ACCOUNT_KEY_ENC = new String(accountKeyBytes);
	}
	
	private HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
		@Override
		public boolean retryRequest(IOException exception, int executionCount, 
				HttpContext ctx) {
			if (executionCount > 2) {
				System.err.println("Maximum retries exceeded");
				return false;
			} else if (exception instanceof NoHttpResponseException) {
				System.err.println("NoHttpResponseException, try again.");
				return true;
			} else if (exception instanceof SocketTimeoutException) {
				System.err.println("SocketTimeoutException, try again.");
				return true;
			} else if (exception instanceof HttpHostConnectException) {
				System.err.println("HttpHostConnectionException, try again.");
				return true;
			} else {
				return false;
			}
		}
	};
	
	private ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		@Override
		public String handleResponse(HttpResponse response)
				throws HttpResponseException, IOException {
			StatusLine statusLine = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(),
						statusLine.getReasonPhrase());
			} else if (entity == null) {
				return null;
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
					if (numRead < 0) {
						break;
					}
					Matcher m =  Pattern.compile("charset=([\\w-\"]+)").matcher(
							new String(buffer, 0, numRead, "UTF-8"));
					if (m.find()) {
						charset = m.group(1).replaceAll("\"", "").toUpperCase();
						break;
					}
				}
				// Reset stream so that we can read again 
				resetStream(is);
					
//				System.out.println("Charset: " + charset);
				if (charset.equals("")) {
					System.err.println("No charset is found, default will be used");
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
	
	
	/*
	 * 向Bing发起搜索，解析HTML，获取文档的工作线程
	 */
	private class WorkerThread extends Thread {
		
		private volatile ConcurrentMap<Long, Result> mSharedMap;
		private Query mQuery;
		private CloseableHttpClient mHttpClient;
		
		public WorkerThread(Query q, ConcurrentMap<Long, Result> sharedmap,
				CloseableHttpClient httpclient) {
			mQuery = q;
			mSharedMap = sharedmap;
			mHttpClient = httpclient;
		}
		
		@Override
		public void run() {
			System.out.println("Thread #" + Thread.currentThread().getId() + " started");
			
			List<String> urls = searchBing(MAX_SEARCH_RESULTS);
			if (urls == null) {
				return;
			}
			
			for (Iterator<String> it = urls.iterator(); it.hasNext(); ) {
				String url = it.next();
				if (mUrlSet.contains(url)) {
					it.remove();
				} else {
					mUrlSet.add(url);
				}
			}
			
			List<String> snippets = fetchSnippets(urls);
			
			Map<Long, Result> res = new HashMap<Long, Result>();
			for (int i = 0; i < urls.size(); i++) {
				if ((snippets.get(i) != null) && (!snippets.get(i).equals(""))) {
					res.put(System.currentTimeMillis(),
							new Result(snippets.get(i), urls.get(i), i + 1, mQuery));
				}
			}
			mSharedMap.putAll(res);
		}
		
		/*
		 * 将查询串发往Bing搜索引擎，返回topN个URL
		 */
		private List<String> searchBing(int topN) {
			List<String> res = null;
	    	try {
				HttpGet get = new HttpGet(new URIBuilder(BASE_URL)
						.setParameter("Query", "'" + mQuery.getQueryString() + "'")
						.setParameter("$top", topN + "")
						.setParameter("$format", "Json")
						.setParameter("Market", "'" + "zh-CN" + "'")
						.build());
		        get.setHeader("Authorization", "Basic " + ACCOUNT_KEY_ENC);
		        
		        System.out.println(get.getRequestLine());

		        String responseBody = mHttpClient.execute(get, responseHandler);
		        if (responseBody == null) {
		        	return null;
		        }

		        JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(responseBody);
				
				res = fromJson(json);
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("************ IOException in searchBing() ***********");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    	return res;
		}
		
		/*
		 * 从JSONObject中解析出URL
		 */
	    private List<String> fromJson(JSONObject json) {
	    	List<String> urls = new ArrayList<String>();
	    	JSONObject d = (JSONObject) json.get("d");
	    	JSONArray results = (JSONArray) d.get("results");
	    	for (Object r : results) {
	    		urls.add((String)((JSONObject)r).get("Url"));
	    	}
	    	return urls;
	    }
	    
	    /*
	     * 获取urls所指向的网页，并解析出该网页的文本作为文档返回
	     */
	    private List<String> fetchSnippets(List<String> urls) {
	    	List<String> res = new ArrayList<String>(urls.size());
	    	for (int j = 0; j < urls.size(); j++) {
	    		res.add(null);
	    	}
	    	int i = 0;
	    	for (String url : urls) {
	    		System.out.println("Connecting with " + url);
	    		
	    		HttpGet get = new HttpGet(url);
	    		get.setHeader("User-Agent", USER_AGENT);

	    		try {
					String html = mHttpClient.execute(get, responseHandler);
					
					if (html == null) {
						continue;
					}
					String content = "";
					
					Document doc = Jsoup.parse(html);
					Elements paras = doc.select("p");
//					Elements divs = doc.select("div");
					StringBuilder sb = new StringBuilder();
					Pattern pattern = Pattern.compile("[。！；]+");
					for (Element p : paras) {
						if (pattern.matcher(p.text()).find()) {
							sb.append(p.text());
						}
					}
/*					for (Element div : divs) {
						if (pattern.matcher(div.text()).find()) {
							sb.append(div.text());
						}
					}*/
					content = sb.toString();
					
/*					System.out.println("URL: " + url);
					System.out.println(content);*/
					res.set(i++, content);
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch(UnknownHostException e) {
					System.err.println("Failed to resolve host");
					continue;
				} catch (IOException e) {
					System.err.println("********** IOException in fetchSnippets() **********");
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("********** UnknownException in fetchSnippets() ******");
					e.printStackTrace();
				}
	    	}
	    	return res;
	    }
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
