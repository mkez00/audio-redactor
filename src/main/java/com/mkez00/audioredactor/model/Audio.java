package com.mkez00.audioredactor.model;

import com.fasterxml.jackson.databind.ser.Serializers;

import java.util.List;

public class Audio extends BaseAudio{
    private List<RedactionSegment> segments;
    private String destinationFileName;

    public List<RedactionSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<RedactionSegment> segments) {
        this.segments = segments;
    }

    public String getDestinationFileName() {
        return destinationFileName;
    }

    public void setDestinationFileName(String destinationFileName) {
        this.destinationFileName = destinationFileName;
    }
}
