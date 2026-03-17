---
globs: src/test/kotlin/**/*.kt
---

# Kotlin 테스트 규칙

테스트 파일에 적용되는 규칙입니다.

## 테스트 구조

```kotlin
@SpringBootTest
class MyServiceTest {

    @Autowired
    private lateinit var myService: MyService

    // 테스트 이름은 행동을 설명 (한글 허용)
    @Test
    fun `빈 입력에 대해 에러를 반환해야 한다`() {
        // given (Arrange)
        val input = ""

        // when (Act)
        val result = assertThrows<AppException> {
            myService.validate(input)
        }

        // then (Assert)
        assertThat(result.errorCode).isEqualTo(ErrorCode.VALIDATION_ERROR)
    }
}
```

## 단위 테스트 (Mock)

```kotlin
@ExtendWith(MockitoExtension::class)
class MyServiceUnitTest {

    @Mock
    private lateinit var myRepository: MyRepository

    @InjectMocks
    private lateinit var myService: MyService

    @Test
    fun `정상적으로 조회되어야 한다`() {
        // given
        given(myRepository.findById(1L))
            .willReturn(Optional.of(MyEntity(id = 1L, name = "test")))

        // when
        val result = myService.findById(1L)

        // then
        assertThat(result.name).isEqualTo("test")
    }
}
```

## 테스트 커버리지

- 모든 public 메서드에 최소 1개 테스트
- 정상 케이스 + 에러 케이스 모두 테스트
- 엣지 케이스 (빈 값, 최대값, 특수문자)

## 허용 사항

테스트 코드에서는 다음이 허용됩니다:
- `!!` (non-null assertion) 사용 가능
- 하드코딩된 테스트 데이터
- `lateinit var` 사용 가능
