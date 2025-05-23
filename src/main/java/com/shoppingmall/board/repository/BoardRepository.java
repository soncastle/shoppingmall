package com.shoppingmall.board.repository;

import com.shoppingmall.user.model.UserRoleType;
import java.time.LocalDateTime;
import java.util.List;

import com.shoppingmall.board.dto.BoardResponseDTO;
import com.shoppingmall.board.model.Comment;
import com.shoppingmall.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shoppingmall.board.model.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>{
	//키워드 검색
	@Query("SELECT b FROM Board b WHERE " +
		       "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "OR LOWER(b.user.userId) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
		       "AND (:category = '' OR b.categoryId = :category) " +
		       "AND (:bydate = '전체' OR b.createdAt >= :startDate) " +
		       "ORDER BY CASE WHEN :order = '인기순' THEN b.likeCount ELSE 0 END DESC, " +
		       "b.createdAt DESC")
	List<Board> searchBoards(@Param("keyword") String keyword, @Param("category") String category, 
			@Param("order") String order, @Param("bydate") String bydate, @Param("startDate") LocalDateTime startDate);
	
  // 전체 최신순 정렬
  Page<Board> findAllByOrderByBoardIdDesc(Pageable pageable);

  List<Board> findTop9ByOrderByCreatedAtDesc();

  List<Board> findTop9ByOrderByLikeCountDesc();

  @Query("SELECT b FROM Board b ORDER BY b.likeCount DESC")
  Page<Board> findAllSortedByLikes(Pageable pageable);

  @Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword%")
  List<Board> searchByKeyword(String keyword);

  List<Board> findTop9ByOrderByViewCountDesc();

  Page<Board> findBoardByUser(User user, Pageable pageable);

  int countByUser(User user);

    // 사용자가 댓글 단 게시물 수 조회
  @Query("SELECT COUNT(DISTINCT c.board) FROM Comment c WHERE c.user = :user")
  int countDistinctBoardsByCommentUser(@Param("user") User user);

  List<Board> findTop9ByCategoryIdOrderByCreatedAtDesc(String 공지);
  Page<Board> findByUserRole(UserRoleType role, Pageable pageable);
  Page<Board> findByUserRoleAndCreatedAtAfter(UserRoleType role, LocalDateTime startDate, Pageable pageable);
  // 카테고리 + 역할 필터링 메서드
  Page<Board> findByCategoryIdAndUserRole(String category, UserRoleType role, Pageable pageable);
  Page<Board> findByCategoryIdAndUserRoleAndCreatedAtAfter(String category, UserRoleType role, LocalDateTime startDate, Pageable pageable);
  // 검색 + 역할 필터링 메서드
  Page<Board> findByUserRoleAndCreatedAtAfterAndTitleContaining(UserRoleType role, LocalDateTime startDate, String search, Pageable pageable);
  Page<Board> findByCategoryIdAndUserRoleAndCreatedAtAfterAndTitleContaining(String category, UserRoleType role, LocalDateTime startDate, String search, Pageable pageable);


    Page<Board> findAll(Specification<Board> spec, Pageable pageable);
}
