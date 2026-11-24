package ru.mealcard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import ru.mealcard.models.AppChangerIoMsgsEntity;

import java.util.List;

@ApplicationScoped
public class IoMsgsRepository implements PanacheRepository<AppChangerIoMsgsEntity> {

    @Transactional
    public void save(AppChangerIoMsgsEntity ioMsgs) {
        persist(ioMsgs);
    }

    public List<AppChangerIoMsgsEntity> findByTransId(Long transId) {
        return list("transId", transId);
    }
}
