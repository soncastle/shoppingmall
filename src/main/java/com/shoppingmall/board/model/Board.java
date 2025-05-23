package com.shoppingmall.board.model;

import com.shoppingmall.board.dto.BoardResponseDTO;
import com.shoppingmall.board.dto.PostType;
import com.shoppingmall.user.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.shoppingmall.user.model.User;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "board")
@Getter @Setter
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(nullable = false, length = 50)
    private String title;
    
    @Column(nullable = true, length = 255)
    private String image;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private Set<String> hashtag;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Lob
    @Column(name = "view_contain")
    @Builder.Default
    private Set<Long> viewContain = new HashSet<Long>();

    @Lob
    @Column(name = "like_contain")
    @Builder.Default
    private Set<Long> likeContain = new HashSet<Long>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "category_id", length = 20)
    private String categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }


    public Board(String title, String image, String nickname, String content, Set<String> hashtag, String categoryId, User user , PostType postType) {
        this.title = title;
        this.image = image;
        this.nickname = nickname;
        this.content = content;
        this.hashtag = hashtag;
        this.categoryId = categoryId;
        this.user = user;
        this.postType = postType;
        this.commentCount = 0;  // 기본값
        this.viewCount = 0;     // 기본값
        this.likeCount = 0;     // 기본값
        this.viewContain = new HashSet<>();  // 기본값
        this.likeContain = new HashSet<>();  // 기본값
    }

    public BoardResponseDTO toDTO(){
        return BoardResponseDTO.builder()
                .boardId(boardId)
                .title(title)
                .categoryId(categoryId)
                .image(image)
                .nickname(nickname)
                .hashtag(hashtag)
                .content(content)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .createAt(TimeUtils.getTimeAgo(LocalDate.from(createdAt)))
                .build();
    }
}