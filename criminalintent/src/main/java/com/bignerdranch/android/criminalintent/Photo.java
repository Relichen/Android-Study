package com.bignerdranch.android.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zhuo
 *         2015/11/20
 */
public class Photo {
    private static final String JSON_FILENAME = "filename";
    private static final String JSON_ORIENTATION = "orientation";
    private String mFilename;
    private int mOrientation;

    public Photo(String filename) {
        mFilename = filename;
        mOrientation = 0;
    }

    public Photo(String filename, int orientation) {
        mFilename = filename;
        mOrientation = orientation;
    }

    public Photo(JSONObject jsonObject) throws JSONException {
        mFilename = jsonObject.getString(JSON_FILENAME);
        mOrientation = jsonObject.getInt(JSON_ORIENTATION);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFilename);
        json.put(JSON_ORIENTATION, mOrientation);
        return json;
    }

    public String getFilename() {
        return mFilename;
    }

    public int getOrientation() {
        return mOrientation;
    }
}
