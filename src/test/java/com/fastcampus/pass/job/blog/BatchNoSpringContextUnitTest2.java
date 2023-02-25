package com.fastcampus.pass.job.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.*;

import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassStatus;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.database.JdbcPagingItemReader;

import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public class BatchNoSpringContextUnitTest2 {
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private ConfigurableApplicationContext context;
	private LocalDate orderDate;
	private BatchOnlyJdbcReaderTestConfiguration job;

	@Before
	void setUp() {
		this.context = new AnnotationConfigApplicationContext(TestDataSourceConfiguration.class);
		this.dataSource = (DataSource) context.getBean("dataSource");
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
		this.orderDate = LocalDate.of(2019, 10, 6);
		this.job = new BatchOnlyJdbcReaderTestConfiguration(dataSource);
		this.job.setChunkSize(10);
 	}

	@After
	public void tearDown() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void 기간내_Sales가_집계되어_SalesSum이된다() throws Exception {
		//given
		long amount1 = 1000;
		long amount2 = 100;
		long amount3 = 10;
		jdbcTemplate.update("insert into"); //테스트 환경을 구축
		jdbcTemplate.update("insert into");
		jdbcTemplate.update("insert into");

		//reader를 가져온ㄷ나
 		JdbcPagingItemReader<PassEntity> reader = job.batchOnlyJdbcReaderTestJobReader(orderDate.toString());
		 //reader의 쿼리를 생성합니다.
		reader.afterPropertiesSet();

		//when & then
		assertThat(reader.read().getStatus()).isEqualTo(PassStatus.READY);
		assertThat(reader.read()).isNull();
	}

	@Configuration
	public static class TestDataSourceConfiguration {
		private static final String CREATE_SQL =
				"create table IF NOT EXISTS `sales` (id bigint not null auto_increment, amount bigint not null, order_date date, order_no varchar(255), primary key (id)) engine=InnoDB;";

		@Bean
		public DataSource dataSource() {
			EmbeddedDatabaseFactory databaseFactory = new EmbeddedDatabaseFactory();
			databaseFactory.setDatabaseType(H2);
			return databaseFactory.getDatabase();
		}

		@Bean
		public DataSourceInitializer initializer(DataSource dataSource) {
			DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
			dataSourceInitializer.setDataSource(dataSource);

			Resource create = new ByteArrayResource(CREATE_SQL.getBytes());
			dataSourceInitializer.setDatabasePopulator(new ResourceDatabasePopulator(create));

			return dataSourceInitializer;
		}
	}
}
