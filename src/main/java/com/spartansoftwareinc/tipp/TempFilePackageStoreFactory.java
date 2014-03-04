package com.spartansoftwareinc.tipp;

import java.io.IOException;

public class TempFilePackageStoreFactory implements PackageStoreFactory {

    public PackageStore newPackageStore() throws IOException {
        return new TempFileBackingStore();
    }

}
