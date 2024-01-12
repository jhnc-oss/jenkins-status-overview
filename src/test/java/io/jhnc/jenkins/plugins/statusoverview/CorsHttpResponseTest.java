/*
 * MIT License
 *
 * Copyright (c) 2021-2024 jhnc-oss
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
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorsHttpResponseTest {
    private static final String CONTENT_TYPE = "text/plain";
    @Mock
    Jenkins jenkins;
    @Spy
    ResponseCapture respMock = spy(new ResponseCapture());
    @Mock
    StatusOverviewConfiguration.DescriptorImpl descriptor;

    @Test
    void payloadIsIncluded() throws IOException {
        final ResponseCapture capture = new ResponseCapture();
        final CorsHttpResponse resp = create(new CorsHttpResponse("abc def", CONTENT_TYPE));
        resp.generateResponse(null, capture, null);

        assertThat(capture.getResponseString()).isEqualTo("abc def");
    }

    @Test
    void includesAccessControlAllowOriginHeaderIfUrlIsConfigured() throws IOException {
        when(descriptor.getLinkRoot()).thenReturn("https://abc.de");
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final CorsHttpResponse resp = create(new CorsHttpResponse("x", CONTENT_TYPE));
        resp.generateResponse(null, respMock, null);

        verify(respMock).addHeader("Access-Control-Allow-Origin", "https://abc.de");
        verify(respMock).addHeader("Access-Control-Allow-Credentials", "true");
    }

    @Test
    void doesNotIncludeAccessControlAllowOriginHeaderIfUrlIsNotConfigured() throws IOException {
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(null);

        final CorsHttpResponse resp = create(new CorsHttpResponse("x", CONTENT_TYPE));
        resp.generateResponse(null, respMock, null);

        verify(respMock, never()).addHeader(eq("Access-Control-Allow-Origin"), any());
        verify(respMock, never()).addHeader(eq("Access-Control-Allow-Credentials"), any());
    }

    @Test
    void doesNotIncludeAccessControlAllowOriginHeaderIfUrlIsEmpty() throws IOException {
        when(descriptor.getLinkRoot()).thenReturn("");
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final CorsHttpResponse resp = create(new CorsHttpResponse("x", CONTENT_TYPE));
        resp.generateResponse(null, respMock, null);

        verify(respMock, never()).addHeader(eq("Access-Control-Allow-Origin"), any());
    }

    @Test
    void includesContentTypeHeader() throws IOException {
        final CorsHttpResponse resp = create(new CorsHttpResponse("x", "application/json"));
        resp.generateResponse(null, respMock, null);

        verify(respMock).setContentType("application/json;charset=UTF-8");
    }

    @Test
    void jsonResponseConvertsToJson() throws IOException {
        final Map<String, String> data = new HashMap<>();
        data.put("a", "1");
        data.put("b", "2");
        final CorsHttpResponse resp = create(CorsHttpResponse.json(data));
        resp.generateResponse(null, respMock, null);

        assertThat(respMock.getResponseString()).isEqualTo("{\"a\":\"1\",\"b\":\"2\"}");
        verify(respMock).setContentType("application/json;charset=UTF-8");
    }

    @NonNull
    private CorsHttpResponse create(@NonNull CorsHttpResponse response) {
        final CorsHttpResponse spy = Mockito.spy(response);
        doReturn(jenkins).when(spy).getJenkins();
        return spy;
    }
}