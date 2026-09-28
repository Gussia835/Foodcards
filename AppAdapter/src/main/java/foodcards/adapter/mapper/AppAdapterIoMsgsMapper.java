package foodcards.adapter.mapper;

import foodcards.adapter.models.AppAdapterIoMsgs;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppAdapterIoMsgsMapper {
    void insert(AppAdapterIoMsgs ioMsgs);
}
