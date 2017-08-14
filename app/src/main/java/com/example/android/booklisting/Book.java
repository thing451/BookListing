package com.example.android.booklisting;


public class Book {

    private String mTitle, mInfoURL, mAuthor, mImageURL, mDescription;

    public Book(String title, String infoURL, String author, String imageURL, String description) {
        mTitle = title;
        mInfoURL = infoURL;
        mAuthor = author;
        mImageURL = imageURL;
        mDescription = description;
    }

    public String getBookTitle() {
        return mTitle;
    }

    public String getBookImageURL() {
        return mImageURL;
    }

    public String getBookAuthor() {
        return mAuthor;
    }

    public String getBookInfoURL() {
        return mInfoURL;
    }

    public String getBookDescription () {
        return mDescription;
    }
}
