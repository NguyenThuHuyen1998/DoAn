package com.example.crud.repository;

import com.example.crud.entity.PostTag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends PagingAndSortingRepository<PostTag, Long> {
//    @Query("select t from PostTag ")
}
