package com.spartansoftwareinc.tipp;

class RequestPackageBase extends PackageBase implements RequestTIPP {

    RequestPackageBase(Payload payload, Manifest manifest) {
        super(payload, manifest);
    }

    public boolean isRequest() {
        return true;
    }
}
