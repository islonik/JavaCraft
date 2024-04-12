package my.javacraft.elastic.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DateService {

    private final DateTimeFormatter isoInstant;

    @Autowired
    public DateService() {
        this.isoInstant = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendInstant(3) // should limit the amount of number after 'dot'
                .toFormatter();
    }

    // returns: 2024-01-08T18:16:41.531Z
    public String getCurrentDate() {
        return isoInstant.format(Instant.now());
    }

    public String getNDaysBeforeDate(int n) {
        return isoInstant.format(Instant.now().minus(n, ChronoUnit.DAYS));
    }

}
