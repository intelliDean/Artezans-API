package com.api.artezans.utils;

import com.api.artezans.exceptions.ArtezanException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqlScriptExecutor {

    private final DataSource dataSource;

    //when this method is called with a path of the sql script, this method runs the script
    public void executeScript(String scriptLocation) {  //example of scriptLocation is: script/test_script.sql
        ClassPathResource classPathResource = new ClassPathResource(scriptLocation);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            ScriptUtils.executeSqlScript(connection, classPathResource);
        } catch (SQLException e) {
            throw new ArtezanException("invalid script location: " + scriptLocation);
        }
    }
}