package com.shoppingmall.board.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "board")
@NoArgsConstructor
@AllArgsConstructor
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long boardId;
	
	private String userId;
	
	private String nickname;
	
	@Column(nullable = false, length = 20)
	private String title;
	
	@Column(nullable = false)
	private String content;
	
	private String hashtag;
	
	private int commentCount = 0;
	
	private int viewCount = 0;
	
	@Column(length = 1000)
	private List<Long> viewContain = new ArrayList<Long>();
	
	private int likeCount = 0;
	
	@Column(length = 1000)
	private List<Long> likeContain = new ArrayList<Long>();
	
	@CreationTimestamp
    @Column(updatable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false)
	private String categoryId;
	
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments;
}
