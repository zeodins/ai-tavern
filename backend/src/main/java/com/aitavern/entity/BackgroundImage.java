package com.aitavern.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "backgrounds")
public class BackgroundImage {

    @Id
    private String id;
    private byte[] data;
    private String contentType;

    public BackgroundImage() {}

    public BackgroundImage(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
