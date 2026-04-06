package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@ActiveProfiles("test")
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Commit
class bulkinsertfivemillionusers {

    // 생성할 총 유저 수
    private static final int TOTAL_COUNT = 5000000;

    // 한 번에 insert 할 batch 크기
    private static final int BATCH_SIZE = 10_000;

    // 기본 비밀번호 (예시)
    private static final String DEFAULT_PASSWORD = "encoded_password";

    // 기본 권한 (예시)
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bulkInsertUsers() {
        long startedAt = System.currentTimeMillis();

        for (int start = 0; start < TOTAL_COUNT; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_COUNT - start);
            int batchStart = start;

            jdbcTemplate.batchUpdate(
                    """
                            INSERT INTO users (email, nickname, password, user_role, created_at, modified_at)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            long sequence = (long) batchStart + index + 1;
                            String nickname = generateNickname(sequence);
                            LocalDateTime now = LocalDateTime.now();

                            ps.setString(1, "bulk_user_" + sequence + "@example.com");
                            ps.setString(2, nickname);
                            ps.setString(3, DEFAULT_PASSWORD);
                            ps.setString(4, DEFAULT_ROLE);
                            ps.setObject(5, now);
                            ps.setObject(6, now);
                        }

                        @Override
                        public int getBatchSize() {
                            return currentBatchSize;
                        }
                    }
            );

            int insertedCount = batchStart + currentBatchSize;
            System.out.println("insert 진행: " + insertedCount + " / " + TOTAL_COUNT);
        }

        long elapsedMillis = System.currentTimeMillis() - startedAt;
        System.out.println("bulk insert 완료, 총 소요 시간(ms): " + elapsedMillis);
    }

    /**
     * 닉네임 생성
     * - sequence를 포함하여 중복을 방지
     * - 랜덤 prefix를 붙여 랜덤성 추가
     */
    private String generateNickname(long sequence) {
        String randomPrefix = Long.toString(
                ThreadLocalRandom.current().nextLong(36L * 36L * 36L * 36L), 36
        );

        return "user_" + padLeft(randomPrefix, 4) + "_" + Long.toString(sequence, 36);
    }

    /**
     * 랜덤 prefix 길이가 부족하면 앞을 0으로 채움
     */
    private String padLeft(String value, int targetLength) {
        if (value.length() >= targetLength) {
            return value;
        }

        StringBuilder builder = new StringBuilder(targetLength);
        for (int i = value.length(); i < targetLength; i++) {
            builder.append('0');
        }
        builder.append(value);
        return builder.toString();
    }

    @Test
    @Commit
    void insertTargetUsers() {

        int TARGET_COUNT = 1000;

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO users (email, nickname, password, user_role, created_at, modified_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {

                        String email = "target_user_" + i + "@example.com";
                        String nickname = "target_user";
                        LocalDateTime now = LocalDateTime.now();

                        ps.setString(1, email);
                        ps.setString(2, nickname);
                        ps.setString(3, "encoded_password");
                        ps.setString(4, "USER");
                        ps.setObject(5, now);
                        ps.setObject(6, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return TARGET_COUNT;
                    }
                }
        );
    }
}
