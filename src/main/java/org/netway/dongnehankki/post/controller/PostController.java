package org.netway.dongnehankki.post.controller;

import org.netway.dongnehankki.post.dto.request.PostCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post") // 3. URL 경로는 @RequestMapping으로 지정
public class PostController {

    @PostMapping("/create")
    public ResponseEntity<Void> createPost(@RequestBody PostCreateRequest request) {
        // TODO : 게시글 생성 로직
        System.out.println("Controller received content: " + request.getContent());
        System.out.println("Controller received image URLs: " + request.getImageUrls());
        System.out.println("Controller received hashtags: " + request.getHashtags());
        return ResponseEntity.ok().build();
    }

}
