package com.ariat.app.entity;

import lombok.Data;

@Data
public class EarningCall {
    private long id;
    private String ticker;
    private long datePublished;            // timestamp in milliseconds
    private String title;
    private String type;
    private String presentationUrl;
    private String transcriptAudioUrl;
    private boolean presentationAvailable;
    private boolean transcriptAudioAvailable;
}
