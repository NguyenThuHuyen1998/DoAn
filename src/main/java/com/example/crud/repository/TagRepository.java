package com.example.crud.repository;

import com.example.crud.entity.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {
    @Query("select t from Tag t where t.tagName = :tagName")
    Tag getTagByTagName(@Param("tagName") String tagName);
}
