package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.BaseEntity;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "pass")
public class PassEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer passSeq;

	private Integer packageSeq;

	private String userId;

	@Enumerated(EnumType.STRING)
	private PassStatus status;

	private Integer remainingCount;

	private LocalDateTime startedAt;

	private LocalDateTime endedAt;

	private LocalDateTime expiredAt;
}
