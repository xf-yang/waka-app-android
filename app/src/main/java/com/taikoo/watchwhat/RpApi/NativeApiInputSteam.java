package com.taikoo.watchwhat.RpApi;


import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class NativeApiInputSteam  {
    private String mFileId;
    private api.FileReador fr;
    private byte[] dt;
    private int index = 0;

    Bitmap bitmap;

    public NativeApiInputSteam(String fileId) throws Exception {
        mFileId = fileId;
        if (dt == null) {
            fr = new api.FileReador(mFileId);
            dt = fr.read(0, fr.getSize());

//             bitmap = BitmapFactory.decodeByteArray(dt, 0,dt.length);
//
//
//            byte[] b = dt;
//            Log.d("aaaaaaaaaa", "NativeApiInputSteam: " + dt.length);
        }
    }


    public InputStream GetInputStream(){

        InputStream is = new ByteArrayInputStream(dt);

//        InputStream is;
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return is;
    }



}
