package org.netway.dongnehankki.user.domain;

import java.util.ArrayList;
import java.util.List;

import org.netway.dongnehankki.global.common.BaseEntity;
import org.netway.dongnehankki.follow.domain.Follow;
import org.netway.dongnehankki.post.domain.Post;
import org.netway.dongnehankki.store.domain.Store;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	private String id;

	private String password;

	private String nickname;

	@Enumerated(EnumType.STRING)
	private Role role;

	@OneToOne(optional = true)
	@JoinColumn(name = "store_id")
	private Store store;

	@OneToMany(mappedBy = "user")
	private List<Follow> follows = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<Post> posts = new ArrayList<>();

	public enum Role {
		OWNER, CUSTOMER, ADMIN
	}

	public static User ofCustomer(String id, String password, String nickname){
		return User.builder()
			.id(id)
			.password(password)
			.nickname(nickname)
			.role(Role.CUSTOMER)
			.build();
	}

	public static User ofOwner(String id, String password, String nickname, Store store){
		return User.builder()
			.id(id)
			.password(password)
			.nickname(nickname)
			.store(store)
			.role(Role.OWNER)
			.build();
	}
}
