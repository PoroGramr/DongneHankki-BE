package org.netway.dongnehankki.post.repository;

import java.util.List;
import java.util.Optional;
import org.netway.dongnehankki.post.domain.Post;
import org.netway.dongnehankki.store.domain.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "store", "store.user", "images", "postHashtags", "postHashtags.hashtag"})
    Optional<Post> findById(Long postId);

    List<Post> findByStore_StoreIdAndPostIdLessThanOrderByPostIdDesc(Long storeId,
        Long cursorPostId, Pageable pageable);

    List<Post> findByStore_StoreIdOrderByPostIdDesc(Long storeId, Pageable pageable);

    List<Post> findByStore_StoreIdAndRoleAndPostIdLessThanOrderByPostIdDesc(Long storeId,
        Post.Role role, Long cursorPostId, Pageable pageable);

    List<Post> findByStore_StoreIdAndRoleOrderByPostIdDesc(Long storeId, Post.Role role,
        Pageable pageable);

    List<Post> findByStoreInAndPostIdLessThanOrderByPostIdDesc(List<Store> stores,
        Long cursorPostId, Pageable pageable);

    List<Post> findByStoreInOrderByPostIdDesc(List<Store> stores, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store", "store.user", "images", "postHashtags",
        "postHashtags.hashtag", "postLikes"})
    List<Post> findAllByOrderByPostIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "store", "store.user", "images", "postHashtags",
        "postHashtags.hashtag", "postLikes"})
    List<Post> findAllByPostIdLessThanOrderByPostIdDesc(Long cursorPostId, Pageable pageable);

    // --- 추천 API 최적화를 위한 메소드 ---

    // 콜드 스타트용: 최신 게시글 ID 조회
    @Query("SELECT p.postId FROM Post p ORDER BY p.createdAt DESC")
    List<Long> findTopPostIdsByOrderByCreatedAtDesc(Pageable pageable);

    // 해시태그 기반 추천: 관심 해시태그를 포함하는 게시글 ID 조회
    @Query("SELECT p.postId FROM Post p JOIN p.postHashtags ph WHERE ph.hashtag.name IN :hashtags AND p.postId NOT IN :excludePostIds GROUP BY p.postId ORDER BY MAX(p.createdAt) DESC")
    List<Long> findRecommendedPostIdsByHashtags(@Param("hashtags") List<String> hashtags,
        @Param("excludePostIds") List<Long> excludePostIds,
        Pageable pageable);

    // 인기 게시글 ID 조회: 좋아요 수 기준으로 정렬
    @Query("SELECT p.postId FROM Post p LEFT JOIN p.postLikes pl GROUP BY p.postId ORDER BY COUNT(pl.id) DESC")
    List<Long> findTopNPopularPostIds(Pageable pageable);

    // findAllById 오버라이드: 추천 로직의 2단계 조회를 위해 EntityGraph 적용
    @Override
    @EntityGraph(attributePaths = {"user", "store", "store.user", "images", "postHashtags", "postHashtags.hashtag", "postLikes"})
    List<Post> findAllById(Iterable<Long> ids);

    List<Post> findTop5ByStoreAndRoleOrderByCreatedAtDesc(Store store, Post.Role role);

}