package com.lexmark.dev.FoIP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 * @author www.codejava.net
 *
 */
public class MultipartUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private WebConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(MFP mfp, String requestURL, String charset, String referer, String cookie)
            throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        httpConn = new WebConnection(mfp,requestURL);
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
        httpConn.setRequestProperty("Test", "Bonjour");
        httpConn.setRequestProperty("Target", "_self");
        if (!cookie.isEmpty())
            httpConn.setRequestProperty("Cookie", cookie);
        if (!referer.isEmpty())
            httpConn.setRequestProperty("Referer",referer);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(name);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=");
        writer.append(charset);
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"; filename=\"");
        writer.append(fileName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: ");
        writer.append(URLConnection.guessContentTypeFromName(fileName));
        writer.append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param inputStream an InputStream to be uploaded
     * @throws IOException
     */
    public void addFilePartFromStream(String fieldName, InputStream inputStream, String fileName)
            throws IOException {
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"; filename=\"");
        writer.append(fileName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: ");
        writer.append(URLConnection.guessContentTypeFromName(fileName));
        writer.append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        //FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            System.out.println(bytesRead);
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param inputStream an InputStream to be uploaded
     * @throws IOException
     */
    void addFilePartFromStream2(String fieldName, InputStream inputStream, String fileName)
            throws IOException {
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"; filename=\"");
        writer.append(fileName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: application/octet-stream");
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        //FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }
    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePartAlternate(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"; filename=\"");
        writer.append(fileName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: application/octet-stream");
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param fileName a File to be uploaded
     * @throws IOException
     */
    public void addFilePartAlternate2(String fieldName, String fileName, byte[] uploadBytes)
            throws IOException {
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"; filename=\"");
        writer.append(fileName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: application/octet-stream");
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        outputStream.write(uploadBytes, 0, uploadBytes.length);
        outputStream.flush();

        writer.flush();
    }


    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @throws IOException
     */
    public void addSourcePart(String fieldName)
            throws IOException {
        writer.append("--");
        writer.append(boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"");
        writer.append(fieldName);
        writer.append("\"");
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append("local");
        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addSourcePart(String fieldName, String value)
            throws IOException {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName + "\"")
                .append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(
                "local")
                .append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request.
     * @param name - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public java.util.List<String> finish() throws IOException {
        java.util.List<String> response = new ArrayList<>();

        writer.append(LINE_FEED);
        writer.flush();
        writer.append("--");
        writer.append(boundary);
        writer.append("--");
        writer.append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }
}
