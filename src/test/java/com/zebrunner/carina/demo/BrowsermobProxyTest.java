package com.zebrunner.carina.demo;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.HarEntry;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.core.registrar.ownership.MethodOwner;
import com.zebrunner.carina.proxy.browserup.CarinaBrowserUpProxy;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.zebrunner.carina.utils.R;
import com.zebrunner.carina.proxy.ProxyPool;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BrowsermobProxyTest implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test(description = "Test proxy")
    @MethodOwner(owner = "oshcherbina")
    public void analyzeTrafficTest() {
        final String targetUrl = "https://www.solvd.com/";
        final String trackingRequestUrl = "https://px.ads.linkedin.com/wa/";
        final String jsonPropertyToCheck = "domain";
        final String expectedPropertyValue = "solvd.com";

        R.CONFIG.put("browserup_proxy", "true", true);
        R.CONFIG.put("proxy_type", "DYNAMIC", true);
        R.CONFIG.put("proxy_port", "0", true);
        WebDriver driver = getDriver();
        BrowserUpProxy proxy = ProxyPool.getOriginal(CarinaBrowserUpProxy.class)
                .orElseThrow().getProxy();
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.newHar();

        driver.get(targetUrl);

        List<HarEntry> relevantHarEntries = proxy.getHar().getLog().getEntries().stream()
                .filter(entry -> entry.getRequest().getUrl().contains(trackingRequestUrl))
                .collect(Collectors.toList());
        Assert.assertFalse(relevantHarEntries.isEmpty(), "No requests to the specified URL were captured.");

        String requestBody = relevantHarEntries.get(0).getRequest().getPostData().getText();
        Optional<String> propertyValue = getJsonPropertyValue(jsonPropertyToCheck, requestBody);
        Assert.assertTrue(propertyValue.isPresent() && expectedPropertyValue.equals(propertyValue.get()),
                "The expected JSON property value was not found or did not match.");
    }

    private Optional<String> getJsonPropertyValue(String property, String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (jsonObject.has(property) && jsonObject.get(property).isJsonPrimitive() && jsonObject.get(property).getAsJsonPrimitive().isString()) {
                return Optional.of(jsonObject.get(property).getAsString());
            }
        } catch (Exception e) {
            LOGGER.error("Error processing JSON: " + json, e);
        }
        return Optional.empty();
    }
}

