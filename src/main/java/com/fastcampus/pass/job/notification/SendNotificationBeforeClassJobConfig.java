package com.fastcampus.pass.job.notification;

import com.fastcampus.pass.repository.booking.BookingEntity;
import com.fastcampus.pass.repository.booking.BookingStatus;
import com.fastcampus.pass.repository.notification.NotificationEntity;
import com.fastcampus.pass.repository.notification.NotificationEvent;
import java.time.LocalDateTime;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class SendNotificationBeforeClassJobConfig {
	private final int CHUNK_SIZE = 10;

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	private final SendNotificationItemWriter sendNotificationItemWriter;

	@Bean
	public Job sendNotificationBeforeClassJob() {
		return this.jobBuilderFactory.get("sendNotificationBeforeClassJob")
				.start(addNotificationStep())
				.next(sendNotificationStep())
				.build();
	}

	@Bean
	public Step addNotificationStep() {
		return this.stepBuilderFactory.get("addNotificationStep")
				//input output
				.<BookingEntity, NotificationEntity>chunk(CHUNK_SIZE)
				.reader(addNotificationItemReader())
				.processor(addNotificationItemProcessor())
				.writer(addNotificationItemWriter())
				.build();
	}

	/**
	 * JpaPagingItemReader: JPA에서 사용하는 페이징 기법
	 * 쿼리 당 PageSize만큼 가져오며 다름 PagingItemReader와 마찬가지로 Thread-safe 합니다.
	 */
	@Bean
	public JpaPagingItemReader<BookingEntity> addNotificationItemReader() {
		return new JpaPagingItemReaderBuilder<BookingEntity>()
				.name("addNotificationItemReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(CHUNK_SIZE)
				.queryString("select b from BookingEntity b join fetch b.userEntity where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
				.parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
				.build();
	}

	@Bean
	public ItemProcessor<BookingEntity, NotificationEntity> addNotificationItemProcessor() {
		return bookingEntity -> NotificationModelMapper.INSTANCE.toNotificationEntity(bookingEntity, NotificationEvent.BEFORE_CLASS);
	}

	@Bean
	public JpaItemWriter<NotificationEntity> addNotificationItemWriter() {
		return new JpaItemWriterBuilder<NotificationEntity>()
				.entityManagerFactory(entityManagerFactory)
				.build();
	}

	@Bean
	public Step sendNotificationStep() {
		return this.stepBuilderFactory.get("sendNotificationStep")
				.<NotificationEntity, NotificationEntity>chunk(CHUNK_SIZE)
				.reader(sendNotificationItemReader())
				.writer(sendNotificationItemWriter)
				.taskExecutor(new SimpleAsyncTaskExecutor())
				.build();
	}

	/**
	 * SynchronizedItemStreamReader: multi-thread 환경에서 reader와 writer는 thread-safe해야 합니다.
	 * Cursor 기법의 ItemReader는 thread-safe 하지 않아 Paging 기법을 사용하거나 synchronized를 선언하여 순차적으로 수행해야 합니다.
	 * @return
	 */
	@Bean
	public SynchronizedItemStreamReader<NotificationEntity> sendNotificationItemReader() {
		JpaCursorItemReader<NotificationEntity> itemReader = new JpaCursorItemReaderBuilder<NotificationEntity>()
				.name("sendNotificationItemReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("select n from NotificationEntity n where n.event = :event and n.sent = :sent")
				.parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
				.build();

		return new SynchronizedItemStreamReaderBuilder<NotificationEntity>()
				.delegate(itemReader)
				.build();
	}
}
