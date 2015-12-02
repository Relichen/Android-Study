package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * @author Zhuo
 *         2015/11/15
 */
public class CriminalIntentJSONSerializer {

    private Context mContext;
    private String mFileName;

    public CriminalIntentJSONSerializer(Context context, String fileName) {
        mContext = context;
        mFileName = fileName;
    }

    public void saveCrimes(ArrayList<Crime> crimes) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        for (Crime c : crimes) {
            array.put(c.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream out = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                直接存放在根目录
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mFileName));
//                存放在/data/data/包名文件夹/files 中
//                out = new FileOutputStream(new File(mContext.getExternalFilesDir(null), mFileName));
            } else {
                out = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            }

            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public ArrayList<Crime> loadCrimes() throws IOException, JSONException {
        ArrayList<Crime> crimes = new ArrayList<>();
        BufferedReader reader = null;

        try {
            InputStream in = null;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                同 saveCrimes()方法
                in = new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mFileName));
            } else {
                in = mContext.openFileInput(mFileName);
            }

            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
//            忽略，它在starting fresh 时发生
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return crimes;
    }
}
