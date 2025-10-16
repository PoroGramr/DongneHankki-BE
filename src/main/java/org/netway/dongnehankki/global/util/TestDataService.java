package org.netway.dongnehankki.global.util;

import lombok.RequiredArgsConstructor;
import org.netway.dongnehankki.post.domain.Hashtag;
import org.netway.dongnehankki.post.domain.Post;
import org.netway.dongnehankki.post.domain.PostLike;
import org.netway.dongnehankki.post.repository.HashtagRepository;
import org.netway.dongnehankki.post.repository.PostLikeRepository;
import org.netway.dongnehankki.post.repository.PostRepository;
import org.netway.dongnehankki.store.domain.Store;
import org.netway.dongnehankki.store.infrastructure.repository.StoreRepository;
import org.netway.dongnehankki.user.domain.User;
import org.netway.dongnehankki.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TestDataService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final HashtagRepository hashtagRepository;

    @Transactional
    public void generatePosts(int count) {
        User likerUser = userRepository.findByLoginId("liker_user")
                .orElseGet(() -> userRepository.save(User.ofCustomer("liker_user", "password", "liker_nickname", "liker_name", "01087654321", LocalDate.now())));

        List<User> randomUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String loginId = "testuser" + i;
            int index = i;
            User user = userRepository.findByLoginId(loginId)
                    .orElseGet(() -> userRepository.save(User.ofCustomer(loginId, "password", "testnickname" + index, "testname" + index, "0101234567" + index, LocalDate.now())));
            randomUsers.add(user);
        }

        List<Store> stores = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final String storeName = "test store" + i;
            int index = i;
            Store store = storeRepository.findByName(storeName)
                    .orElseGet(() -> storeRepository.save(Store.createStore(storeName, 37.5 + (
                            index * 0.01), 127.5 + (index * 0.01), "test address" + index, "test sigun" + index,
                        index, 123456789L + index)));
            stores.add(store);
        }

        List<Hashtag> hashtags = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final String tagName = "tag" + i;
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> hashtagRepository.save(Hashtag.createHashtag(tagName)));
            hashtags.add(hashtag);
        }

        Random random = new Random();
        String[] adjectives = {"맛있는", "최고의", "환상적인", "가성비 좋은", "분위기 있는", "특별한"};
        String[] nouns = {"맛집", "한끼", "식사", "경험", "장소", "추억"};
        List<Post> posts = new ArrayList<>();
        long baseCount = postRepository.count();
        for (int i = 0; i < count; i++) {
            User randomUser = randomUsers.get(random.nextInt(randomUsers.size()));
            Store randomStore = stores.get(random.nextInt(stores.size()));
            String content = adjectives[random.nextInt(adjectives.length)] + " " + nouns[random.nextInt(nouns.length)] + " #" + (baseCount + i);
            Post post = Post.createPost(content, randomStore, randomUser, Post.Role.CUSTOMER);
            post.addImage("https://postfiles.pstatic.net/MjAyNTEwMTJfMTY5/MDAxNzYwMjUzNTAyMTI1.OhFHru_gdTBu-lDk2ZZW-A95T_tg3QSJhbrszIRvqsgg.6rEgtwtdv9bvF0lmfMCsiyV60paopVl20lG7GWnCYMsg.JPEG/IMG%EF%BC%BF6787.JPG?type=w3840", 0);

            Collections.shuffle(hashtags);
            int numHashtags = 2 + random.nextInt(2);
            for (int j = 0; j < numHashtags; j++) {
                post.addPostHashtag(hashtags.get(j));
            }
            posts.add(post);
        }
        postRepository.saveAll(posts);

        List<PostLike> postLikes = new ArrayList<>();
        int likesCount = Math.min(count, 5000);
        for (int i = 0; i < likesCount; i++) {
            Post postToLike = posts.get(i);
            if (!postLikeRepository.existsByUser_UserIdAndPost_PostId(likerUser.getUserId(), postToLike.getPostId())) {
                PostLike postLike = PostLike.of(likerUser, postToLike);
                postLikes.add(postLike);
            }
        }
        postLikeRepository.saveAll(postLikes);
    }
}
