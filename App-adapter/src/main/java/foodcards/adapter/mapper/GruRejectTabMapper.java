package foodcards.adapter.mapper;

import foodcards.adapter.models.GruRejectTabEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GruRejectTabMapper {
    void insert(GruRejectTabEntity rejectTab);

    void batchInsert(@Param("list") List<GruRejectTabEntity> list);
}
