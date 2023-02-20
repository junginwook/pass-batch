package com.fastcampus.pass.repository.booking;

import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.user.UserEntity;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "booking")
public class BookingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bookingSeq;
	private Integer passSeq;
	private String userId;

	@Enumerated(EnumType.STRING)
	private BookingStatus status;
	private boolean usedPass;
	private boolean attended;

	private LocalDateTime startedAt;
	private LocalDateTime endedAt;
	private LocalDateTime cancelledAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private UserEntity userEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "passSeq", insertable = false, updatable = false)
	private PassEntity passEntity;

	public LocalDateTime getStatisticsAt() {
		return this.endedAt.withHour(0).withMinute(0).withSecond(0).withNano(0);
	}

}
