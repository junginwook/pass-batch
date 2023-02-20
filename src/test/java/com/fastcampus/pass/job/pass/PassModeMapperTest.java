package com.fastcampus.pass.job.pass;

import static org.junit.jupiter.api.Assertions.*;

import com.fastcampus.pass.repository.pass.BulkPassEntity;
import com.fastcampus.pass.repository.pass.BulkPassStatus;
import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PassModeMapperTest {

	@Test
	void test_toPassEntity() {
		//given
		LocalDateTime now = LocalDateTime.now();
		final String userId = "A1000000";

		BulkPassEntity bulkPassEntity = new BulkPassEntity();
		bulkPassEntity.setPackageSeq(1);
		bulkPassEntity.setUserGroupId("GROUP");
		bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
		bulkPassEntity.setCount(10);
		bulkPassEntity.setStartedAt(now.minusDays(60));
		bulkPassEntity.setEndedAt(now);

		//when
		final PassEntity passEntity = PassModeMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);

		//then
		assertEquals(passEntity.getPackageSeq(), bulkPassEntity.getPackageSeq());
		assertEquals(passEntity.getStatus(), PassStatus.READY);
		assertEquals(passEntity.getRemainingCount(), bulkPassEntity.getCount());
		assertEquals(passEntity.getStartedAt(), bulkPassEntity.getStartedAt());
		assertEquals(passEntity.getEndedAt(), bulkPassEntity.getEndedAt());
	}
}