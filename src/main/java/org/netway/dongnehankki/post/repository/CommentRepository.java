package org.netway.dongnehankki.post.repository;

import org.netway.dongnehankki.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostId(Long postId);
    int countByPost_PostId(Long postId);

    @Query("SELECT c.post.postId, COUNT(c.id) FROM Comment c WHERE c.post.postId IN :postIds GROUP BY c.post.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
