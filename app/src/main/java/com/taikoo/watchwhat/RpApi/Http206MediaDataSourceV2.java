package com.taikoo.watchwhat.RpApi;

import android.media.MediaDataSource;
import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Http206MediaDataSourceV2 extends MediaDataSource {
    private static final String TAG = "Http206MediaDataSourceV2";
    private final Uri mUri;
    long mDataSize = 0;

    DataPack mFirstPkg = new DataPack(0);

    DataPack[] packs = null;

    Lock lockNewPack = new ReentrantLock();

    public Http206MediaDataSourceV2(Uri uri) {
        mUri = uri;
        Log.d(TAG, "Http206MediaDataSourceV2: ");

        Runnable r = () -> request(mFirstPkg);
        new Thread(r).start();
    }


    /**
     * @param position the position in the data source to read from.
     * @param buffer   the buffer to read the data into.
     * @param offset   the offset within buffer to read the data into.
     * @param size     the number of bytes to read.
     */
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        return readAt(position, buffer, offset, size, 0);
    }


    private int readAt(long position, byte[] buffer, int offset, int size, int retryCount) throws IOException {
        if (retryCount > 10) {
            throw new IOException("data error");
        }

        int packIndex = (int) Math.floor((double) position / (double) DataPack.PackSize);

        if (packs == null) {//初始化容器
            while (mDataSize == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (packs == null) {
                int packNum = (int) Math.ceil((double) mDataSize / (double) DataPack.PackSize);
                packs = new DataPack[packNum];
                packs[0] = mFirstPkg;
//                Log.d(TAG, "readAt: new packs ,mDataSize:" + mDataSize + " packs.length:" + packs.length);
            }
        }

//        Log.d(TAG, "readAt:   position:" + position + " offset:" + offset + " size:" + size + " packIndex:" + packIndex);

        if (position >= mDataSize) {
            return -1;
        }
        if (position + size >= mDataSize) {
            size = (int) (mDataSize - position);
        }


        DataPack pkg = getDataPack(packIndex);

        if (pkg.State == DataPack.STATE_DONE) {
            int pos = (int) position % DataPack.PackSize;
            int remLen = DataPack.PackSize - pos;
            int len = Math.min(size, remLen);
            System.arraycopy(pkg.buf, pos, buffer, offset, len);
            preloadNextPack(packIndex + 1);
            return len;
        } else if (pkg.State == DataPack.STATE_ERROR) {
            request(pkg);
        } else if (pkg.State == DataPack.STATE_IDLE) {
            request(pkg);
        } else if (pkg.State == DataPack.STATE_REQUESTING) {
            Log.d(TAG, "readAt: packIndex:" + packIndex + " pkg.State:" + pkg.State);
        }

        if (pkg.State == DataPack.STATE_DONE) {
            Log.d(TAG, "readAt: packIndex:" + packIndex + " pkg.State:" + pkg.State);
        } else {
            Log.e(TAG, "readAt: packIndex:" + packIndex + " pkg.State:" + pkg.State);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return readAt(position, buffer, offset, size, retryCount + 1);
    }

    @NotNull
    private DataPack getDataPack(int packIndex) {
        DataPack pkg = packs[packIndex];

        lockNewPack.lock();
        if (pkg == null) {
//            Log.d(TAG, "readAt: new DataPack:index:" + packIndex);
            pkg = new DataPack(packIndex);
            packs[packIndex] = pkg;
        }
        lockNewPack.unlock();
        return pkg;
    }

    void preloadNextPack(int packIndex) {
        if (packIndex > packs.length - 1) return;
        DataPack pkg = getDataPack(packIndex);
        if (pkg.State == DataPack.STATE_IDLE) {
            request(pkg);
        }
    }

    @Override
    public long getSize() throws IOException {
        while (mDataSize == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mDataSize;
    }

    @Override
    public void close() throws IOException {
        Log.d(TAG, "close: ");
        packs = null;
    }

    void request(DataPack pkg) {
//        Log.d(TAG, "request: " + pkg.DataIndexFrom + " - " + pkg.DataIndexTo);
        pkg.State = DataPack.STATE_REQUESTING;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(mUri.toString());
            conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(10000);//设置超时时间
            conn.setRequestMethod("GET");//设置请求方式
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Range", "bytes=" + pkg.DataIndexFrom + "-" + pkg.DataIndexTo);
            int code = conn.getResponseCode();
            if (code != 206) {
                Log.e(TAG, "request: ERROR , the server not 206 response !");
                throw new ProtocolException();
            }

            String strContent_Range = conn.getHeaderField("Content-Range");
            if (mDataSize == 0) {
                int ind_1 = strContent_Range.indexOf("/") + 1;
                String dataSizeStr = strContent_Range.substring(ind_1);
                mDataSize = Integer.parseInt(dataSizeStr);
                Log.d(TAG, "request: mDataSize:" + mDataSize);
            }

            String strContent_Length = conn.getHeaderField("Content-Length");
            int dataLength = Integer.parseInt(strContent_Length);
//                mInputStreamPosition = start;

            InputStream is = conn.getInputStream();

            int loopReadLen = 1024 * 4;//每次读取的数据包大小
            int off = 0;
            int sumLen = 0;
            while (true) {
                int freeSize = DataPack.PackSize - off;
                int len2read = Math.min(freeSize, loopReadLen);
                int readLen = is.read(pkg.buf, off, len2read);
                if (readLen == -1) break;
//                Log.d(TAG, "request: loop read packIndex:" + pkg.mPackIndex + " off:" + off + " readLen:" + readLen);
                off += readLen;
                sumLen += readLen;
            }
//            Log.d(TAG, "request:  ContentLength:" + sumLen);

            if (dataLength != sumLen) {
                Log.e(TAG, "request: dataLength!=off  dataLength:" + dataLength + " off:" + sumLen);
            }

            pkg.State = DataPack.STATE_DONE;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (pkg.State != DataPack.STATE_DONE) {
            pkg.State = DataPack.STATE_ERROR;
        }
    }

    //=====================================================================
    static class DataPack {

        public static final int STATE_ERROR = -1;
        public static final int STATE_IDLE = 0;
        public static final int STATE_REQUESTING = 1;
        public static final int STATE_DONE = 2;

        public static final int PackSize = 1273 * 512;//合并请求的数据包大小
        public int State = STATE_IDLE;

        public int mPackIndex;

        public int DataIndexFrom;
        public int DataIndexTo;

        public byte[] buf;

        public DataPack(int packIndex) {
            mPackIndex = packIndex;

            DataIndexFrom = mPackIndex * PackSize;
            DataIndexTo = DataIndexFrom + PackSize - 1;

            buf = new byte[PackSize];
        }

    }
}
