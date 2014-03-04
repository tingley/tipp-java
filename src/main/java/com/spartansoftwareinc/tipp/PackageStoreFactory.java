package com.spartansoftwareinc.tipp;

import java.io.IOException;

public interface PackageStoreFactory {
    PackageStore newPackageStore() throws IOException;
}
