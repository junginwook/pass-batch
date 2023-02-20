package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassStatus;
import java.time.LocalDateTime;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpirePassesJobConfig {

	//@EnableBatchProcessing로 인해 Bean으로 제공된 JobBuilderFactory, StepBuilderFactory
	private final JobBuilderFactory jobBuilderFactory;

	private final StepBuilderFactory stepBuilderFactory;

	private final EntityManagerFactory entityManagerFactory;

	private static final int CHUNK_SIZE = 5;

	public ExpirePassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.entityManagerFactory = entityManagerFactory;
	}

	@Bean
	public Job expirePassesJob() {
		return this.jobBuilderFactory.get("expirePassesJob")
				.start(expirePassesStep())
				.build();
	}

	@Bean
	public Step expirePassesStep() {
		return this.stepBuilderFactory.get("expirePassesStep")
				.<PassEntity, PassEntity>chunk(CHUNK_SIZE)
				.reader(expirePassesItemReader())
				.processor(expirePassesItemProcessor())
				.writer(expirePassesItemWriter())
				.build();
	}

	/**
	 * status 인 것들만 처리를 해줘야 하는데
	 * 페이징 기법은 가져온 페이지가 변경될 가능성이 있다.
	 * 따라서 페이징 기법보다 높은 성능으로, 데이터 변경에 무관한 무결성 조회가 가능하다.
	 * @return
	 */
	@Bean
	@StepScope
	public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
		return new JpaCursorItemReaderBuilder<PassEntity>()
				.name("expirePassesItemReader")
				.entityManagerFactory(entityManagerFactory)
				//상태(status)가 진행중이며, 종료일시(endedAt)이 현재 시점보다 과거일 경우 만료 대상이 됩니다.
				.queryString("select p from PassEntity p where p.status =:status and p.endedAt <= :endedAt")
				.parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
				.build();
	}

	@Bean
	public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
		return passEntity -> {
			passEntity.setStatus(PassStatus.EXPIRED);
			passEntity.setExpiredAt(LocalDateTime.now());
			return passEntity;
		};
	}

	@Bean
	public JpaItemWriter<PassEntity> expirePassesItemWriter() {
		return new JpaItemWriterBuilder<PassEntity>()
				.entityManagerFactory(entityManagerFactory)
				.build();
	}
}
