package com.visitcard.repository;
import com.visitcard.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    List<Company> findByAdmin_Id(Long adminId);
}
