package com.eraf.test.builder;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestBuilder 테스트
 */
class TestBuilderTest {

    @Test
    void of_ShouldCreateBuilder() {
        // when
        TestBuilder<TestUser> builder = TestBuilder.of(TestUser::new);

        // then
        assertThat(builder).isNotNull();
    }

    @Test
    void build_WithNoSetters_ShouldCreateDefaultObject() {
        // when
        TestUser user = TestBuilder.of(TestUser::new).build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void with_ShouldSetSingleProperty() {
        // when
        TestUser user = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "John")
                .build();

        // then
        assertThat(user.getName()).isEqualTo("John");
    }

    @Test
    void with_ShouldSetMultipleProperties() {
        // when
        TestUser user = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "John")
                .with(TestUser::setEmail, "john@example.com")
                .with(TestUser::setAge, 30)
                .build();

        // then
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getAge()).isEqualTo(30);
    }

    @Test
    void with_ShouldChainMultipleCalls() {
        // when
        TestUser user = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "Alice")
                .with(TestUser::setEmail, "alice@example.com")
                .with(TestUser::setAge, 25)
                .with(TestUser::setActive, true)
                .build();

        // then
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getAge()).isEqualTo(25);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void buildList_ShouldCreateListOfObjects() {
        // when
        List<TestUser> users = TestBuilder.of(TestUser::new)
                .with(TestUser::setActive, true)
                .buildList(5);

        // then
        assertThat(users).hasSize(5);
        assertThat(users).allMatch(TestUser::isActive);
    }

    @Test
    void buildList_WithZeroCount_ShouldReturnEmptyList() {
        // when
        List<TestUser> users = TestBuilder.of(TestUser::new)
                .buildList(0);

        // then
        assertThat(users).isEmpty();
    }

    @Test
    void buildList_ShouldCreateIndependentInstances() {
        // when
        List<TestUser> users = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "SameName")
                .buildList(3);

        // Modify one object - others should NOT be affected
        users.get(0).setName("DifferentName");

        // then - each element is an independent instance
        assertThat(users.get(0).getName()).isEqualTo("DifferentName");
        assertThat(users.get(1).getName()).isEqualTo("SameName");
        assertThat(users.get(2).getName()).isEqualTo("SameName");
        assertThat(users.get(0)).isNotSameAs(users.get(1));
        assertThat(users.get(1)).isNotSameAs(users.get(2));
    }

    @Test
    void with_ShouldWorkWithComplexTypes() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<String> roles = List.of("USER", "ADMIN");

        // when
        TestUser user = TestBuilder.of(TestUser::new)
                .with(TestUser::setCreatedAt, now)
                .with(TestUser::setRoles, roles)
                .build();

        // then
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getRoles()).containsExactly("USER", "ADMIN");
    }

    @Test
    void with_ShouldOverridePreviousValue() {
        // when
        TestUser user = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "FirstName")
                .with(TestUser::setName, "SecondName")
                .build();

        // then
        assertThat(user.getName()).isEqualTo("SecondName");
    }

    @Test
    void build_ShouldReturnNewInstanceEachTime() {
        // given
        TestBuilder<TestUser> builder = TestBuilder.of(TestUser::new)
                .with(TestUser::setName, "John");

        // when
        TestUser user1 = builder.build();
        TestUser user2 = builder.build();

        // then - each build() creates a new instance with same configuration
        assertThat(user1).isNotSameAs(user2);
        assertThat(user1.getName()).isEqualTo(user2.getName());
    }

    // Test helper class
    static class TestUser {
        private String name;
        private String email;
        private Integer age;
        private boolean active;
        private LocalDateTime createdAt;
        private List<String> roles = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
