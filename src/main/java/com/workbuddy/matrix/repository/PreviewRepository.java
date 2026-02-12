package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.Preview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreviewRepository extends JpaRepository<Preview,Long> {}
