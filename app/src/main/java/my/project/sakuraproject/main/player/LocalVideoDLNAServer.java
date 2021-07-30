package my.project.sakuraproject.main.player;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class LocalVideoDLNAServer extends NanoHTTPD {
    private String path;

    public LocalVideoDLNAServer(int port, String path) {
        super(port);
        this.path = path;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String uri = session.getUri();
        File f = new File(path);
        return serveFile(uri, headers, f);
    }

    private Response serveFile(String uri, Map<String, String> header, File file) {
        for (Map.Entry<String, String> entry : header.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue();
            System.out.println("key, " + key + " value " + value);
        }
        Response res;
        String mime = getMimeTypeForFile(uri);
        try {
            String etag = Integer.toHexString((file.getAbsolutePath() +
                    file.lastModified() + "" + file.length()).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    //endAt=startFrom+1000000;
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis,dataLen);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" +
                            endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                    Log.d("Server", "serveFile --1--: Start:"+startFrom+" End:"+endAt);
                }
            } else {
                if (etag.equals(header.get("if-none-match"))) {
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                    Log.d("Server", "serveFile --2--: Start:"+startFrom+" End:"+endAt);
                }
                else
                {
                    FileInputStream fis=new FileInputStream(file);
                    res = createResponse(Response.Status.OK, mime, fis,fis.available());
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                    Log.d("Server", "serveFile --3--: Start:"+startFrom+" End:"+endAt);
                }
            }
        } catch (IOException ioe) {
            res = getResponse("Forbidden: Reading file failed");
        }

        return (res == null) ? getResponse("Error 404: File not found") : res;
    }

    private Response createResponse(Response.Status status, String mimeType, InputStream message, long totalBytes) {
        Response res = newFixedLengthResponse(status, mimeType, message,totalBytes);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = newFixedLengthResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response getResponse(String message) {
        return createResponse(Response.Status.OK, "text/plain", message);
    }
}
