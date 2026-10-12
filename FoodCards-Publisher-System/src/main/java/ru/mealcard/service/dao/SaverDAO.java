package ru.mealcard.service.dao;

import lombok.RequiredArgsConstructor;
import ru.mealcard.exception.DatabaseSaveException;
import ru.mealcard.models.gru.GruVistaTab;
import ru.mealcard.models.pom.PomFile;
import ru.mealcard.models.pom.PomUnit;
import ru.mealcard.models.pom.PomUnitError;
import ru.mealcard.repository.gru.GruVistaTabRepository;
import ru.mealcard.repository.pom.PomFileRepository;
import ru.mealcard.repository.pom.PomUnitErrorRepository;
import ru.mealcard.repository.pom.PomUnitRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaverDAO {
    private final PomUnitRepository pomUnitRepository;
    private final PomUnitErrorRepository pomUnitErrorRepository;
    private final PomFileRepository pomFileRepository;
    private final GruVistaTabRepository gruVistaTabRepository;

    @Transactional("pomTransactionManager")
    public PomFile savePomFile(PomFile pomFile) {
        try {
            return pomFileRepository.save(pomFile);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomFile", e);
        }
    }

    @Transactional("gruTransactionManager")
    public void saveGRU(GruVistaTab gru) {
        try {
            gruVistaTabRepository.save(gru);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("GruVistaTab", e);
        }
    }

    @Transactional("pomTransactionManager")
    public void saveError(PomUnitError error) {
        try {
            pomUnitErrorRepository.save(error);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomUnitError", e);
        }
    }

    @Transactional("pomTransactionManager")
    public PomUnit savePomUnit(PomUnit pomUnit) {
        try {
            return pomUnitRepository.saveAndFlush(pomUnit);
        } catch (DataAccessException e) {
            throw new DatabaseSaveException("PomUnit", e);
        }
    }
}
