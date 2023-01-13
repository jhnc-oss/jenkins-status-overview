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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Computer;
import hudson.node_monitors.SwapSpaceMonitor;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NodeComputerDetails {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeComputerDetails.class);
    private static final String PLACEHOLDER = "<unknown>";
    private static final double BYTES_TO_GB_DIVISOR = 1024 * 1024 * 1024;
    private final Computer computer;

    public NodeComputerDetails(@NonNull Computer computer) {
        this.computer = Objects.requireNonNull(computer, "Computer must not be null");
    }

    @NonNull
    public String getHostname() {
        try {
            final String hostName = computer.getHostName();

            if (hostName != null) {
                return hostName.toLowerCase(Locale.UK);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Failed to obtain hostname", e);
        }

        return PLACEHOLDER;
    }

    @NonNull
    public String getOperatingSystem() {
        return Objects.requireNonNullElse(getMonitorDataOrEmpty()
                .getOrDefault("hudson.node_monitors.ArchitectureMonitor", PLACEHOLDER), PLACEHOLDER).toString();
    }

    public int getNumExecutors() {
        return computer.getNumExecutors();
    }

    public boolean isOffline() {
        return computer.isOffline();
    }

    @NonNull
    public String getOfflineCauseReason() {
        return computer.getOfflineCauseReason();
    }

    @NonNull
    public String getMemoryUtilization() {
        final Map<String, Object> monitorData = getMonitorDataOrEmpty();
        final SwapSpaceMonitor.MemoryUsage2 memUsage = (SwapSpaceMonitor.MemoryUsage2) monitorData.get("hudson.node_monitors.SwapSpaceMonitor");

        if (memUsage != null) {
            return bytesToGB(memUsage.totalPhysicalMemory - memUsage.availablePhysicalMemory)
                    + "/" + bytesToGB(memUsage.totalPhysicalMemory) + " GB";
        }
        return PLACEHOLDER;
    }

    @NonNull
    public String getJavaVersion() {
        try {
            final Map<Object, Object> systemProperties = computer.getSystemProperties();
            return systemProperties.getOrDefault("java.runtime.name", PLACEHOLDER) + " "
                    + systemProperties.getOrDefault("java.runtime.version", PLACEHOLDER);
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Failed to obtain java version", e);
        }

        return PLACEHOLDER;
    }

    @NonNull
    public String getCoreVersion() {
        final VersionNumber coreVersion = getJenkinsVersion();
        return coreVersion == null ? PLACEHOLDER : coreVersion.toString();
    }

    @CheckForNull
    protected VersionNumber getJenkinsVersion() {
        return Jenkins.getVersion();
    }

    private long bytesToGB(long bytes) {
        return Math.round(bytes / BYTES_TO_GB_DIVISOR);
    }

    @NonNull
    private Map<String, Object> getMonitorDataOrEmpty() {
        return Objects.requireNonNullElse(computer.getMonitorData(), Collections.emptyMap());
    }
}
