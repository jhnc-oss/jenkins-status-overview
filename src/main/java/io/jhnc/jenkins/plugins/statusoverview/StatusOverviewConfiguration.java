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
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.net.URI;

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
        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
            if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                final String overviewLink = o.getString("statusOverviewLink");

                //check overview link again before saving so an incorrect value won't be saved to the config
                if (checkOverviewLink(overviewLink)) {
                    setOverviewLink(overviewLink);
                }

                save();
                return super.configure(req, o);
            }
            throw new FormException("User doesn't have permissions to save", "Server ID");
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

        public void setOverviewLink(@Nullable String overviewLink) {
            this.overviewLink = safeAndTrimmed(overviewLink);
        }

        public FormValidation doCheckOverviewLink(@QueryParameter String overviewLink) {
            return checkOverviewLink(overviewLink) ? FormValidation.ok()
                    : FormValidation.error("The URL you've specified is invalid! Please specify a correct URL.");
        }

        private boolean checkOverviewLink(String overviewLink) {
            return overviewLink.isEmpty() || ValidationUtils.isValidUrl(overviewLink);
        }

        @NonNull
        private String safeAndTrimmed(@Nullable String str) {
            return Util.fixNull(str).trim();
        }
    }
}
