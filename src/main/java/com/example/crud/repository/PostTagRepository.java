package com.example.crud.repository;

import com.example.crud.entity.PostTag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends PagingAndSortingRepository<PostTag, Long> {
//    @Query("select t from PostTag t left join fetch t.news tcc where tcc.newsId =:newsId")
//    List<PostTag> getListPostTagByNews(@Param("news-id") long newsId);
//    t left join fetch t.user tcc where tcc.userId = :userId
}
