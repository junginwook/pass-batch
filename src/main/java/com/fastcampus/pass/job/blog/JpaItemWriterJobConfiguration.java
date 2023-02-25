package com.fastcampus.pass.job.blog;

import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassStatus;
import java.time.LocalDateTime;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaItemWriterJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	private final static int chunkSize = 10;

	@Bean
	public Job jpaItemWriterJob() {
		return jobBuilderFactory.get("jpaItemWriterJob")
				.start(jpaItemWriterStep())
				.build();
	}

	@Bean
	public Step jpaItemWriterStep() {
		return stepBuilderFactory.get("jpaItemWriterStep")
				.<PassEntity, PassEntity>chunk(chunkSize)
				.reader(jpaItemWriterReader())
				.processor(jpaItemProcessor())
				.writer(jpaItemWriter())
				.build();
	}

	@Bean
	public JpaPagingItemReader<PassEntity> jpaItemWriterReader() {
		return new JpaPagingItemReaderBuilder<PassEntity>()
				.pageSize(chunkSize)
				.name("expirePassesItemReader")
				.entityManagerFactory(entityManagerFactory)
				//상태(status)가 진행중이며, 종료일시(endedAt)이 현재 시점보다 과거일 경우 만료 대상이 됩니다.
				.queryString("select p from PassEntity p where p.status =:status and p.endedAt <= :endedAt")
				.parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
				.build();
	}

	@Bean
	public ItemProcessor<PassEntity, PassEntity> jpaItemProcessor() {
		return passEntity -> {
			passEntity.setStatus(PassStatus.EXPIRED);
			return passEntity;
		};
	}

	@Bean
	public JpaItemWriter<PassEntity> jpaItemWriter() {
		return new JpaItemWriterBuilder<PassEntity>()
				.entityManagerFactory(entityManagerFactory)
				.build();
	}
}
