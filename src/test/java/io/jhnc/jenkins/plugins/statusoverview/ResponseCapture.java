/*
 * MIT License
 *
 * Copyright (c) 2021 jhnc-oss
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jhnc.jenkins.plugins.statusoverview;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.json.JsonConfig;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.fail;

public class ResponseCapture implements StaplerResponse {
    private final StringWriter out = new StringWriter();
    private final PrintWriter writer = new PrintWriter(out);
    private int statusCode = 0;

    public String getResponseString() {
        return out.toString();
    }

    @Override
    public void forward(Object it, String url, StaplerRequest request) {
    }

    @Override
    public void forwardToPreviousPage(StaplerRequest request) {
    }

    @Override
    public void sendRedirect2(@NonNull String url) {
    }

    @Override
    public void sendRedirect(int statusCore, @NonNull String url) {
    }

    @Override
    public void serveFile(StaplerRequest request, URL res) {
    }

    @Override
    public void serveFile(StaplerRequest request, URL res, long expiration) {
    }

    @Override
    public void serveLocalizedFile(StaplerRequest request, URL res) {
    }

    @Override
    public void serveLocalizedFile(StaplerRequest request, URL res, long expiration) {
    }

    @Override
    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, long contentLength, String fileName) {
    }

    @Override
    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long expiration, int contentLength, String fileName) {
    }

    @Override
    public void serveFile(StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName) {
    }

    @Override
    public void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName) {
    }

    @Override
    public void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) {
    }

    @Override
    public OutputStream getCompressedOutputStream(HttpServletRequest req) {
        return null;
    }

    @Override
    public Writer getCompressedWriter(HttpServletRequest req) {
        return null;
    }

    @Override
    public int reverseProxyTo(URL url, StaplerRequest req) {
        return 0;
    }

    @Override
    public void setJsonConfig(JsonConfig config) {
    }

    @Override
    public JsonConfig getJsonConfig() {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) {
    }

    @Override
    public void sendError(int sc) {
    }

    @Override
    public void sendRedirect(String location) {
    }

    @Override
    public void setDateHeader(String name, long date) {
    }

    @Override
    public void addDateHeader(String name, long date) {
    }

    @Override
    public void setHeader(String name, String value) {
    }

    @Override
    public void addHeader(String name, String value) {
    }

    @Override
    public void setIntHeader(String name, int value) {
    }

    @Override
    public void addIntHeader(String name, int value) {
    }

    @Override
    public void setStatus(int sc) {
        statusCode = sc;
    }

    @Override
    public void setStatus(int sc, String sm) {
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return null;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {
    }

    @Override
    public void setContentLength(int len) {
    }

    @Override
    public void setContentLengthLong(long len) {
    }

    @Override
    public void setContentType(String type) {
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() {
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLocale(Locale loc) {
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public static ResponseCapture fromResponse(HttpResponse resp) {
        final ResponseCapture responseCapture = new ResponseCapture();
        try {
            resp.generateResponse(null, responseCapture, null);
        } catch (IOException | ServletException e) {
            fail(e);
        }
        return responseCapture;
    }
}
