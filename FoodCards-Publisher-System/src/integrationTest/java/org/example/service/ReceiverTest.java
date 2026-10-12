package org.example.service;

import ru.mealcard.builders.EnrollBuilder;
import ru.mealcard.models.gru.GruVistaTab;
import ru.mealcard.models.pom.PomFile;
import ru.mealcard.models.pom.PomUnit;
import ru.mealcard.repository.gru.GruVistaTabRepository;
import ru.mealcard.repository.pom.PomFileRepository;
import ru.mealcard.repository.pom.PomUnitRepository;
import ru.mealcard.service.FileReceiverService;
import ru.mealcard.service.dao.SaverDAO;
import ru.mealcard.service.impl.visitor.EnrollParserVisitor;
import ru.mealcard.service.impl.visitor.validator.EnrollValidator;
import ru.mealcard.utils.filename.FileManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        FileReceiverService.class,
        EnrollValidator.class,
        EnrollParserVisitor.class,
        EnrollBuilder.class,
        SaverDAO.class,
        ReceiverTest.TestConfig.class
})
class ReceiverTest {

    @TestConfiguration
    static class TestConfig {

        @Bean(name = {"transactionManager", "pomTransactionManager", "gruTransactionManager"})
        public PlatformTransactionManager transactionManager(
                @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.hbm2ddl.create_namespaces", () -> "true");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Autowired
    private FileReceiverService service;

    @Autowired
    private PomFileRepository pomFileRepository;

    @Autowired
    private PomUnitRepository pomUnitRepository;

    @Autowired
    private GruVistaTabRepository gruVistaTabRepository;

    @MockBean
    private FileManager fileManager;

    @TempDir
    Path tempDir;

    private static final Charset WIN_1251 = Charset.forName("windows-1251");
    private static final String VALID_FILENAME = "Z001032.GLAER_ENROLL0010321.249";
    private static final String INVALID_FILENAME = "Z001032.GLAER_ENROLL0010321.250";
    private static final String VALID_CONTENT = "H 20260907 182606 IMMEDIATE               \r\n" +
            "Кузнецов Олег                                                                                       1000401000050003              DR                 777\r\n" +
            "T                  1\r\n";
    private static final String INVALID_CONTENT = "X 20260907 182606 INVALID                 \r\n" +
            "Кузнецов Олег                                                                                       1000401000050003              DR                 777\r\n" +
            "T                  1\r\n";

    @Nested
    class SuccessTests {
        @Test
        void processValidFileAndSaveToAllTablesTest() throws Exception {
            Path testFile = tempDir.resolve(VALID_FILENAME);
            Files.writeString(testFile, VALID_CONTENT, WIN_1251);
            when(fileManager.moveToInProgress(any())).thenReturn(testFile);
            doNothing().when(fileManager).moveToFileResult(any(), anyBoolean());

            service.processFile(testFile);

            Optional<PomFile> savedFile = pomFileRepository.findByFilename(VALID_FILENAME);
            assertThat(savedFile).isPresent();
            assertThat(savedFile.get().getFileStatus()).isEqualTo("SUCCESS");
            assertThat(savedFile.get().getUliDate()).isEqualTo("249");
            List<PomUnit> units = pomUnitRepository.findAll();
            assertThat(units).hasSizeGreaterThanOrEqualTo(3);

            List<GruVistaTab> gruRecords = gruVistaTabRepository.findAll();
            assertThat(gruRecords).hasSize(1);
            assertThat(gruRecords.get(0).getSystemAccount()).isEqualTo("1000401000050003");
        }
    }

    @Nested
    class ErrorTests {
        @Test
        void processInvalidFileAndMarkAsErrorTest() throws Exception {
            Path testFile = tempDir.resolve(INVALID_FILENAME);
            Files.writeString(testFile, INVALID_CONTENT, WIN_1251);
            when(fileManager.moveToInProgress(any())).thenReturn(testFile);
            doNothing().when(fileManager).moveToFileResult(any(), anyBoolean());

            service.processFile(testFile);

            Optional<PomFile> savedFile = pomFileRepository.findByFilename(INVALID_FILENAME);
            assertThat(savedFile).isPresent();
            assertThat(savedFile.get().getFileStatus()).isEqualTo("ERROR");
        }
    }
}