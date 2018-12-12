package ru.relex.techtalks.async.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ru.relex.techtalks.async.model.Patient;

@Mapper
public interface IPatientMapper {

  @Select(
      // language=PostgreSQL
      "SELECT id AS id, \n" +
              "       first_name AS firstName, \n" +
              "       last_name AS lastName,\n" +
              "       to_char(dob, 'MM-DD-YYYY') AS dateOfBirth,\n" +
              "       ext_id AS extId\n" +
              "FROM users\n" +
          "WHERE id = ${id}")
  Patient getById(@Param("id") long id);
}
