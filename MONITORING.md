# Spring Boot 애플리케이션 모니터링 가이드

이 문서는 Prometheus와 Grafana를 사용한 Co's House 애플리케이션 모니터링 설정 및 확인 방법을 설명합니다.

## 설정 완료 항목

### 1. 의존성 추가 (build.gradle)
- `spring-boot-starter-actuator`: Spring Boot Actuator
- `micrometer-registry-prometheus`: Prometheus 메트릭 수집

### 2. Actuator 설정 (application.yml)
- Actuator endpoints 활성화: health, info, metrics, prometheus
- Prometheus 메트릭 엔드포인트: `/actuator/prometheus`
- HTTP 요청 히스토그램 활성화

### 3. Security 설정 (SecurityConfig.java)
- `/actuator/**` 경로 public 접근 허용

### 4. Docker Compose 설정
- Prometheus 컨테이너 (포트: 9090)
- Grafana 컨테이너 (포트: 3000)
- MySQL 컨테이너 (기존)

---

## 시작하기

### 1. 애플리케이션 및 모니터링 스택 시작

```bash
# 1. Docker Compose로 Prometheus, Grafana, MySQL 시작
docker-compose up -d

# 2. 컨테이너 상태 확인
docker ps

# 예상 출력:
# CONTAINER ID   IMAGE                   PORTS                      NAMES
# ...            prom/prometheus         0.0.0.0:9090->9090/tcp     cos-prometheus
# ...            grafana/grafana         0.0.0.0:3000->3000/tcp     cos-grafana
# ...            mysql:8.4               0.0.0.0:13306->3306/tcp    cos-mysql

# 3. Spring Boot 애플리케이션 시작
./gradlew bootRun
```

---

## 모니터링 확인 방법

### 1. Spring Boot Actuator 확인

#### Health Check
```bash
# 브라우저 또는 curl로 접속
curl http://localhost:8080/actuator/health

# 예상 응답:
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### Metrics 확인
```bash
# 사용 가능한 메트릭 목록
curl http://localhost:8080/actuator/metrics

# 특정 메트릭 조회 (예: JVM 메모리 사용량)
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

#### Prometheus 메트릭 엔드포인트
```bash
# Prometheus 형식의 모든 메트릭 조회
curl http://localhost:8080/actuator/prometheus

# 예상 응답 (일부):
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 1.2345678E7
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",uri="/",} 42.0
```

---

### 2. Prometheus 확인

#### Prometheus UI 접속
1. 브라우저에서 접속: **http://localhost:9090**
2. 상단 메뉴에서 **Status → Targets** 클릭
3. `spring-boot-app` job이 **UP** 상태인지 확인

#### 메트릭 쿼리 테스트
1. 메인 페이지 (Graph) 에서 쿼리 입력:
   ```promql
   # JVM 메모리 사용량
   jvm_memory_used_bytes

   # HTTP 요청 수
   http_server_requests_seconds_count

   # HTTP 요청 평균 응답 시간 (초)
   rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

   # CPU 사용률
   system_cpu_usage

   # 데이터베이스 커넥션 활성
   hikaricp_connections_active
   ```

2. **Execute** 버튼 클릭하여 결과 확인
3. **Graph** 탭에서 시각화 확인

---

### 3. Grafana 대시보드 설정

#### 초기 로그인
1. 브라우저에서 접속: **http://localhost:3000**
2. 로그인 정보:
   - **Username**: `admin`
   - **Password**: `admin`
3. 비밀번호 변경 요청 시 Skip 가능 (개발 환경)

#### Prometheus 데이터 소스 추가
1. 좌측 메뉴 **Configuration (톱니바퀴 아이콘) → Data sources** 클릭
2. **Add data source** 클릭
3. **Prometheus** 선택
4. 설정:
   - **Name**: `Prometheus`
   - **URL**: `http://prometheus:9090` (Docker 네트워크 내부 접근)
5. 하단 **Save & test** 클릭
6. "Data source is working" 메시지 확인

