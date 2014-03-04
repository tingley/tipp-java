package com.spartansoftwareinc.tipp;

public class InMemoryPackageStoreFactory implements PackageStoreFactory {

    public PackageStore newPackageStore() {
        return new InMemoryBackingStore();
    }
}
