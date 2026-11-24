package ru.mealcard.service.processing;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.mealcard.models.AppChangerTransEntity;
import ru.mealcard.repository.TransRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.mealcard.utils.Constants.STATUS_NEW;

@ApplicationScoped
public class StuckTransactionService {

    @Inject
    TransRepository transRepo;

    @Scheduled(every = "${app.stuck-check.interval:5m}")
    @Transactional
    public void resetStuckTransactions() {
        List<AppChangerTransEntity> stuck = transRepo.findStuckTransactions(10);

        if (stuck.isEmpty()) {
            return;
        }

        Log.warn("Found " + stuck.size() + " stuck transactions, resetting to NEW");

        for (AppChangerTransEntity trans : stuck) {
            trans.setStatus(STATUS_NEW);
            trans.setUpdTs(LocalDateTime.now());
        }
    }
}