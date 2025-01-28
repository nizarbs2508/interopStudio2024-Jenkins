package com.ans.cda.fhir;

public class HttpResponseWrapper {
    private final int statusCode;
    private final String responseBody;

    public HttpResponseWrapper(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
