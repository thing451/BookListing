package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String REQUEST_URL_START = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String REQUEST_URL_END = "&maxResults=";

    private String mSearchResults;

    // Adapter for the list of books
    private BookAdapter mAdapter;

    private TextView mEmptyStateTextView;
    private ListView bookListView;
    private View loadingIndicator;
    private EditText editBookTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editBookTitle = (EditText) findViewById(R.id.book_search);

        Spinner spinner;
        spinner = (Spinner) findViewById(R.id.search_results_spinner);
        String[] spinnerList = getResources().getStringArray(R.array.search_results);
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, spinnerList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                mSearchResults = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        bookListView = (ListView) findViewById(R.id.list);
        bookListView.setEmptyView(mEmptyStateTextView);

        // Set empty state text to display "Please enter topic and click search"
        mEmptyStateTextView.setText(R.string.starting_text);

        // Hide loading indicator because no search has been initiated yet
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                Book currentBook = mAdapter.getItem(position);

                Uri bookUri = Uri.parse(currentBook.getBookInfoURL());
                Intent webIntent = new Intent(Intent.ACTION_VIEW, bookUri);
                startActivity(webIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        Button b1 = (Button) findViewById(R.id.button_search);
        b1.setOnClickListener(new View.OnClickListener() {
            String userSearchInput;

            @Override
            public void onClick(View view) {
                // Gets the user inputted search terms and combines them with the url data for a
                // complete url link
                userSearchInput = editBookTitle.getText().toString().trim();
                String completeRequest_URL = REQUEST_URL_START + userSearchInput +
                        REQUEST_URL_END + mSearchResults;

                // Hide keyboard UI after button click
                editBookTitle.onEditorAction(EditorInfo.IME_ACTION_DONE);

                // Checks the adapter for data and clears if there is
                if (mAdapter != null) {
                    mAdapter.clear();
                }

                // Get details on the currently active default data network
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Remove starting text and show loading indicator after user clicks search
                    mEmptyStateTextView.setText("");
                    loadingIndicator.setVisibility(View.VISIBLE);
                    // Begins new asynctask for getting the JSON data from the user provided input
                    new BookQueryAsyncTask().execute(completeRequest_URL);
                    bookListView.setAdapter(mAdapter);
                } else {
                    loadingIndicator.setVisibility(View.GONE);
                    // Update empty state with no connection error message
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });
    }

    private class BookQueryAsyncTask extends AsyncTask<String, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(String... url) {
            if (url.length < 1 || url[0] == null) {
                return null;
            }

            List<Book> books = QueryBook.fetchBookData(url[0]);
            return books;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null || books.isEmpty()) {
                mEmptyStateTextView.setText(R.string.search_error);
                loadingIndicator.setVisibility(View.GONE);
                return;
            }
            mAdapter = new BookAdapter(getApplication(), books);
            bookListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            loadingIndicator.setVisibility(View.GONE);
        }
    }
}