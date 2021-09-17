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

import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.security.ACLContext;
import hudson.slaves.DumbSlave;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusOverviewActionTest {
    private static final StaplerRequest ignore = mock(StaplerRequest.class);
    @Mock
    Jenkins jenkins;
    @Mock
    ACLContext context;


    @Test
    void actionIsNotVisible() {
        final StatusOverviewAction action = new StatusOverviewAction();
        assertThat(action.getDisplayName()).isNull();
        assertThat(action.getIconFileName()).isNull();
    }

    @Test
    void actionUrl() {
        final StatusOverviewAction action = new StatusOverviewAction();
        assertThat(action.getUrlName()).isEqualTo("status-overview");
    }

    @Test
    void accessRequiresPermission() {
        final StatusOverviewAction action = Mockito.spy(StatusOverviewAction.class);
        doNothing().when(action).checkPermission();
        assertThat(action.getTarget()).isEqualTo(action);
    }

    @Test
    void accessDeniedIfNoPermission() {
        final StatusOverviewAction action = Mockito.spy(StatusOverviewAction.class);
        doThrow(AccessDeniedException.class).when(action).checkPermission();
        assertThrows(AccessDeniedException.class, action::getTarget);
    }

    @Test
    void pluginsRequestChangesACLContext() {
        final StatusOverviewAction action = createSpy();
        final PluginManager pluginManager = mock(PluginManager.class);
        doReturn(pluginManager).when(jenkins).getPluginManager();
        when(pluginManager.getPlugins()).thenReturn(Collections.emptyList());

        action.doPlugins(ignore);
        verify(action).changeContext();
    }

    @Test
    void pluginsRequestReturnsPluginsDataJson() {
        final StatusOverviewAction action = createSpy();
        final PluginManager pluginManager = mock(PluginManager.class);
        doReturn(pluginManager).when(jenkins).getPluginManager();
        final PluginWrapper pluginA = mockPlugin("pi0", "plugin-0", "0.0.1");
        final PluginWrapper pluginB = mockPlugin("pi1", "plugin-1", "0.1.3");
        when(pluginManager.getPlugins()).thenReturn(Arrays.asList(pluginA, pluginB));

        final HttpResponse resp = action.doPlugins(ignore);
        assertThat(responseString(resp)).isEqualTo("[{\"displayName\":\"plugin-0\",\"name\":\"pi0\"," +
                "\"version\":\"0.0.1\"},{\"displayName\":\"plugin-1\",\"name\":\"pi1\",\"version\":\"0.1.3\"}]");
    }

    @Test
    void pluginsRequestCaches() {
        final StatusOverviewAction action = createSpy();
        final PluginManager pluginManager = mock(PluginManager.class);
        doReturn(pluginManager).when(jenkins).getPluginManager();
        when(pluginManager.getPlugins()).thenReturn(Collections.emptyList());

        final HttpResponse resp0 = action.doPlugins(ignore);
        final HttpResponse resp1 = action.doPlugins(ignore);

        assertThat(responseString(resp0)).isEqualTo(responseString(resp1));
        verify(pluginManager, times(1)).getPlugins();
    }

    @Test
    void pluginsRequestReturnsEmptyIfNoAgents() {
        final StatusOverviewAction action = createSpy();
        final PluginManager pluginManager = mock(PluginManager.class);
        doReturn(pluginManager).when(jenkins).getPluginManager();
        when(pluginManager.getPlugins()).thenReturn(Collections.emptyList());

        final HttpResponse resp = action.doPlugins(ignore);
        assertThat(responseString(resp)).isEqualTo("[]");
    }

    @Test
    void agentsRequestChangesACLContext() {
        final StatusOverviewAction action = createSpy();

        action.doAgents(ignore);
        verify(action).changeContext();
    }

    @Test
    void agentsRequestReturnsAgentDataJson() throws IOException, Descriptor.FormException {
        final StatusOverviewAction action = createSpy();
        final DumbSlave nodeA = new DumbSlave("agent-0", "/tmp/a", null);
        final DumbSlave nodeB = new DumbSlave("agent-1", "/tmp/b", null);
        doReturn(Arrays.asList(nodeA, nodeB)).when(jenkins).getNodes();
        final NodeComputerDetails detailsA = mockNodeDetails("agent-0", 3);
        when(detailsA.isOffline()).thenReturn(false);
        final NodeComputerDetails detailsB = mockNodeDetails("agent-1", 1);
        when(detailsB.isOffline()).thenReturn(true);
        when(detailsB.getOfflineCauseReason()).thenReturn("a reason");
        final Computer computerA = mock(Computer.class);
        final Computer computerB = mock(Computer.class);
        doReturn(computerA).when(action).getComputer(nodeA);
        doReturn(computerB).when(action).getComputer(nodeB);
        doReturn(detailsA).when(action).getNodeDetails(computerA);
        doReturn(detailsB).when(action).getNodeDetails(computerB);

        final HttpResponse resp = action.doAgents(ignore);
        assertThat(responseString(resp)).isEqualTo("[{\"numExecutors\":\"3\",\"memory\":\"2/8 GB\"," +
                "\"javaVersion\":\"11.2.3\",\"name\":\"agent-0\",\"operatingSystem\":\"Linux\",\"status\":\"Online\"}," +
                "{\"numExecutors\":\"1\",\"memory\":\"2/8 GB\",\"javaVersion\":\"11.2.3\",\"name\":\"agent-1\"," +
                "\"operatingSystem\":\"Linux\",\"status\":\"Offline (a reason)\"}]");
    }

    @Test
    void agentsRequestReturnsEmptyIfNoAgents() {
        final StatusOverviewAction action = createSpy();
        doReturn(Collections.emptyList()).when(jenkins).getNodes();

        final HttpResponse resp = action.doAgents(ignore);
        assertThat(responseString(resp)).isEqualTo("[]");
    }

    @Test
    void agentsRequestReturnsEmptyOnNullNode() {
        final StatusOverviewAction action = createSpy();
        final List<DumbSlave> nullList = Collections.singletonList(null);
        doReturn(nullList).when(jenkins).getNodes();

        final HttpResponse resp = action.doAgents(ignore);
        assertThat(responseString(resp)).isEqualTo("[]");
    }

    @Test
    void agentsRequestReturnsEmptyOnNullComputer() throws IOException, Descriptor.FormException {
        final StatusOverviewAction action = createSpy();
        final DumbSlave node = new DumbSlave("agent-0", "/tmp/a", null);
        doReturn(Collections.singletonList(node)).when(jenkins).getNodes();
        doReturn(null).when(action).getComputer(node);

        final HttpResponse resp = action.doAgents(ignore);
        assertThat(responseString(resp)).isEqualTo("[]");
    }

    @Test
    void agentsRequestCaches() {
        final StatusOverviewAction action = createSpy();
        doReturn(Collections.emptyList()).when(jenkins).getNodes();

        final HttpResponse resp0 = action.doAgents(ignore);
        final HttpResponse resp1 = action.doAgents(ignore);

        assertThat(responseString(resp0)).isEqualTo(responseString(resp1));
        verify(jenkins, times(1)).getNodes();
    }

    @Test
    void masterRequestChangesACLContext() {
        final StatusOverviewAction action = createSpy();
        final Computer computer = mock(Computer.class);
        doReturn(new Computer[]{computer}).when(jenkins).getComputers();
        final NodeComputerDetails details = mockNodeDetails("master", 0);
        doReturn(details).when(action).getNodeDetails(computer);

        action.doMaster(ignore);
        verify(action).changeContext();
    }

    @Test
    void masterRequestReturnsMasterDataJson() {
        final StatusOverviewAction action = createSpy();
        final Computer computer = mock(Computer.class);
        doReturn(new Computer[]{computer}).when(jenkins).getComputers();
        final NodeComputerDetails details = mockNodeDetails("master", 0);
        doReturn(details).when(action).getNodeDetails(computer);

        final HttpResponse resp = action.doMaster(ignore);
        assertThat(responseString(resp)).isEqualTo("[{\"numExecutors\":\"0\",\"memory\":\"2/8 GB\"," +
                "\"javaVersion\":\"11.2.3\",\"name\":\"master\",\"operatingSystem\":\"Linux\",\"coreVersion\":\"1.2.3\"}]");
    }

    @Test
    void masterRequestReturnsErrorIfNullNode() {
        final StatusOverviewAction action = createSpy();
        doReturn(new Computer[]{null}).when(jenkins).getComputers();

        final HttpResponse resp = action.doMaster(ignore);
        assertThat(responseCode(resp)).isEqualTo(404);
    }

    @Test
    void masterRequestCaches() {
        final StatusOverviewAction action = createSpy();
        final Computer computer = mock(Computer.class);
        doReturn(new Computer[]{computer}).when(jenkins).getComputers();
        final NodeComputerDetails details = mockNodeDetails("master", 0);
        doReturn(details).when(action).getNodeDetails(computer);

        final HttpResponse resp0 = action.doMaster(ignore);
        final HttpResponse resp1 = action.doMaster(ignore);

        assertThat(responseString(resp0)).isEqualTo(responseString(resp1));
        verify(jenkins, times(1)).getComputers();
    }

    private String responseString(HttpResponse resp) {
        if (resp instanceof CorsHttpResponse) {
            final CorsHttpResponse spy = Mockito.spy((CorsHttpResponse) resp);
            doReturn(jenkins).when(spy).getJenkins();
            return ResponseCapture.fromResponse(spy).getResponseString();
        }
        return ResponseCapture.fromResponse(resp).getResponseString();
    }

    private int responseCode(HttpResponse resp) {
        return ResponseCapture.fromResponse(resp).getStatus();
    }

    private StatusOverviewAction createSpy() {
        final StatusOverviewAction action = Mockito.spy(StatusOverviewAction.class);
        doReturn(context).when(action).changeContext();
        doReturn(jenkins).when(action).getJenkins();
        return action;
    }

    private PluginWrapper mockPlugin(String shortName, String displayName, String version) {
        final PluginWrapper plugin = mock(PluginWrapper.class);
        when(plugin.getShortName()).thenReturn(shortName);
        when(plugin.getDisplayName()).thenReturn(displayName);
        when(plugin.getVersion()).thenReturn(version);
        return plugin;
    }

    private NodeComputerDetails mockNodeDetails(String name, int executor) {
        final NodeComputerDetails node = mock(NodeComputerDetails.class);
        when(node.getHostname()).thenReturn(name);
        when(node.getOperatingSystem()).thenReturn("Linux");
        when(node.getNumExecutors()).thenReturn(executor);
        when(node.getMemoryUtilization()).thenReturn("2/8 GB");
        when(node.getJavaVersion()).thenReturn("11.2.3");

        if (name.equals("master")) {
            when(node.getCoreVersion()).thenReturn("1.2.3");
        }
        return node;
    }
}