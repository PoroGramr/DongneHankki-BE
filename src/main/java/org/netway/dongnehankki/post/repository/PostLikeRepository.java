package org.netway.dongnehankki.post.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.netway.dongnehankki.post.domain.PostLike;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUser_UserIdAndPost_PostId(Long userId, Long postId);
    boolean existsByUser_UserIdAndPost_PostId(Long userId, Long postId);

    @EntityGraph(attributePaths = {"post", "post.postHashtags", "post.postHashtags.hashtag"})
    List<PostLike> findByUser_UserId(Long userId);

    List<PostLike> findByUser_UserIdAndPost_PostIdIn(Long userId, List<Long> postIds);

}
