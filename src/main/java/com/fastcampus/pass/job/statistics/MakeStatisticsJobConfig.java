package com.fastcampus.pass.job.statistics;

import com.fastcampus.pass.job.pass.util.LocalDateTimeUtils;
import com.fastcampus.pass.repository.booking.BookingEntity;
import com.fastcampus.pass.repository.statistics.StatisticsEntity;
import com.fastcampus.pass.repository.statistics.StatisticsRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class MakeStatisticsJobConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;

	private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;
	private static final int CHUNK_SIZE = 10;
	private final StatisticsRepository statisticsRepository;

	@Bean
	public Job makeStatisticsJob() {
		Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
				.start(addStatisticsStep())
				.build();

		Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
				.start(makeDailyStatisticsStep())
				.build();

		Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
				.start(makeWeeklyStatisticsStep())
				.build();

		Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
				.split(new SimpleAsyncTaskExecutor())
				.add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow)
				.build();

		return this.jobBuilderFactory.get("makeStatisticsJob")
				.start(addStatisticsFlow)
				.next(parallelMakeStatisticsFlow)
				.build()
				.build();
	}

	@Bean
	public Step addStatisticsStep() {
		return this.stepBuilderFactory.get("addStatisticsStep")
				.<BookingEntity, BookingEntity>chunk(CHUNK_SIZE)
				.reader(addStatisticsItemReader(null, null))
				.writer(addStatisticsItemWriter())
				.build();
	}

	@Bean
	@StepScope
	public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(@Value("#{jobPrameter[from]}") String fromString, @Value("#{jobParameter[to]}") String toString) {
		final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
		final LocalDateTime to = LocalDateTimeUtils.parse(toString);

		return new JpaCursorItemReaderBuilder<BookingEntity>()
				.name("addStatisticsItemReaderBuilder")
				.entityManagerFactory(entityManagerFactory)
				.queryString("select b from BookingEntity b where b.endedAt between :from and :to")
				.parameterValues(Map.of("from", from, "to", to))
				.build();
	}

	@Bean
	public ItemWriter<BookingEntity> addStatisticsItemWriter() {
		return bookingEntities -> {
			Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();

			for (BookingEntity bookingEntity: bookingEntities) {
				final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt();
				StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

				if (statisticsEntity == null) {
					statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(bookingEntity));
				} else {
					statisticsEntity.add(bookingEntity);
				}
			}
			final List<StatisticsEntity> statisticsEntityList = new ArrayList<>(statisticsEntityMap.values());
			statisticsRepository.saveAll(statisticsEntityList);
		};
	}

	@Bean
	public Step makeDailyStatisticsStep() {
		return this.stepBuilderFactory.get("makeDailyStatisticsStep")
				.tasklet(makeDailyStatisticsTasklet)
				.build();
	}

	@Bean
	public Step makeWeeklyStatisticsStep() {
		return this.stepBuilderFactory.get("makeWeeklyStatisticsStep")
				.tasklet(makeWeeklyStatisticsTasklet)
				.build();
	}
}
