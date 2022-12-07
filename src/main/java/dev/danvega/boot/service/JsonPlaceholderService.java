package dev.danvega.boot.service;

import dev.danvega.boot.model.Post;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface JsonPlaceholderService {

    @GetExchange("/posts")
    List<Post> loadPosts();

}
