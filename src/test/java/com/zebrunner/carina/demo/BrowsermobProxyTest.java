package com.zebrunner.carina.demo;

import com.browserup.harreader.model.HarEntry;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.demo.utils.ProxyServerUtil;

import org.openqa.selenium.WebDriver;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.zebrunner.carina.utils.R;

import java.util.List;
import java.util.Optional;

import static com.zebrunner.carina.demo.utils.TestDataConstants.EXPECTED_PROPERTY_VALUE;
import static com.zebrunner.carina.demo.utils.TestDataConstants.JSON_PROPERTY_TO_CHECK;
import static com.zebrunner.carina.demo.utils.TestDataConstants.TRACKING_REQUEST_URL;

public class BrowsermobProxyTest extends AbstractTest {

    @Test(description = "Test proxy")
    @MethodOwner(owner = "oshcherbina")
    public void analyzeTrafficTest() {

        WebDriver driver = getDriver();

        ProxyServerUtil proxyServerUtil = new ProxyServerUtil();
        proxyServerUtil.setFilter(TRACKING_REQUEST_URL);

        driver.get(R.CONFIG.get("url"));

        List<HarEntry> relevantHarEntries = proxyServerUtil.getHarEntries(TRACKING_REQUEST_URL);
        Assert.assertFalse(relevantHarEntries.isEmpty(), "No requests to the specified URL were captured.");

        String requestBody = relevantHarEntries.get(0).getRequest().getPostData().getText();
        Optional<String> propertyValue = proxyServerUtil.getJsonPropertyValue(JSON_PROPERTY_TO_CHECK, requestBody);
        Assert.assertTrue(propertyValue.isPresent() && EXPECTED_PROPERTY_VALUE.equals(propertyValue.get()),
                "The expected JSON property value was not found or did not match.");
    }
}

