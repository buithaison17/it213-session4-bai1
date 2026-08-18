# Giải thích cơ chế nạp Profile động của Spring Boot

## 1. Profile trong Spring Boot là gì?

Spring Boot Profile là cơ chế cho phép ứng dụng sử dụng các cấu hình
khác nhau tùy theo môi trường chạy.

Ví dụ hệ thống **AI Logistics Incident Reporter** có hai môi trường:

-   `local`: chạy mô hình `qwen2.5-coder:7b` thông qua Ollama trên máy
    cá nhân.
-   `cloud`: sử dụng mô hình `gemini-2.5-flash` thông qua OpenRouter.

Thay vì sửa mã nguồn Java mỗi khi chuyển môi trường, Spring Boot cho
phép tách cấu hình thành các file:

``` text
application.properties
application-local.properties
application-cloud.properties
```

Khi thay đổi Profile, Spring Boot sẽ tự động chọn và nạp cấu hình tương
ứng.

------------------------------------------------------------------------

## 2. Cấu trúc các file cấu hình

### application.properties

Đây là file cấu hình chung của ứng dụng:

``` properties
spring.application.name=ai-logistics-incident-reporter

spring.profiles.active=local
```

Dòng:

``` properties
spring.profiles.active=local
```

quy định Profile mặc định được kích hoạt là `local`.

------------------------------------------------------------------------

## 3. application-local.properties

File này chứa cấu hình dành riêng cho môi trường Local:

``` properties
spring.ai.model.chat=ollama

spring.ai.ollama.base-url=http://localhost:11434

spring.ai.ollama.chat.model=qwen2.5-coder:7b
```

Khi Profile `local` được kích hoạt, Spring Boot sẽ đọc thêm file:

``` text
application-local.properties
```

Khi đó ứng dụng sử dụng Ollama làm Chat Model và model:

``` text
qwen2.5-coder:7b
```

------------------------------------------------------------------------

## 4. application-cloud.properties

File này chứa cấu hình dành riêng cho môi trường Cloud:

``` properties
spring.ai.model.chat=openai

spring.ai.openai.base-url=https://openrouter.ai/api

spring.ai.openai.api-key=${ROUTER_API_KEY}

spring.ai.openai.chat.model=google/gemini-2.5-flash
```

Khi Profile `cloud` được kích hoạt, Spring Boot sẽ đọc thêm:

``` text
application-cloud.properties
```

Ứng dụng sử dụng OpenAI-compatible API của OpenRouter và model:

``` text
google/gemini-2.5-flash
```

API Key được lấy từ biến môi trường:

``` text
ROUTER_API_KEY
```

Do đó API Key không cần ghi trực tiếp vào source code.

------------------------------------------------------------------------

# 5. Cơ chế Spring Boot nạp Profile

Khi ứng dụng khởi động, Spring Boot xây dựng một hệ thống `Environment`
chứa toàn bộ các thuộc tính cấu hình.

Quá trình có thể hình dung như sau:

``` text
Spring Boot Start
       |
       v
Đọc application.properties
       |
       v
Xác định Active Profile
       |
       +----------------------+
       |                      |
       v                      v
    local                   cloud
       |                      |
       v                      v
application-local.properties
                         application-cloud.properties
       |                      |
       v                      v
  Ollama Config         OpenRouter Config
       |                      |
       v                      v
OllamaChatModel          OpenAI ChatModel
```

Spring Boot sử dụng giá trị của:

``` properties
spring.profiles.active
```

để xác định Profile đang hoạt động.

------------------------------------------------------------------------

# 6. Profile mặc định

Trong `application.properties`:

``` properties
spring.profiles.active=local
```

Nếu chạy ứng dụng bình thường:

``` bash
mvn spring-boot:run
```

Spring Boot sẽ sử dụng:

``` text
local
```

và tự động nạp:

``` text
application.properties
application-local.properties
```

Kết quả:

``` text
Profile: local
LLM: qwen2.5-coder:7b
Provider: Ollama
```

------------------------------------------------------------------------

# 7. Override Profile khi chạy ứng dụng

Điểm quan trọng của Profile là không cần sửa `application.properties`.

Có thể truyền Profile từ command line:

``` bash
java -jar app.jar --spring.profiles.active=cloud
```

Hoặc với Maven:

``` bash
mvn spring-boot:run -Dspring-boot.run.profiles=cloud
```

Khi đó:

``` text
spring.profiles.active=cloud
```

được ưu tiên hơn giá trị `local` được khai báo trong
`application.properties`.

Spring Boot sẽ sử dụng:

``` text
application.properties
application-cloud.properties
```

thay vì:

``` text
application-local.properties
```

Kết quả:

``` text
Profile: cloud
LLM: google/gemini-2.5-flash
Provider: OpenRouter
```

------------------------------------------------------------------------

# 8. Vì sao không cần sửa code Java?

Đây là ưu điểm quan trọng nhất của Profile.

Controller, Service hoặc các thành phần nghiệp vụ không cần viết:

``` java
if (environment == "local") {
    // dùng Ollama
} else {
    // dùng OpenRouter
}
```

Thay vào đó, Spring AI đọc cấu hình đã được Spring Boot nạp.

Ví dụ Local:

``` properties
spring.ai.model.chat=ollama
spring.ai.ollama.chat.model=qwen2.5-coder:7b
```

Spring AI sẽ tạo Chat Model tương ứng với Ollama.

