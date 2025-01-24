/*
 * MIT License
 *
 * Copyright (c) 2021-2025 jhnc-oss
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
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.net.URI;
import java.util.Objects;

public class StatusOverviewConfiguration extends GlobalConfiguration {

    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalConfiguration> {
        private String overviewLink;

        public DescriptorImpl() {
            super(StatusOverviewConfiguration.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Status Overview Plugin";
        }

        @Override
        public boolean configure(@NonNull StaplerRequest2 req, @NonNull JSONObject o) throws FormException {
            if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                final String overviewLink = o.getString("statusOverviewLink");

                if (!checkOverviewLink(overviewLink)) {
                    throw new FormException(Messages.StatusOverviewConfiguration_urlValidationError(), "statusOverviewLink");
                }

                setOverviewLink(overviewLink);
                save();
                return super.configure(req, o);
            }
            throw new FormException(Messages.StatusOverviewConfiguration_configurePermissionDenied(), "statusOverviewLink");
        }

        @NonNull
        public String getOverviewLink() {
            return safeAndTrimmed(overviewLink);
        }

        @NonNull
        public String getLinkRoot() {
            final String url = getOverviewLink();

            if (!url.isEmpty()) {
                final URI uri = URI.create(getOverviewLink());
                return uri.getScheme() + "://" + uri.getAuthority();
            }
            return url;
        }

        public void setOverviewLink(@CheckForNull String overviewLink) {
            this.overviewLink = safeAndTrimmed(overviewLink);
        }

        @RequirePOST
        @NonNull
        public FormValidation doCheckOverviewLink(@NonNull @QueryParameter String overviewLink) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return checkOverviewLink(overviewLink)
                    ? FormValidation.ok()
                    : FormValidation.error(Messages.StatusOverviewConfiguration_urlValidationError());
        }

        private boolean checkOverviewLink(@NonNull String overviewLink) {
            return overviewLink.isEmpty() || ValidationUtils.isValidUrl(overviewLink);
        }

        @NonNull
        private String safeAndTrimmed(@CheckForNull String str) {
            return Objects.requireNonNullElse(str, "").trim();
        }
    }
}
