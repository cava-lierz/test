package com.mentara.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mentara.entity.PostTag;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

}