package com.example.kuwalog;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// DB接続が必要なため、ローカルでPostgreSQLが起動している環境でのみ実行する
@Disabled("DB接続が必要。ローカル起動時に手動で実行すること")
@SpringBootTest
class KuwalogApplicationTests {

    @Test
    void contextLoads() {
    }
}
