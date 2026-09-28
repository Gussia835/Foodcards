package foodcards.adapter.mapper;

import foodcards.adapter.AppAdapterApplication;
import foodcards.adapter.models.AppAdapterConfig;
import foodcards.adapter.models.AppAdapterTrans;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppAdapterTransMapper {
    void insert(AppAdapterTrans trans);
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    AppAdapterTrans findByRequestId(@Param("requestId") String requestId);

}
