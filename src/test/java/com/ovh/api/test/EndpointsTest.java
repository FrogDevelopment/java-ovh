package com.ovh.api.test;

import com.ovh.api.OvhApi;
import com.ovh.api.OvhProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OvhApi.class)
public class EndpointsTest {

    @Before
    public void setup() throws Exception {
        var mockCon = Mockito.mock(HttpURLConnection.class);
        given(mockCon.getInputStream()).willReturn(new ByteArrayInputStream("".getBytes(UTF_8)));
        given(mockCon.getResponseCode()).willReturn(HTTP_OK);

        var mockedUrl = mock(URL.class);
        whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(mockedUrl);
        when(mockedUrl.openConnection()).thenReturn(mockCon);
    }

    @Test
    public void raw() throws Exception {
        // given
        var api = givenEndpoint("https://foo.bar");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://foo.bar/me");
    }


    @Test
    public void ovhEu() throws Exception {
        // given
        var api = givenEndpoint("ovh-eu");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://eu.api.ovh.com/1.0/me");
    }


    @Test
    public void ovhCa() throws Exception {
        // given
        var api = givenEndpoint("ovh-ca");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://ca.api.ovh.com/1.0/me");
    }

    @Test
    public void kimsufiEu() throws Exception {
        // given
        var api = givenEndpoint("kimsufi-eu");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://eu.api.kimsufi.com/1.0/me");
    }

    @Test
    public void kimsufiCa() throws Exception {
        // given
        var api = givenEndpoint("kimsufi-ca");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://ca.api.kimsufi.com/1.0/me");
    }

    @Test
    public void soyoustartEu() throws Exception {
        // given
        var api = givenEndpoint("soyoustart-eu");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://eu.api.soyoustart.com/1.0/me");
    }

    @Test
    public void soyoustartCa() throws Exception {
        // given
        var api = givenEndpoint("soyoustart-ca");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://ca.api.soyoustart.com/1.0/me");
    }

    @Test
    public void runabove() throws Exception {
        // given
        var api = givenEndpoint("runabove");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://api.runabove.com/1.0/me");
    }

    @Test
    public void runaboveCa() throws Exception {
        // given
        var api = givenEndpoint("runabove-ca");

        // when
        api.get("/me");

        // then
        verifyNew(URL.class).withArguments("https://api.runabove.com/1.0/me");
    }

    private OvhApi givenEndpoint(@NotBlank String endpoint) {
        var ovhProperties = new OvhProperties();
        ovhProperties.setEndpoint(endpoint);
		ovhProperties.setApplicationKey("");
		ovhProperties.setApplicationSecret("");
		ovhProperties.setConsumerKey("");

        return new OvhApi(ovhProperties);
    }

}
