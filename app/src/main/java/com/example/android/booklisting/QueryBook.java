package com.example.android.booklisting;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryBook {

    private static final String LOG_TAG = "QueryBook";

    private QueryBook() {
    }

    public static List<Book> extractBookFromJSON(String bookJSON) {
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        List<Book> books = new ArrayList<>();

        try {
            JSONObject baseJSONResponse = new JSONObject(bookJSON);

            // Checks if searched term is available, if not returns empty book
            JSONArray bookArray;
            if (!baseJSONResponse.isNull("items")) {
                bookArray = baseJSONResponse.getJSONArray("items");
            } else {
                // Returns empty book list
                return books;
            }

            for (int i = 0; i < bookArray.length(); i++) {
                JSONObject currentBook = bookArray.getJSONObject(i);
                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                String title = volumeInfo.getString("title");

                ArrayList<String> authors = new ArrayList<>();
                StringBuilder completeAuthorList = new StringBuilder();
                if (!volumeInfo.isNull("authors")) {
                    JSONArray authorsArray = (JSONArray) volumeInfo.get("authors");
                    for (int j = 0; j < authorsArray.length(); j++) {
                        authors.add(authorsArray.getString(j));
                    }
                    for (String s : authors) {
                        completeAuthorList.append('\t');
                        completeAuthorList.append(s);
                        completeAuthorList.append('\n');
                    }
                } else {
                    completeAuthorList.append("No Author Info Available");
                }
                JSONObject imageLink = volumeInfo.getJSONObject("imageLinks");
                String thumbnailURL = imageLink.getString("thumbnail");

                // Checks to see if the value is null for the webinfo link, if not then pulls
                // available description text.
                String infoLink;
                if (!volumeInfo.isNull("infoLink")) {
                    infoLink = volumeInfo.getString("infoLink");
                } else {
                    // Sets default link for books with no link to google books homepage.
                    infoLink = "https://play.google.com/store/books";
                }

                // Checks to see if the value is null for the description, if not then pulls
                // available description text.
                String textSnippet;
                if (!volumeInfo.isNull("description")) {
                    textSnippet = volumeInfo.getString("description");
                } else {
                    textSnippet = "No Description Available";
                }

                Book book = new Book(title, infoLink, completeAuthorList.toString(),
                        thumbnailURL, textSnippet);

                books.add(book);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the book results", e);
        }
        return books;
    }

    public static List<Book> fetchBookData(String requestURL) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        URL url = createURL(requestURL);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<Book> books = extractBookFromJSON(jsonResponse);

        return books;
    }

    private static URL createURL(String stringURL) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
