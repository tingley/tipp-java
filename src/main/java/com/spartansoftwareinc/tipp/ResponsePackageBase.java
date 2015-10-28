package com.spartansoftwareinc.tipp;

class ResponsePackageBase extends PackageBase implements ResponseTIPP {

	ResponsePackageBase(Payload payload, Manifest manifest) {
		super(payload, manifest);
	}

    @Override
	public boolean isRequest() {
		return false;
	}

	@Override
	public String getRequestPackageId() {
		return ((TIPPTaskResponse)getManifest().getTask()).getRequestPackageId();
	}

    @Override
	public TIPPCreator getRequestCreator() {
		return ((TIPPTaskResponse)getManifest().getTask()).getRequestCreator();
	}

    @Override
	public TIPPResponseCode getCode() {
		return ((TIPPTaskResponse)getManifest().getTask()).getMessage();
	}

	public String getComment() {
		return ((TIPPTaskResponse)getManifest().getTask()).getComment();
	}

}
