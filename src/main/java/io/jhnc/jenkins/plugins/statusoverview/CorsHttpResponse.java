/*
 * MIT License
 *
 * Copyright (c) 2021-2022 jhnc-oss
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
import groovy.json.JsonBuilder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class CorsHttpResponse implements HttpResponse {
    private final String text;
    private final String contentType;

    public CorsHttpResponse(@NonNull String text, @NonNull String contentType) {
        this.text = text;
        this.contentType = contentType;
    }

    @Override
    public void generateResponse(StaplerRequest req, @NonNull StaplerResponse rsp, Object node) throws IOException {
        rsp.setContentType(contentType + ";charset=UTF-8");

        final String originUrl = getOriginUrl();

        if (!originUrl.isEmpty()) {
            rsp.addHeader("Access-Control-Allow-Origin", originUrl);
            rsp.addHeader("Access-Control-Allow-Credentials", "true");
        }

        try (PrintWriter pw = rsp.getWriter()) {
            pw.print(text);
            pw.flush();
        }
    }

    @NonNull
    protected Jenkins getJenkins() {
        return Jenkins.get();
    }

    @NonNull
    private String getOriginUrl() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = (StatusOverviewConfiguration.DescriptorImpl)
                getJenkins().getDescriptor(StatusOverviewConfiguration.class);
        return descriptor == null ? "" : descriptor.getLinkRoot();
    }

    @NonNull
    public static CorsHttpResponse json(@NonNull Object obj) {
        return json(new JsonBuilder(obj).toString());
    }

    @NonNull
    public static CorsHttpResponse json(@NonNull String payload) {
        return new CorsHttpResponse(payload, "application/json");
    }
}
