package com.fastcampus.pass.repository.user;

import com.fastcampus.pass.repository.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Getter
@Setter
@Entity
@Table(name = "user")
@TypeDef(name = "json", typeClass = JsonType.class)
public class UserEntity extends BaseEntity {
	@Id
	private String userId;

	private String userName;
	@Enumerated(EnumType.STRING)
	private UserStatus status;
	private String phone;

	@Type(type = "json")
	private Map<String, Object> meta;

	public String getUuid() {
		String uuid = null;
		if (meta.containsKey("uuid")) {
			uuid = String.valueOf(meta.get("uuid"));
		}

		return uuid;
	}

}
