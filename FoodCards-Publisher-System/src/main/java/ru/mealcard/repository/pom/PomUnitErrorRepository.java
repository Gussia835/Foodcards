package ru.mealcard.repository.pom;

import ru.mealcard.models.pom.PomUnitError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PomUnitErrorRepository extends JpaRepository<PomUnitError, Long> {


}
