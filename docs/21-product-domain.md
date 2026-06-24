# Product Domain

## 작업 목적

상품 도메인의 첫 API를 구현합니다.

회원 도메인에서 만든 계층 구조를 상품 도메인에 반복 적용합니다.

```text
Controller
-> Service
-> Repository
-> Entity
```

## 구현 범위

이번 브랜치에서는 아래를 구현합니다.

- 상품 카테고리 생성 API
- 상품 카테고리 목록 API
- 상품 생성 API
- 상품 단건 조회 API
- 상품 목록 조회 API
- 상품 Service 테스트
- 상품 Controller 테스트
- IntelliJ HTTP Client 요청 파일

## DB 관계 설계

`products` 테이블에는 `category_id` 컬럼이 있습니다.

다만 DB foreign key constraint는 걸지 않습니다.

```text
DB FK 제약 없음
category_id 컬럼 있음
Service에서 categoryId 존재 여부 검증
```

상품 생성 흐름은 아래와 같습니다.

```text
CreateProductRequest
-> ProductService
-> ProductCategoryRepository.existsById(categoryId)
-> 없으면 404 category not found
-> 있으면 Product 저장
```

## API 목록

### 상품 카테고리 생성

```text
POST /api/v1/product-categories
```

요청:

```json
{
  "name": "사료",
  "displayOrder": 1
}
```

### 상품 카테고리 목록

```text
GET /api/v1/product-categories
```

### 상품 생성

```text
POST /api/v1/products
```

요청:

```json
{
  "categoryId": 1,
  "name": "고양이 사료",
  "description": "실내묘용 건식 사료",
  "price": 25000
}
```

### 상품 단건 조회

```text
GET /api/v1/products/{productId}
```

### 상품 목록 조회

```text
GET /api/v1/products
```

조건 조회:

```text
GET /api/v1/products?categoryId=1&saleStatus=ON_SALE
```

## 응답 구조

공통 API 응답 구조를 사용합니다.

```json
{
  "success": true,
  "data": {
    "id": 1,
    "categoryId": 1,
    "name": "고양이 사료",
    "description": "실내묘용 건식 사료",
    "price": 25000,
    "saleStatus": "ON_SALE"
  },
  "message": "OK"
}
```

## 검증 방법

```powershell
.\gradlew.bat test --console=plain
```

서버 실행 후 IntelliJ HTTP Client로 확인합니다.

```powershell
.\gradlew.bat bootRun
```

확인 파일:

```text
http/product-api.http
```

## 학습 포인트

- 상품 도메인 패키지 구조
- Entity enum 매핑
- DB FK 없이 Service에서 관계 검증
- 목록 조회 API
- 공통 응답 구조 재사용
- validation 실패 응답 재사용