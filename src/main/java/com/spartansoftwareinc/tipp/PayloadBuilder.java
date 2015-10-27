package com.spartansoftwareinc.tipp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

class PayloadBuilder {

    // Location values shouldn't include the section name; however, they are relative to the
    // section name.  So we need to store both the value of the location attribute in the manifest
    // as well as the actual location in the payload (which includes the section name).
    private EnumMap<TIPPSectionType, Map<String, String>> manifestLocations = new EnumMap<>(TIPPSectionType.class);
    private EnumMap<TIPPSectionType, Map<String, String>> payloadLocations = new EnumMap<>(TIPPSectionType.class);
    private Map<TIPPFile, String> locationMap = new HashMap<>();
    private Map<String, Path> tempFiles = new HashMap<>();

    void addFile(TIPPFile file, InputStream is) throws IOException {
        String suffix = getSuffix(file.getName());
        String manifestLocation = Integer.toString(file.getSequence()) + suffix;
        String payloadLocation = Payload.getFilePath(file.getSectionType(), manifestLocation);
        getSectionMap(manifestLocations, file.getSectionType()).put(file.getName(), manifestLocation);
        getSectionMap(payloadLocations, file.getSectionType()).put(file.getName(), payloadLocation);
        locationMap.put(file, manifestLocation);
        tempFiles.put(payloadLocation, FileUtil.copyToTemp(is, "file", suffix));
    }

    Payload build() {
        return new Payload(tempFiles);
    }

    Map<TIPPFile, String> getLocationMap() {
        return locationMap;
    }

    private Map<String, String> getSectionMap(EnumMap<TIPPSectionType, Map<String, String>> map,
                                              TIPPSectionType type) {
        Map<String, String> m = map.get(type);
        if (m == null) {
            m = new HashMap<>();
            map.put(type, m);
        }
        return m;
    }

    private String getSuffix(String filename) {
        int i = filename.lastIndexOf('.');
        if (i == -1) {
            return "";
        }
        return filename.substring(i);
    }
}
