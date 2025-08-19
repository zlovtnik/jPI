package com.churchapp.repository;

import com.churchapp.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Integer> {
    
    List<Family> findByFamilyNameContainingIgnoreCase(String familyName);
    
    List<Family> findByCity(String city);
    
    List<Family> findByState(String state);
    
    @Query("SELECT f FROM Family f WHERE f.zipCode = :zipCode")
    List<Family> findFamiliesByZipCode(@Param("zipCode") String zipCode);
    
    @Query("SELECT COUNT(f) FROM Family f")
    Long getTotalFamilyCount();
}