Cloud:

``` properties
spring.ai.model.chat=openai
spring.ai.openai.chat.model=google/gemini-2.5-flash
```

Spring AI sẽ tạo Chat Model tương ứng với OpenAI-compatible API.

Như vậy Java code vẫn giữ nguyên:

``` java
private final ChatClient chatClient;
```

Việc thay đổi model và provider được thực hiện ở tầng configuration.

------------------------------------------------------------------------

# 9. Spring Boot kết hợp với Spring AI Auto-Configuration

Spring Boot có cơ chế Auto-Configuration.

Khi project có dependency của Spring AI và cấu hình tương ứng, Spring
Boot/Spring AI sẽ kiểm tra các property và tạo Bean cần thiết.

Ví dụ khi Profile `local`:

``` text
spring.ai.model.chat=ollama
             |
             v
Spring AI Ollama Auto Configuration
             |
             v
OllamaChatModel Bean
             |
             v
ChatClient
```

Khi Profile `cloud`:

``` text
spring.ai.model.chat=openai
             |
             v
Spring AI OpenAI Auto Configuration
             |
             v
OpenAI ChatModel Bean
             |
             v
ChatClient
```

Do đó application code không cần biết model đang chạy ở local hay cloud.

------------------------------------------------------------------------

# 10. Kiểm tra Profile bằng SystemConfigController

Để kiểm chứng Spring Boot đã nhận đúng Profile, project có thể cung cấp
endpoint:

``` text
GET /api/v1/incident/config
```

Ví dụ Controller:

``` java
@RestController
@RequestMapping("/api/v1/incident/config")
public class SystemConfigController {

    private final Environment environment;

    @Value("${spring.ai.ollama.chat.model:}")
    private String ollamaModel;

    @Value("${spring.ai.openai.chat.model:}")
    private String openAiModel;

    public SystemConfigController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping
    public Map<String, Object> getConfig() {

        String model = !ollamaModel.isBlank()
                ? ollamaModel
                : openAiModel;

        return Map.of(
                "activeProfile",
                environment.getActiveProfiles(),
                "llmModel",
                model
        );
    }
}
```

Khi chạy Local:

``` json
{
  "activeProfile": [
    "local"
  ],
  "llmModel": "qwen2.5-coder:7b"
}
```

Khi chạy Cloud:

``` json
{
  "activeProfile": [
    "cloud"
  ],
  "llmModel": "google/gemini-2.5-flash"
}
```

Endpoint này là một cách đơn giản để đối soát rằng Profile và cấu hình
LLM đã được nhận đúng.

------------------------------------------------------------------------

# 11. Thứ tự ưu tiên cấu hình

Spring Boot có nhiều nguồn cấu hình khác nhau, ví dụ:

``` text
application.properties
application-{profile}.properties
Environment variables
Command line arguments
```

Khi cùng một property xuất hiện ở nhiều nguồn, nguồn có độ ưu tiên cao
hơn sẽ ghi đè nguồn thấp hơn.

Ví dụ trong:

``` properties
application.properties
```

có:

``` properties
spring.profiles.active=local
```

nhưng khi chạy:

``` bash
java -jar app.jar --spring.profiles.active=cloud
```

thì Profile `cloud` được sử dụng.

Điều này cho phép cùng một file JAR được triển khai ở nhiều môi trường
mà không cần build lại source code.

------------------------------------------------------------------------

# 12. Lợi ích đối với hệ thống AI Logistics Incident Reporter

Cách thiết kế này tạo ra kiến trúc Hybrid AI linh hoạt:

``` text
                    Java Application
                           |
                      Spring AI
                           |
                    ChatClient API
                           |
              +------------+------------+
              |                         |
          Profile local             Profile cloud
              |                         |
              v                         v
           Ollama                  OpenRouter
              |                         |
              v                         v
      qwen2.5-coder:7b        gemini-2.5-flash
```

### Local

Ưu điểm:

-   Dữ liệu nội bộ có thể được xử lý tại máy local.
-   Không cần gửi dữ liệu lên dịch vụ cloud.
-   Không phát sinh chi phí API trong quá trình phát triển.
-   Phù hợp cho development và testing.

### Cloud

Ưu điểm:

-   Có thể sử dụng model mạnh hơn.
-   Không cần chạy model local.
-   Có thể triển khai lên server/cloud.
-   API Key được quản lý bằng biến môi trường.

------------------------------------------------------------------------

# 13. Kết luận

Spring Boot Profile giúp tách cấu hình khỏi source code và cho phép ứng
dụng chạy với nhiều môi trường khác nhau.

Trong bài toán AI Logistics Incident Reporter:

``` text
local
  -> Ollama
  -> qwen2.5-coder:7b

cloud
  -> OpenRouter
  -> google/gemini-2.5-flash
```

Việc chuyển đổi môi trường chỉ cần thay đổi:

``` text
spring.profiles.active
```

Ví dụ:

``` bash
java -jar app.jar --spring.profiles.active=local
```

hoặc:

``` bash
java -jar app.jar --spring.profiles.active=cloud
```

Không cần thay đổi mã nguồn Java.

Đây là cách tiếp cận phù hợp với nguyên tắc **Configuration over Code**,
giúp hệ thống dễ triển khai, dễ bảo trì và có khả năng chuyển đổi giữa
mô hình AI local và cloud một cách linh hoạt.
