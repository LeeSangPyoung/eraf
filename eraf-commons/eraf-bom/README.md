# eraf-bom

ERAF Commons Bill of Materials (BOM) 모듈입니다.

## 용도

모든 ERAF 모듈의 버전을 중앙에서 관리합니다. 소비자 프로젝트에서 BOM을 import하면 개별 모듈의 버전을 명시할 필요가 없습니다.

## 사용법

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

이후 개별 모듈을 버전 없이 추가:

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-core</artifactId>
</dependency>
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-web</artifactId>
</dependency>
```

## 등록 모듈 (46개)

Core(12), Web(5), Data(6), Messaging(2), Integration(6), Processing(7), Observability(2), Document & Media(5), Test(1)
