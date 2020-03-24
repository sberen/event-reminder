package api;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NewsAPIGetter implements APIGetter<Article> {

    private String key;
    private String endpoint;
    public static final String API_URL = "http://newsapi.org";
    public static final int PAGE_SIZE = 5;
    public static final String DEFAULT_PARAMETER = "?q=coronavirus";

    // pre: given a valid authentication String and the desired endpoint,
    // post: constructs and returns a api.NewsAPIGetter that will be hooked up
    //      to said endpoint.
    public NewsAPIGetter(String key, String endpoint) {
        this.endpoint = endpoint;
        this.key = key;
    }

    public Article[] query(Map<String, String> parameters) throws Exception {
        String paramString = paramString(parameters);
        URL url = new URL(API_URL + endpoint + paramString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Api-Key", key);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(con.getInputStream());
        } catch (Exception e) {
            con.disconnect();
            throw new IllegalArgumentException("The parameters passed in were not valid");
        }
        BufferedReader in = new BufferedReader(reader);
        String json = readQuery(in);
        return getArticleArray(json);
    }

    private Article[] getArticleArray(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(jsonString);
        JSONArray arr = (JSONArray) obj.get("articles");
        int arrSize = Math.min(PAGE_SIZE, arr.size()); // just in case there are less than five entries
        Article[] result = new Article[arrSize];
        for (int i = 0; i < arrSize; i++) {
            JSONObject element = (JSONObject) arr.get(i);
            String author = (String) element.get("author");
            String url = (String) element.get("url");
            String title = (String) element.get("title");
            String description = (String) element.get("description");
            JSONObject sourceObject = (JSONObject) element.get("source");
            String source = (String) sourceObject.get("name");
            String dateTime = (String) element.get("publishedAt");
            //String date = dateTime.substring(0, 10); // dateTime format as "yyyy-mm-ddThh:mm:ssZ"
            //String time = dateTime.substring(11, dateTime.length() - 1);
            result[i] = new Article(title, description, url, author, source, dateTime);
        }
        return result;
    }

    public String paramString(Map<String, String> parameters) {
        if (parameters.size() == 0) {
            return DEFAULT_PARAMETER;
        }
        String params = "?";
        int i = parameters.keySet().size();
        for (String type : parameters.keySet()) {
            params += type + "=" + parameters.get(type);
            if (i != 1) { params += "&"; }
            i--;
        }
        return params;
    }

    public void changeKey(String newKey) { this.key = newKey; }

    public void setEndpoint(String newEnd) { this.endpoint = newEnd; }

    public static void main(String[] args) throws Exception {
        APIGetter<Article> news = new NewsAPIGetter("190415b2675d41f6b5397bd6e3484f13", "/v2/top-headlines");
        Map<String, String> parameters = new HashMap<>();
        //news.setEndpoint("/v2/everything");
        System.out.println(Arrays.toString(news.query(parameters)));
    }
}