package com.github.ontio.explorer.statistics;

import com.github.ontio.explorer.statistics.aggregate.ranking.RankingDuration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.List;

/**
 * @author zhouq
 * @version 1.0
 * @date 2020/3/15
 */
public class DurationTest {

    @Test
    public void durationTest01() {
        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime dt = now.withTime(now.getHourOfDay(), 0, 0, 0);
        RankingDuration duration = RankingDuration.THREE_DAYS;

        List<RankingDuration> durations = (List<RankingDuration>)duration.involved();
    }

}
