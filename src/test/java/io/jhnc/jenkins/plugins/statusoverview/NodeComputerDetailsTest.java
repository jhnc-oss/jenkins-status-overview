/*
 * MIT License
 *
 * Copyright (c) 2021-2023 jhnc-oss
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

import hudson.model.Computer;
import hudson.node_monitors.SwapSpaceMonitor;
import hudson.util.VersionNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.MemoryUsage;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeComputerDetailsTest {
    @Mock
    Computer computer;

    @Test
    void hostnameReturnsHostname() throws IOException, InterruptedException {
        when(computer.getHostName()).thenReturn("jenkins-host");

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getHostname()).isEqualTo("jenkins-host");
    }

    @Test
    void hostnameReturnsLowerCase() throws IOException, InterruptedException {
        when(computer.getHostName()).thenReturn("JENkiNS-hOst12");

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getHostname()).isEqualTo("jenkins-host12");
    }

    @Test
    void hostnameReturnsIPIfNoHostNameSupplied() throws IOException, InterruptedException {
        when(computer.getHostName()).thenReturn("127.0.0.1");

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getHostname()).isEqualTo("127.0.0.1");
    }

    @Test
    void hostnameReturnsPlaceholderIfNotAvailable() throws IOException, InterruptedException {
        when(computer.getHostName()).thenReturn(null);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getHostname()).isEqualTo("<unknown>");
    }

    @Test
    void hostnameReturnsPlaceholderOnException() throws IOException, InterruptedException {
        when(computer.getHostName()).thenThrow(IOException.class);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getHostname()).isEqualTo("<unknown>");
    }

    @Test
    void operatingSystemReturnsSystemName() {
        final Map<String, Object> data = new HashMap<>();
        data.put("hudson.node_monitors.ArchitectureMonitor", "Linux");
        when(computer.getMonitorData()).thenReturn(data);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getOperatingSystem()).isEqualTo("Linux");
    }

    @Test
    void operatingSystemReturnsPlaceholderIfNotAvailable() {
        when(computer.getMonitorData()).thenReturn(Collections.emptyMap());

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getOperatingSystem()).isEqualTo("<unknown>");
    }

    @Test
    void operatingSystemReturnsPlaceholderIfAgentNotAccessible() {
        when(computer.getMonitorData()).thenReturn(null);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getOperatingSystem()).isEqualTo("<unknown>");
    }

    @Test
    void operatingSystemReturnsPlaceholderIfValueNull() {
        final Map<String, Object> data = new HashMap<>();
        data.put("hudson.node_monitors.ArchitectureMonitor", null);
        when(computer.getMonitorData()).thenReturn(data);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getOperatingSystem()).isEqualTo("<unknown>");
    }

    @Test
    void numberOfExecutors() {
        when(computer.getNumExecutors()).thenReturn(12);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getNumExecutors()).isEqualTo(12);
    }

    @Test
    void javaVersionReturnsRuntimeNameAndVersion() throws IOException, InterruptedException {
        final Map<Object, Object> data = new HashMap<>();
        data.put("java.runtime.name", "OpenJDK Runtime Environment");
        data.put("java.runtime.version", "11.0.2+34-LTS");
        when(computer.getSystemProperties()).thenReturn(data);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getJavaVersion()).isEqualTo("OpenJDK Runtime Environment 11.0.2+34-LTS");
    }

    @Test
    void javaVersionPlaceholderIfNotAvailable() throws IOException, InterruptedException {
        when(computer.getSystemProperties()).thenReturn(Collections.emptyMap());

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getJavaVersion()).isEqualTo("<unknown> <unknown>");
    }

    @Test
    void javaVersionPlaceholderOnException() throws IOException, InterruptedException {
        when(computer.getSystemProperties()).thenThrow(IOException.class);

        final NodeComputerDetails details = new NodeComputerDetails(computer);
        assertThat(details.getJavaVersion()).isEqualTo("<unknown>");
    }

    @Test
    void coreVersionReturnsJenkinsVersion() {
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));
        when(details.getJenkinsVersion()).thenReturn(new VersionNumber("2.345.6"));
        assertThat(details.getCoreVersion()).isEqualTo("2.345.6");
    }

    @Test
    void coreVersionReturnsPlaceholderIfNotAvailable() {
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));
        when(details.getJenkinsVersion()).thenReturn(null);
        assertThat(details.getCoreVersion()).isEqualTo("<unknown>");
    }

    @Test
    void memoryUtilizationReturnsMemoryUsageInGB() {
        final Map<String, Object> data = new HashMap<>();
        data.put("hudson.node_monitors.SwapSpaceMonitor",
                new SwapSpaceMonitor.MemoryUsage2(new MemoryUsage(bytesToGB(32L), bytesToGB(7L), 0L, 0L)));
        when(computer.getMonitorData()).thenReturn(data);
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));

        assertThat(details.getMemoryUtilization()).isEqualTo("25/32 GB");
    }

    @Test
    void memoryUtilizationRounds() {
        final Map<String, Object> data = new HashMap<>();
        data.put("hudson.node_monitors.SwapSpaceMonitor",
                new SwapSpaceMonitor.MemoryUsage2(new MemoryUsage(17179869184L - 1, bytesToGB(2L), 0L, 0L)));
        when(computer.getMonitorData()).thenReturn(data);
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));

        assertThat(details.getMemoryUtilization()).isEqualTo("14/16 GB");
    }

    @Test
    void memoryUtilizationReturnsPlaceholderIfNotAvailable() {
        when(computer.getMonitorData()).thenReturn(Collections.emptyMap());
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));

        assertThat(details.getMemoryUtilization()).isEqualTo("<unknown>");
    }

    @Test
    void memoryUtilizationReturnsPlaceholderIfAgentNotAccessible() {
        when(computer.getMonitorData()).thenReturn(null);
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));

        assertThat(details.getMemoryUtilization()).isEqualTo("<unknown>");
    }

    @Test
    void memoryUtilizationReturnsPlaceholderIfValueNull() {
        final Map<String, Object> data = new HashMap<>();
        data.put("hudson.node_monitors.SwapSpaceMonitor", null);
        when(computer.getMonitorData()).thenReturn(data);
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));

        assertThat(details.getMemoryUtilization()).isEqualTo("<unknown>");
    }

    @Test
    void statusReturnsOnlineStatusIfOnline() {
        when(computer.isOffline()).thenReturn(false);
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));
        assertThat(details.isOffline()).isFalse();
    }

    @Test
    void statusReturnsOnlineStatusIfOffline() {
        when(computer.isOffline()).thenReturn(true);
        when(computer.getOfflineCauseReason()).thenReturn("node maintenance");
        final NodeComputerDetails details = spy(new NodeComputerDetails(computer));
        assertThat(details.isOffline()).isTrue();
        assertThat(details.getOfflineCauseReason()).isEqualTo("node maintenance");
    }

    private long bytesToGB(long bytes) {
        return 1024 * 1024 * 1024 * bytes;
    }
}