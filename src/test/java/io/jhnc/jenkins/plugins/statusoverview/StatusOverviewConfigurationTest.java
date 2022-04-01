package io.jhnc.jenkins.plugins.statusoverview;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class StatusOverviewConfigurationTest {

    @Test
    void overviewLinkIsEmptyByDefault() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
        assertThat(descriptor.getOverviewLink()).isEmpty();
    }

    @Test
    void overviewLinkIsTrimmed() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
        descriptor.setOverviewLink("    ");
        assertThat(descriptor.getOverviewLink()).isEmpty();
        descriptor.setOverviewLink("  https://li.nk   \n\t ");
        assertThat(descriptor.getOverviewLink()).isEqualTo("https://li.nk");
    }

    @Test
    void overviewLinkIsSafetToNull() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
        descriptor.setOverviewLink(null);
        assertThat(descriptor.getOverviewLink()).isEmpty();
    }

    @Test
    void linkRootIsEmptyOnEmptyUrl() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
        descriptor.setOverviewLink("");
        assertThat(descriptor.getLinkRoot()).isEmpty();
    }

    @Test
    void linkRootContainsProtocolAndRootPath() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
        descriptor.setOverviewLink("https://abc.de/efg/hi");
        assertThat(descriptor.getLinkRoot()).isEqualTo("https://abc.de");
    }

    @Test
    void checkOverviewLinkChecksPermission() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);

            final FormValidation result = descriptor.doCheckOverviewLink("");
            assertThat(result.kind).isEqualTo(FormValidation.Kind.OK);

            verify(jenkins).checkPermission(Jenkins.ADMINISTER);
        }
    }

    @Test
    void checkOverviewLinkAcceptsEmpty() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);

            final FormValidation result = descriptor.doCheckOverviewLink("");
            assertThat(result.kind).isEqualTo(FormValidation.Kind.OK);

            verify(jenkins).checkPermission(Jenkins.ADMINISTER);
        }
    }

    @Test
    void checkOverviewLinkAcceptsValidUrl() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            mockJenkins(mockStatic);

            final FormValidation result = descriptor.doCheckOverviewLink("https://abc.de/fg/hij/kl");
            assertThat(result.kind).isEqualTo(FormValidation.Kind.OK);
        }
    }

    @Test
    void checkOverviewLinkRejectsInvalidUrl() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            mockJenkins(mockStatic);

            final FormValidation result = descriptor.doCheckOverviewLink("invalid url");
            assertThat(result.kind).isEqualTo(FormValidation.Kind.ERROR);
        }
    }

    @Test
    void configureChecksPermission() throws Descriptor.FormException {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);
            when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true);

            descriptor.configure(mock(StaplerRequest.class), new JSONObject().element("statusOverviewLink", ""));

            verify(jenkins).hasPermission(Jenkins.ADMINISTER);
        }
    }

    @Test
    void configureThrowsOnMissingPermission() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);
            when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(false);

            assertThrows(Descriptor.FormException.class, () ->
                    descriptor.configure(mock(StaplerRequest.class), new JSONObject().element("statusOverviewLink", "")));
        }
    }

    @Test
    void configureSetsOverviewLink() throws Descriptor.FormException {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);
            when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true);

            descriptor.configure(mock(StaplerRequest.class), new JSONObject().element("statusOverviewLink", "https://xy.z"));

            assertThat(descriptor.getOverviewLink()).isEqualTo("https://xy.z");
            verify(descriptor).save();
        }
    }

    @Test
    void configureThrowsOnInvalidOverviewLink() {
        try (MockedStatic<Jenkins> mockStatic = Mockito.mockStatic(Jenkins.class)) {
            final StatusOverviewConfiguration.DescriptorImpl descriptor = createSpy();
            final Jenkins jenkins = mockJenkins(mockStatic);
            when(jenkins.hasPermission(Jenkins.ADMINISTER)).thenReturn(true);

            assertThrows(Descriptor.FormException.class, () ->
                    descriptor.configure(mock(StaplerRequest.class), new JSONObject().element("statusOverviewLink", "an invalid url")));
        }
    }

    private StatusOverviewConfiguration.DescriptorImpl createSpy() {
        final StatusOverviewConfiguration.DescriptorImpl descriptor = mock(StatusOverviewConfiguration.DescriptorImpl.class,
                withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        doNothing().when(descriptor).save();
        return descriptor;
    }

    private Jenkins mockJenkins(MockedStatic<Jenkins> mockedStatic) {
        final Jenkins jenkins = mock(Jenkins.class);
        mockedStatic.when(Jenkins::get).thenReturn(jenkins);
        doNothing().when(jenkins).checkPermission(any());
        return jenkins;
    }
}