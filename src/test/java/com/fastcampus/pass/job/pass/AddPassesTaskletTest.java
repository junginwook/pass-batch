package com.fastcampus.pass.job.pass;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fastcampus.pass.repository.pass.BulkPassEntity;
import com.fastcampus.pass.repository.pass.BulkPassRepository;
import com.fastcampus.pass.repository.pass.BulkPassStatus;
import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassRepository;
import com.fastcampus.pass.repository.pass.PassStatus;
import com.fastcampus.pass.repository.user.UserGroupMappingEntity;
import com.fastcampus.pass.repository.user.UserGroupMappingRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AddPassesTaskletTest {

	@Mock
	private StepContribution stepContribution;

	@Mock
	private ChunkContext chunkContext;

	@Mock
	private PassRepository passRepository;

	@Mock
	private BulkPassRepository bulkPassRepository;

	@Mock
	private UserGroupMappingRepository userGroupMappingRepository;

	@InjectMocks
	private AddPassesTasklet addPassesTasklet;

	@Test
	public void test_execute() throws Exception {
		//given
		final String userGroupId = "GROUP";
		final String userId = "A10000000";
		final Integer packageSeq = 1;
		final Integer count = 10;

		final LocalDateTime now = LocalDateTime.now();

		final BulkPassEntity bulkPassEntity = new BulkPassEntity();
		bulkPassEntity.setPackageSeq(packageSeq);
		bulkPassEntity.setUserGroupId(userGroupId);
		bulkPassEntity.setStatus(BulkPassStatus.READY);
		bulkPassEntity.setCount(count);
		bulkPassEntity.setStartedAt(now);
		bulkPassEntity.setEndedAt(now.plusDays(60));

		final UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
		userGroupMappingEntity.setUserGroupId(userGroupId);
		userGroupMappingEntity.setUserId(userId);

		//when
		given(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any(LocalDateTime.class))).willReturn(List.of(bulkPassEntity));
		given(userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())).willReturn(List.of(userGroupMappingEntity));
		RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);

		//then
		assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);
		ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);
		verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture());
		final List<PassEntity> passEntities = passEntitiesCaptor.getValue();

		assertThat(passEntities).hasSize(1);
		final PassEntity passEntity = passEntities.get(0);
		assertThat(passEntity.getPackageSeq()).isEqualTo(packageSeq);
		assertThat(passEntity.getUserId()).isEqualTo(userId);
		assertThat(passEntity.getRemainingCount()).isEqualTo(count);
		assertThat(passEntity.getStatus()).isEqualTo(PassStatus.READY);

		then(bulkPassRepository).should().findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any(LocalDateTime.class));
		then(userGroupMappingRepository).should().findByUserGroupId(bulkPassEntity.getUserGroupId());
	}
}