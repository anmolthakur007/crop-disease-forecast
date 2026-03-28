package com.cropsentinel.backend_api.repository;

import com.cropsentinel.backend_api.model.ForecastHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface  ForecastRepository extends JpaRepository<ForecastHistory, Long> {
    List<ForecastHistory> findAllByOrderByCreatedAtDesc();
}
