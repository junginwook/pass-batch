package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.BulkPassEntity;
import com.fastcampus.pass.repository.pass.BulkPassStatus;
import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PassModeMapper {
	PassModeMapper INSTANCE = Mappers.getMapper(PassModeMapper.class);

	//필드값이 같지 않거나 custom하게 매핑해주기 위해서는 @Mapping을 추가해주면 됩니다.
	@Mapping(target = "status", qualifiedByName = "defaultStatus")
	@Mapping(target = "remainingCount", source = "bulkPassEntity.count")
	PassEntity toPassEntity(BulkPassEntity bulkPassEntity, String userId);

	@Named("defaultStatus")
	default PassStatus status(BulkPassStatus status) {
		return PassStatus.READY;
	}
}
