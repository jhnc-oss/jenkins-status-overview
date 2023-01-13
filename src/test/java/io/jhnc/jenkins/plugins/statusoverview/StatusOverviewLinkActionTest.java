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

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusOverviewLinkActionTest {
    @Mock
    Jenkins jenkins;
    @Mock
    StatusOverviewConfiguration.DescriptorImpl descriptor;

    @Test
    void getTargetChecksPermission() {
        final StatusOverviewLinkAction action = spy(StatusOverviewLinkAction.class);
        doNothing().when(action).checkPermission();

        assertThat(action.getTarget()).isEqualTo(action);
        verify(action).checkPermission();
    }

    @Test
    void notVisibleIfNoPermission() {
        when(jenkins.hasPermission(StatusOverviewAction.READ)).thenReturn(false);
        final StatusOverviewLinkAction action = createSpy();
        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isNull();
    }

    @Test
    void notVisibleIfUrlEmpty() {
        when(descriptor.getOverviewLink()).thenReturn("");
        when(jenkins.hasPermission(StatusOverviewAction.READ)).thenReturn(true);
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final StatusOverviewLinkAction action = createSpy();
        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isNull();
    }

    @Test
    void notVisibleIfUrlNull() {
        when(descriptor.getOverviewLink()).thenReturn(null);
        when(jenkins.hasPermission(StatusOverviewAction.READ)).thenReturn(true);
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final StatusOverviewLinkAction action = createSpy();
        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isNull();
    }

    @Test
    void visibleIfPermissionAndValidUrl() {
        when(descriptor.getOverviewLink()).thenReturn("https://abc.de/fg");
        when(jenkins.hasPermission(StatusOverviewAction.READ)).thenReturn(true);
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final StatusOverviewLinkAction action = createSpy();
        assertThat(action.getIconFileName()).endsWith(".png");
        assertThat(action.getDisplayName()).isNotEmpty();
    }

    @Test
    void getUrlReturnsUrl() {
        when(descriptor.getOverviewLink()).thenReturn("https://ab.cd");
        when(jenkins.getDescriptor(StatusOverviewConfiguration.class)).thenReturn(descriptor);

        final StatusOverviewLinkAction action = createSpy();
        assertThat(action.getUrlName()).isEqualTo("https://ab.cd");
    }

    @NonNull
    private StatusOverviewLinkAction createSpy() {
        final StatusOverviewLinkAction action = spy(StatusOverviewLinkAction.class);
        doReturn(jenkins).when(action).getJenkins();
        return action;
    }
}