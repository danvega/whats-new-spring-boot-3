package dev.danvega.boot.repository;

import dev.danvega.boot.model.Post;
import org.springframework.data.repository.ListCrudRepository;

public interface PostRepository extends ListCrudRepository<Post,Integer> {

}