#### Spring Boot 대시보드 임포트
1. 좌측 메뉴 **Dashboards (네모 4개 아이콘) → Import** 클릭
2. **Import via grafana.com** 필드에 입력: `4701`
   - JVM (Micrometer) 대시보드
3. **Load** 클릭
4. 설정:
   - **Prometheus data source**: 방금 추가한 Prometheus 선택
   - **application**: `cos-house` 입력 (application.yml에 설정한 이름)
5. **Import** 클릭

#### 추가 대시보드 (선택사항)
```
대시보드 ID | 설명
-----------|-----
11378      | JVM (Micrometer) - 상세 JVM 메트릭
12900      | Spring Boot 2.1+ Statistics
10280      | Spring Boot Statistics
```

---

## 주요 모니터링 메트릭

### 1. 애플리케이션 성능
- **http_server_requests_seconds**: HTTP 요청 응답 시간 및 처리량
- **http_server_requests_seconds_count**: HTTP 요청 총 수
- **http_server_requests_seconds_sum**: HTTP 요청 총 처리 시간

### 2. JVM 메트릭
- **jvm_memory_used_bytes**: JVM 메모리 사용량 (heap, non-heap)
- **jvm_memory_max_bytes**: JVM 최대 메모리
- **jvm_gc_pause_seconds**: GC 정지 시간
- **jvm_threads_live**: 현재 활성 스레드 수

### 3. 시스템 리소스
- **system_cpu_usage**: 시스템 CPU 사용률
- **process_cpu_usage**: 프로세스 CPU 사용률
- **system_load_average_1m**: 시스템 로드 평균 (1분)

### 4. 데이터베이스 (HikariCP)
- **hikaricp_connections_active**: 활성 DB 커넥션 수
- **hikaricp_connections_idle**: 유휴 DB 커넥션 수
- **hikaricp_connections_pending**: 대기 중인 커넥션 요청
- **hikaricp_connections_timeout_total**: 커넥션 타임아웃 총 수

### 5. 로그백 (Logback)
- **logback_events_total**: 로그 이벤트 수 (level별)

---

## 유용한 Prometheus 쿼리

### HTTP 요청률 (초당 요청 수)
```promql
rate(http_server_requests_seconds_count{application="cos-house"}[1m])
```

### HTTP 에러율 (5xx, 4xx)
```promql
rate(http_server_requests_seconds_count{application="cos-house", status=~"5.."}[1m])
rate(http_server_requests_seconds_count{application="cos-house", status=~"4.."}[1m])
```

### 평균 응답 시간
```promql
rate(http_server_requests_seconds_sum{application="cos-house"}[5m])
/
rate(http_server_requests_seconds_count{application="cos-house"}[5m])
```

### P95 응답 시간 (95번째 백분위수)
```promql
histogram_quantile(0.95,
  rate(http_server_requests_seconds_bucket{application="cos-house"}[5m])
)
```

### DB 커넥션 풀 사용률
```promql
hikaricp_connections_active{application="cos-house"}
/
hikaricp_connections_max{application="cos-house"}
* 100
```

### 메모리 사용률
```promql
jvm_memory_used_bytes{application="cos-house", area="heap"}
/
jvm_memory_max_bytes{application="cos-house", area="heap"}
* 100
```

---

## 문제 해결 (Troubleshooting)

### Prometheus가 Spring Boot 앱을 수집하지 못함 (Target DOWN)

#### 1. Spring Boot 앱이 실행 중인지 확인
```bash
curl http://localhost:8080/actuator/prometheus
# 메트릭이 반환되면 정상
```

#### 2. Prometheus 설정 확인
```bash
# Prometheus 컨테이너 로그 확인
docker logs cos-prometheus

# prometheus.yml 설정 확인
cat prometheus.yml
```

#### 3. Docker 네트워크 확인
```bash
# host.docker.internal 작동 확인
docker exec -it cos-prometheus ping host.docker.internal

# Mac/Windows: 정상 작동
# Linux: host.docker.internal 대신 호스트 IP 사용 필요
```

