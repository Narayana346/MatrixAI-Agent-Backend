package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan,Long> {}
