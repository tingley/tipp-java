package com.spartansoftwareinc.tipp;

import java.io.IOException;

abstract class PackageSource {
    
    static final String SEPARATOR = "/";
    
    abstract void open(TIPPErrorHandler errorHandler) throws IOException;
    
    abstract boolean close() throws IOException;

    abstract void copyToStore(PackageStore store) throws IOException;

}
