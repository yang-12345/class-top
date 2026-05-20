package io.github.rikkakawaii0612.classtop.course;

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

