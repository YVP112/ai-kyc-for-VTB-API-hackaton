package com.mvp.kyc.model;

public class KycResult {
    private boolean ok;
    private String details;

    public KycResult() {}

    public KycResult(boolean ok, String details) {
        this.ok = ok;
        this.details = details;
    }

    public boolean isOk() { return ok; }
    public String getDetails() { return details; }
    public void setOk(boolean ok) { this.ok = ok; }
    public void setDetails(String details) { this.details = details; }
}