**Linux 사용자의 경우**, prometheus.yml 수정:
```yaml
scrape_configs:
  - job_name: 'spring-boot-app'
    static_configs:
      - targets: ['192.168.x.x:8080']  # 실제 호스트 IP
```

---

### Grafana에서 "No data" 표시

#### 1. Data source 연결 확인
- Grafana → Configuration → Data sources → Prometheus
- **Save & test** 클릭하여 연결 테스트

#### 2. 메트릭 이름 확인
- Prometheus UI (http://localhost:9090)에서 메트릭 존재 확인
- Grafana 패널 쿼리가 올바른지 확인

#### 3. 시간 범위 확인
- Grafana 대시보드 우측 상단 시간 범위 설정
- "Last 15 minutes" 등으로 변경하여 최근 데이터 확인

---

### 메트릭이 수집되지 않음

#### 1. Actuator endpoint 확인
```bash
curl http://localhost:8080/actuator
# prometheus가 목록에 있는지 확인
```

#### 2. Security 설정 확인
```bash
curl http://localhost:8080/actuator/prometheus
# 403 Forbidden이 아닌 메트릭 데이터가 반환되어야 함
```

#### 3. 애플리케이션 재시작
```bash
# 설정 변경 후 반드시 재시작
./gradlew clean bootRun
```

---

## 컨테이너 관리

### 컨테이너 상태 확인
```bash
docker ps
docker logs cos-prometheus
docker logs cos-grafana
```

### 컨테이너 재시작
```bash
# 전체 재시작
docker-compose restart

# 개별 재시작
docker-compose restart prometheus
docker-compose restart grafana
```

### 컨테이너 중지 및 제거
```bash
# 중지
docker-compose stop

# 중지 및 제거 (데이터 볼륨 유지)
docker-compose down

# 중지 및 제거 (데이터 볼륨도 삭제)
docker-compose down -v
```

### 볼륨 확인
```bash
docker volume ls | grep cos
# 출력:
# cos_grafana-data
# cos_prometheus-data
# cos_mysql-data
```

---

## 추가 개선 사항 (선택사항)

### 1. Alerting 설정
Prometheus에서 알림 규칙 추가:
```yaml
# alerts.yml
groups:
  - name: spring-boot-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High HTTP 5xx error rate"
```

### 2. 커스텀 메트릭 추가
Spring Boot 애플리케이션에서 비즈니스 메트릭 추가:
```java
@Service
public class OrderService {
    private final Counter orderCounter;

    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.created")
                .description("Total orders created")
                .register(registry);
    }

    public void createOrder() {
        // ... order creation logic
        orderCounter.increment();
    }
}
```

### 3. Prometheus 데이터 보존 기간 설정
docker-compose.yml에서 retention 설정:
```yaml
prometheus:
  command:
    - '--storage.tsdb.retention.time=30d'  # 30일 보관
```

---

## 참고 자료

- [Spring Boot Actuator 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Prometheus 문서](https://micrometer.io/docs/registry/prometheus)
- [Prometheus 공식 문서](https://prometheus.io/docs/introduction/overview/)
- [Grafana 공식 문서](https://grafana.com/docs/grafana/latest/)
- [Grafana 대시보드 라이브러리](https://grafana.com/grafana/dashboards/)

---

## 요약

### 접속 URL
- **Spring Boot 애플리케이션**: http://localhost:8080
- **Actuator Health**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **Prometheus UI**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### 시작 순서
1. `docker-compose up -d` - Prometheus, Grafana, MySQL 시작
2. `./gradlew bootRun` - Spring Boot 앱 시작
3. Prometheus (http://localhost:9090) 에서 Target 상태 확인
4. Grafana (http://localhost:3000) 에서 대시보드 설정

모니터링 시스템이 정상적으로 작동하면, 애플리케이션의 성능, 리소스 사용량, 에러율 등을 실시간으로 확인할 수 있습니다.