package io.github.rikkakawaii0612.classtop.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeeklySchedule {
    private final Map<DayOfWeek, List<CourseSlot>> dailySchedules = new EnumMap<>(DayOfWeek.class);

    public WeeklySchedule() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            this.dailySchedules.put(dayOfWeek, new ArrayList<>());
        }
    }

    public List<CourseSlot> getDailySchedule(DayOfWeek day) {
        return this.dailySchedules.get(day);
    }

    public void setDailySchedule(DayOfWeek day, List<CourseSlot> newSchedule) {
        this.dailySchedules.put(day, newSchedule);
    }
}