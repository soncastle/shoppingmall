package com.shoppingmall.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shoppingmall.board.model.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>{
	//전체 최신순 정렬
	Page<Board> findAllByOrderByBoardIdDesc(Pageable pageable);
	
	//키워드 검색
	@Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword% OR b.userId LIKE %:keyword% ORDER BY boardId DESC")
	Page<Board> searchBoards(@Param("keyword") String keyword, Pageable pageable);
	
	
}
