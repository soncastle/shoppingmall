package com.shoppingmall.board.model;

import com.shoppingmall.board.dto.BoardResponseDTO;
import com.shoppingmall.user.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    @Column(nullable = true, length = 50)
    private String image;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String hashtag;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Lob
    @Column(name = "view_contain")
    private Set<Long> viewContain = new HashSet<Long>();

    @Lob
    @Column(name = "like_contain")
    private Set<Long> likeContain = new HashSet<Long>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "category_id", length = 20)
    private String categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public BoardResponseDTO toDTO(){
        return BoardResponseDTO.builder()
                .boardId(boardId)
                .title(title)
                .categoryId(categoryId)
                .nickname(nickname)
                .content(content)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .createAt(TimeUtils.getTimeAgo(LocalDate.from(createdAt)))
                .build();
    }
}