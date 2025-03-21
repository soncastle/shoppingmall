package com.shoppingmall.board.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingmall.board.model.Comment;
import com.shoppingmall.board.repository.BoardRepository;
import com.shoppingmall.board.repository.CommentRepository;
import com.shoppingmall.user.model.User;

@Service
public class CommentService {
	@Autowired
	CommentRepository repository;
	BoardRepository boardRepository;
	String toggle = "minus";
	
	//댓글 조회
	public List<Comment> getComment(Long boardId) {
		return repository.findByBoard_BoardIdOrderByCommentId(boardId);
	}
	
	//댓글 등록
	public void saveComment(Comment comment) {
		repository.save(comment);
	}
	
	//id 검색
	public Comment getCommentById(Long commentId) {
		Comment comment = repository.findById(commentId).orElse(null);
	    return repository.save(comment);
	}
	
	//게시글 좋아요
	public void likeComment(Long commentId, User user) {
		Comment comment = repository.findById(commentId).orElse(null);
		Set<Long> container = comment.getLikeContain();
		
		if(!container.contains(user.getId())) {
			container.add(user.getId());
			comment.setLikeContain(container);
			int likeCount = comment.getLikeCount();
			likeCount++;
			comment.setLikeCount(likeCount);
			repository.save(comment);
		}
		else {
			container.remove(user.getId());
			comment.setLikeContain(container);
			int likeCount = comment.getLikeCount();
			likeCount--;
			comment.setLikeCount(likeCount);
			repository.save(comment);
		}
	}
	
	//댓글 삭제
	public void deleteComment(Long commentId) {
		repository.deleteById(commentId);
	}
	
	//댓글 수
	public int countComment(Long boardId) {
		return repository.countByBoardBoardId(boardId);
	}
}
