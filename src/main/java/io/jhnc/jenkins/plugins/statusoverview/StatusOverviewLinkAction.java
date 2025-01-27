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
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerProxy;

@Extension
public class StatusOverviewLinkAction implements RootAction, StaplerProxy {
    @CheckForNull
    @Override
    public String getIconFileName() {
        return showLink() ? "monitor.png" : null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return showLink() ? "Status Overview" : null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return getOverviewLink();
    }

    @Override
    public Object getTarget() {
        checkPermission();
        return this;
    }

    protected void checkPermission() {
        Jenkins.get().checkPermission(StatusOverviewAction.READ);
    }

    @NonNull
    protected Jenkins getJenkins() {
        return Jenkins.get();
    }

    private boolean showLink() {
        return getJenkins().hasPermission(StatusOverviewAction.READ) && !ValidationUtils.isNullOrEmpty(getOverviewLink());
    }

    @CheckForNull
    private String getOverviewLink() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = (StatusOverviewConfiguration.DescriptorImpl)
                getJenkins().getDescriptor(StatusOverviewConfiguration.class);
        return descriptor == null ? null : descriptor.getOverviewLink();
    }
}