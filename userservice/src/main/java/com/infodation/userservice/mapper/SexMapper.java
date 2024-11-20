package com.infodation.userservice.mapper;

import com.infodation.userservice.models.Sex;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper
public interface SexMapper {

    @Named("sexToEnum")
    default Sex mapSexToEnum(String sex) {
        return Sex.valueOf(sex.toUpperCase());
    }
}
