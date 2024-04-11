package com.zebrunner.carina.demo.utils;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.HarEntry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zebrunner.carina.proxy.ProxyPool;
import com.zebrunner.carina.proxy.browserup.CarinaBrowserUpProxy;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProxyServerUtil {
    BrowserUpProxy proxy;

    public ProxyServerUtil() {
        proxy = ProxyPool.getOriginal(CarinaBrowserUpProxy.class)
                .orElseThrow().getProxy();
    }

    public void setFilter(String value) {
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        proxy.newHar();
    }

    public List<HarEntry> getHarEntries(String trackingRequestUrl) {
        return proxy.getHar().getLog().getEntries().stream()
                .filter(entry -> entry.getRequest().getUrl().contains(trackingRequestUrl))
                .collect(Collectors.toList());
    }

    public Optional<String> getJsonPropertyValue(String property, String json) {
        return Optional.ofNullable(JsonParser.parseString(json))
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(jsonObject -> jsonObject.has(property) && jsonObject.get(property).isJsonPrimitive())
                .map(jsonObject -> jsonObject.get(property).getAsString());
    }

}
