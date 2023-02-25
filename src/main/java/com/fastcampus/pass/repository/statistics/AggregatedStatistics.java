package com.fastcampus.pass.repository.statistics;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AggregatedStatistics {
	private LocalDateTime statisticsAt;
	private long allCount;
	private long attendedCount;
	private long cancelledCount;

	public void merge(final AggregatedStatistics statistics) {
		this.allCount += statistics.getAllCount();
		this.attendedCount += statistics.getAttendedCount();
		this.cancelledCount += statistics.getCancelledCount();
	}
}
