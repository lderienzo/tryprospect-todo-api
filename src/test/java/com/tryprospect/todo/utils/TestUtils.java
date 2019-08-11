package com.tryprospect.todo.utils;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import java.io.IOException;
import java.time.Clock;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryprospect.todo.api.Todo;

import io.dropwizard.jackson.Jackson;

public final class TestUtils {

    public static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

    public static Todo createTestTodoFromJson()throws IOException {
        return OBJECT_MAPPER.readValue(fixture("fixtures/todo.json"), Todo.class);
    }

    public static final Date getFutureDate() {
        Calendar calendar = getCalenderSetToNow();
        addMinute(calendar);
        return calendar.getTime();
    }

    public static final Calendar getCalenderSetToNow() {
        Date now = Date.from(Clock.systemDefaultZone().instant());
        return convertDateToCalendar(now);
    }

    private static final Calendar convertDateToCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    private static final void addMinute(Calendar c) {
        c.add(Calendar.MINUTE, 1);
    }

    public static final Date getPastDate() {
        Calendar calendar = getCalenderSetToNow();
        subtractDay(calendar);
        return calendar.getTime();
    }

    private static void subtractDay(Calendar c) {
        c.add(Calendar.DAY_OF_YEAR, -1);
    }

    public static final Date lastModifiedNow() {
        return Date.from(Clock.systemDefaultZone().instant());
    }
}
