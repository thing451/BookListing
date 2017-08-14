package com.example.android.booklisting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Book currentBook = getItem(position);

        TextView bookTitle = listItemView.findViewById(R.id.book_title);
        bookTitle.setText(currentBook.getBookTitle());

        TextView bookAuthor = listItemView.findViewById(R.id.author);
        bookAuthor.setText(currentBook.getBookAuthor());
        if (currentBook.getBookAuthor().equalsIgnoreCase("No Author Info Available")) {
            bookAuthor.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        }

        ImageView bookImage = listItemView.findViewById(R.id.book_image);
        new DownloadImageTask(bookImage).execute(currentBook.getBookImageURL());

        TextView bookDescription = listItemView.findViewById(R.id.book_description);
        bookDescription.setText(currentBook.getBookDescription());

        return listItemView;
    }

    // Code snippet acquired from https://stackoverflow.com/a/34354709
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bookImage;

        public DownloadImageTask(ImageView image) {
            this.bookImage = image;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String urldisplay = url[0];
            Bitmap mIcon11 = null;

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bookImage.setImageBitmap(result);
        }
    }
}
