package com.spartansoftwareinc.tipp;

class RequestPackageBase extends PackageBase implements RequestTIPP {

    RequestPackageBase(Payload payload, Manifest manifest) {
        super(payload, manifest);
    }

    public boolean isRequest() {
        return true;
    }

    @Override
    public RequestTIPP asRequestTIPP() {
        return (RequestTIPP)this;
    }

    @Override
    public ResponseTIPP asResponseTIPP() {
        throw new IllegalStateException("TIPP is not a response");
    }
}
