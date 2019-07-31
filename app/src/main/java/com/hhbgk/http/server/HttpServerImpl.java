package com.hhbgk.http.server;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public final class HttpServerImpl extends NanoHTTPD {
    private String tag = getClass().getSimpleName();
    private FileInputStream mFileInputStream = null;

    public HttpServerImpl(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {

        String range = null;
        Map<String, String> headers = session.getHeaders();
//        Jlog.e(tag, "Request headers:");
        for (String key : headers.keySet()) {
            Log.i(tag, "  " + key + ":" + headers.get(key));
            if ("range".equals(key)) {
                range = headers.get(key);
            }
        }
//        return responseVideoStream();
        if (range != null) {
            try {
                return getPartialResponse(range);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.w(tag, "Not support range");
        return responseVideoStream();
    }

    private Response getPartialResponse(String rangeHeader) throws IOException {
        String rangeValue = rangeHeader.trim().substring("bytes=".length());
        String path = Environment.getExternalStorageDirectory().getPath() + "/dst.mov";
        File file = new File(path);
        if (!file.exists()) {
            Log.e(tag, "File not exit!");
            return null;
        }
        if (mFileInputStream != null) {
            mFileInputStream.close();
        }
        long fileLength = file.length();
        long start, end;
        if (rangeValue.startsWith("-")) {
            end = fileLength - 1;
            start = fileLength - 1 - Long.parseLong(rangeValue.substring("-".length()));
        } else {
            String[] range = rangeValue.split("-");
            start = Long.parseLong(range[0]);
            end = range.length > 1 ? Long.parseLong(range[1]) : fileLength - 1;
        }
        if (end > fileLength - 1) {
            end = fileLength - 1;
        }

        if (start <= end) {

            long contentLength = end - start + 1;
            mFileInputStream = new FileInputStream(file);
            if (start != 0) mFileInputStream.skip(start);

            Response response = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, "video/mp4", mFileInputStream, contentLength);
            response.addHeader("Content-Length", contentLength + "");
            response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            response.addHeader("Content-Type", "video/mp4");
            return response;
        } else {
            Log.e(tag, "start=" + start + " > end=" + end);
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Has closed");
        }
    }

    private Response responseVideoStream() {
        try {

            String path = Environment.getExternalStorageDirectory().getPath() + "/dst.mov";
            File file = new File(path);
            if (!file.exists()) {
                Log.e(tag, "File not exit!");
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            long len = 0;
            try {
                len = fis.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new NanoHTTPD.Response(Response.Status.OK, "video/mp4", fis, len);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return response404();
    }

    private Response response404() {
//        String url = session.getUri();
        String builder = "<!DOCTYPE html><html><body>" +
                "Sorry, Can't Found url !" +
                "</body></html>\n";
        return newFixedLengthResponse(builder);
    }
}
