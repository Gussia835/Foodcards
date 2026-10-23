package foodcards.adapter.mapper;

import foodcards.adapter.models.AppAdapterConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppAdapterConfigMapper {
    List<AppAdapterConfigEntity> findAll();

    AppAdapterConfigEntity findById(@Param("id") Long id);

    void insert(AppAdapterConfigEntity config);
}
