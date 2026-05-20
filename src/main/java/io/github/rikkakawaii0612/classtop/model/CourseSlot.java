package io.github.rikkakawaii0612.classtop.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseSlot {
    public String start;
    public String end;
    public boolean selfStudy;

    // Default constructor for Jackson
    public CourseSlot() {
    }

    public CourseSlot(String start, String end, boolean selfStudy) {
        this.start = start;
        this.end = end;
        this.selfStudy = selfStudy;
    }
}

