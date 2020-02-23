package com.github.ontio.explorer.statistics.aggregate.support;

import org.joda.time.DateTime;
import org.joda.time.Days;

import static org.joda.time.DateTimeZone.UTC;

/**
 * @author LiuQi
 */
public class DateIdUtil {

	public static final DateTime BASE = new DateTime(2015, 1, 1, 0, 0, UTC);

	public static int parseDateId(int txTime) {
		DateTime date = new DateTime(txTime * 1000L, UTC);
		if (date.isBefore(BASE)) {
			throw new IllegalArgumentException("txTime is too early " + txTime);
		}
		return Days.daysBetween(BASE, date).getDays() + 1;
	}

	public static String toDateString(int dateId) {
		return BASE.plusDays(dateId - 1).toString("yyyyMMdd");
	}

	public static void main(String[] args) {
		System.out.println(toDateString(parseDateId(1530316800)));
	}

}
