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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.json.JsonBuilder;
import hudson.Extension;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.RootAction;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("PMD.TooManyMethods")
@Extension
public class StatusOverviewAction implements RootAction, StaplerProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusOverviewAction.class);
    public static final PermissionGroup PERMISSIONS_GROUP = new PermissionGroup(StatusOverviewAction.class, Messages._StatusOverviewManager_PermissionGroup());
    public static final Permission READ = new Permission(PERMISSIONS_GROUP, Messages.StatusOverviewAction_ReadPermission(),
            Messages._StatusOverviewAction_ReadPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);
    private final Cache<String, String> responseCache = Caffeine.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .build();

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "status-overview";
    }

    @Override
    public Object getTarget() {
        checkPermission();
        return this;
    }

    @RequirePOST
    @NonNull
    public HttpResponse doPlugins(@NonNull StaplerRequest req) {
        checkPermission();
        LOGGER.debug("Plugins status request from '{}'", req.getRemoteHost());

        try (ACLContext ignored = changeContext()) {
            return response(responseCache.get("plugins", key -> collectPluginData()));
        }
    }

    @RequirePOST
    @NonNull
    public HttpResponse doAgents(@NonNull StaplerRequest req) {
        checkPermission();
        LOGGER.debug("Agents status request from '{}'", req.getRemoteHost());

        try (ACLContext ignored = changeContext()) {
            return response(responseCache.get("agents", key -> collectAgentData()));
        }
    }

    @RequirePOST
    @NonNull
    public HttpResponse doMaster(@NonNull StaplerRequest req) {
        checkPermission();
        LOGGER.debug("Master status request from '{}'", req.getRemoteHost());

        try (ACLContext ignored = changeContext()) {
            return response(responseCache.get("master", key -> collectMasterData()));
        }
    }

    protected void checkPermission() {
        Jenkins.get().checkPermission(READ);
    }

    @NonNull
    protected ACLContext changeContext() {
        return ACL.as2(ACL.SYSTEM2);
    }

    @NonNull
    protected Jenkins getJenkins() {
        return Jenkins.get();
    }

    @CheckForNull
    protected Computer getComputer(@NonNull Node node) {
        return node.toComputer();
    }

    @NonNull
    protected NodeComputerDetails getNodeDetails(@NonNull Computer computer) {
        return new NodeComputerDetails(computer);
    }

    @NonNull
    private String collectPluginData() {
        final Jenkins jenkins = getJenkins();
        final PluginManager pluginManager = jenkins.getPluginManager();
        final List<Map<String, String>> pluginList = new ArrayList<>();

        for (final PluginWrapper plugin : pluginManager.getPlugins()) {
            final Map<String, String> pluginDetails = new HashMap<>();
            pluginDetails.put("name", plugin.getShortName());
            pluginDetails.put("displayName", plugin.getDisplayName());
            pluginDetails.put("version", plugin.getVersion());
            pluginList.add(pluginDetails);
        }
        return toJson(pluginList);
    }

    @NonNull
    private String collectAgentData() {
        final List<Map<String, String>> nodeList = new ArrayList<>();

        for (final Node node : getJenkins().getNodes()) {
            if (node != null) {
                final Computer computer = getComputer(node);

                if (computer != null) {
                    nodeList.add(transformToMap(getNodeDetails(computer), false));
                }
            }
        }
        return toJson(nodeList);
    }

    @CheckForNull
    private String collectMasterData() {
        final Computer master = getJenkins().getComputers()[0];

        if (master != null) {
            return toJson(Collections.singletonList(transformToMap(getNodeDetails(master), true)));
        }
        return null;
    }

    @NonNull
    private Map<String, String> transformToMap(@NonNull NodeComputerDetails details, boolean isMaster) {
        final Map<String, String> data = new HashMap<>();
        data.put("name", details.getHostname());
        data.put("operatingSystem", details.getOperatingSystem());
        data.put("numExecutors", Integer.toString(details.getNumExecutors()));
        data.put("memory", details.getMemoryUtilization());
        data.put("javaVersion", details.getJavaVersion());

        if (isMaster) {
            data.put("coreVersion", details.getCoreVersion());
        } else {
            data.put("status", details.isOffline() ? "Offline (" + details.getOfflineCauseReason() + ")" : "Online");
        }

        return data;
    }

    @NonNull
    private String toJson(@NonNull Object obj) {
        return new JsonBuilder(obj).toString();
    }

    @NonNull
    private HttpResponse response(@CheckForNull String payload) {
        if (payload != null) {
            return CorsHttpResponse.json(payload);
        }
        return HttpResponses.notFound();
    }
}
