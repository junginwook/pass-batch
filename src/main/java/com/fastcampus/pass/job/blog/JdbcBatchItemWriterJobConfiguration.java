package com.fastcampus.pass.job.blog;

import com.fastcampus.pass.repository.pass.PassEntity;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcBatchItemWriterJobConfiguration {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	private static final int chunkSize = 10;

	@Bean
	public Job jdbcBatchItemWriterJob() {
		return jobBuilderFactory.get("jdbcBatchItemWriterJob")
				.start(jdbcBatchItemWriterStep())
				.build();
	}

	@Bean
	public Step jdbcBatchItemWriterStep() {
		return stepBuilderFactory.get("jdbcBatchItemWriterStep")
				.<Map<String, Object>, Map<String, Object>>chunk(chunkSize)
				.reader(jdbcBatchItemWriterReader())
				.writer(jdbcBatchItemWriter())
				.build();
	}

	@Bean
	public JdbcCursorItemReader<Map<String, Object>> jdbcBatchItemWriterReader() {
		return new JdbcCursorItemReaderBuilder<Map<String, Object>>()
				.fetchSize(chunkSize)
				.dataSource(dataSource)
				.rowMapper(new BeanPropertyRowMapper<Map<String, Object>>())
				.sql("")
				.name("jdbcBatchItemWriter")
				.build();
	}

	@Bean
	public JdbcBatchItemWriter<Map<String, Object>> jdbcBatchItemWriter() {
		return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
				.dataSource(dataSource)
				.sql("")
				.beanMapped()
				.build();
	}
}
