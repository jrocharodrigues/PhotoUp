package com.impecabel.photoup;

import com.alexbbb.uploadservice.NameValue;

import java.util.ArrayList;

/**
 * Created by jrodrigues on 26-01-2015.
 */
public class UploadServer {
    private Boolean enabled;
    private String URL;
    private String method;
    private String type;
    private String fileParameterName;
    private ArrayList<NameValue> headers;
    private ArrayList<NameValue> parameters;

    public UploadServer(String URL, String method, String fileParameterName, String type, ArrayList<NameValue> headers,  ArrayList<NameValue> parameters) {
        this.enabled = true;
        this.URL = URL;
        this.method = method;
        this.fileParameterName = fileParameterName;
        this.type = type;
        this.headers = headers;
        this.parameters = parameters;
    }

    public Boolean isEnabled() { return enabled; }

    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }
    public String getFileParameterName() {
        return fileParameterName;
    }

    public void setFileParameterName(String fileParameterName) {
        this.fileParameterName = fileParameterName;
    }

    public ArrayList<NameValue> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<NameValue> headers) {
        this.headers = headers;
    }

    public ArrayList<NameValue> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<NameValue> parameters) {
        this.parameters = parameters;
    }
}
