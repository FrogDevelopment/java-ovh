package com.ovh.api.test;

import com.google.gson.Gson;
import com.ovh.api.OvhApi;
import com.ovh.api.OvhProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OvhApi.class, System.class})
public class ApiTest {

    private HttpURLConnection mockCon;

    private final String me = "{\"firstname\":\"Foo\",\"vat\":\"\",\"ovhSubsidiary\":\"FR\",\"area\":\"\",\"birthDay\":\"Invalid date\",\"nationalIdentificationNumber\":null,\"spareEmail\":null,\"ovhCompany\":\"ovh\",\"state\":\"complete\",\"email\":\"test@foobar.com\",\"currency\":{\"symbol\":\"€\",\"code\":\"EUR\"},\"city\":\"Roubaix\",\"fax\":\"\",\"nichandle\":\"fb0000-ovh\",\"address\":\"1 rue du Foobar\",\"companyNationalIdentificationNumber\":null,\"birthCity\":\"\",\"country\":\"FR\",\"language\":\"fr_FR\",\"organisation\":\"\",\"name\":\"Bar\",\"phone\":\"+33.000000000\",\"sex\":\"male\",\"zip\":\"59000\",\"corporationType\":\"\",\"legalform\":\"individual\"}";

    @Before
    public void setup() throws Exception {
        mockCon = Mockito.mock(HttpURLConnection.class);

        var mockedUrl = PowerMockito.mock(URL.class);
        whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(mockedUrl);
        when(mockedUrl.openConnection()).thenReturn(mockCon);
    }

    @Test
    public void me() throws Exception {
        // given
        var ovhProperties = new OvhProperties();
        ovhProperties.setEndpoint("ovh-eu");
        ovhProperties.setApplicationKey("000000000000000");
        ovhProperties.setApplicationSecret("00000000000000000000000000000000");
        ovhProperties.setConsumerKey("00000000000000000000000000000000");

        var api = new OvhApi(ovhProperties);

        givenResponse(me, HTTP_OK);

        // when
        var json = api.get("/me");

        // then
        var gson = new Gson();
        var me = gson.fromJson(json, Me.class);

        Assert.assertEquals(me.firstname, "Foo");
        Assert.assertEquals(me.name, "Bar");
        Assert.assertEquals(me.nichandle, "fb0000-ovh");
    }

    private void givenResponse(String resp, int respCode) throws Exception {
        var inputStrm = new ByteArrayInputStream(resp.getBytes(UTF_8));
        given(mockCon.getInputStream()).willReturn(inputStrm);
        given(mockCon.getResponseCode()).willReturn(respCode);
    }

    private static class Me {
        public String firstname;
        public String name;
        public String nichandle;

    }

}
