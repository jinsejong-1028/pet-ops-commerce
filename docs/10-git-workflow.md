# Git Workflow

이 문서는 PetOps Commerce 프로젝트에서 Git을 실무처럼 관리하기 위한 기준을 정리합니다.

## 목표

- `main` 브랜치는 항상 실행 가능한 안정 상태로 유지합니다.
- 기능, 문서, 설정 변경은 별도 브랜치에서 작업합니다.
- 커밋 이력만 봐도 어떤 작업을 했는지 이해할 수 있게 남깁니다.
- Pull Request 단위로 변경 내용을 검토하고 `main`에 병합합니다.

## 기본 브랜치

| 브랜치 | 역할 |
|---|---|
| `main` | 배포 가능하거나 최소한 빌드 가능한 안정 브랜치 |
| `feature/*` | 새 기능 개발 브랜치 |
| `fix/*` | 버그 수정 브랜치 |
| `docs/*` | 문서 작성 또는 수정 브랜치 |
| `chore/*` | 설정, 빌드, 환경 구성 변경 브랜치 |
| `refactor/*` | 기능 변화 없는 코드 구조 개선 브랜치 |

## 브랜치 이름 규칙

브랜치 이름은 작업 성격과 목적이 드러나게 작성합니다.

```text
feature/health-api
feature/member-domain
feature/product-domain
chore/docker-postgres
docs/git-workflow
fix/member-email-duplication
refactor/common-response
```

## 작업 흐름

새 작업은 항상 `main`에서 최신 상태를 확인한 뒤 브랜치를 만듭니다.

```powershell
git checkout main
git pull
git checkout -b feature/health-api
```

작업 후에는 변경 내용을 확인합니다.

```powershell
git status
git diff
```

변경 파일을 스테이징하고 커밋합니다.

```powershell
git add 파일명
git commit -m "feat: add health check API"
```

원격 저장소에 브랜치를 올립니다.

```powershell
git push -u origin feature/health-api
```

GitHub에서 Pull Request를 만들고 확인 후 `main`에 병합합니다.

## 커밋 메시지 규칙

커밋 메시지는 Conventional Commits 형식을 사용합니다.

```text
타입: 변경 내용
```

| 타입 | 의미 | 예시 |
|---|---|---|
| `feat` | 새 기능 추가 | `feat: add member registration API` |
| `fix` | 버그 수정 | `fix: handle duplicated member email` |
| `docs` | 문서 수정 | `docs: add Git workflow guide` |
| `test` | 테스트 추가/수정 | `test: add health API test` |
| `refactor` | 기능 변화 없는 코드 개선 | `refactor: separate member service logic` |
| `chore` | 설정, 빌드, 초기화 | `chore: initialize Spring Boot project` |

## Pull Request 기준

Pull Request에는 다음 내용을 정리합니다.

```text
## 작업 내용
- 무엇을 추가/수정했는지

## 검증
- 실행한 테스트나 확인 방법

## 참고
- 설계 의도, 남은 작업, 주의점
```

## 이 프로젝트에서 사용할 방식

이 프로젝트는 포트폴리오 목적이므로 다음 순서를 지킵니다.

1. 작업 전에 브랜치를 만듭니다.
2. 기능 구현과 문서화를 함께 진행합니다.
3. 작은 단위로 커밋합니다.
4. GitHub Pull Request로 변경 내용을 남깁니다.
5. `main`에는 검증된 작업만 병합합니다.

## 포트폴리오 어필 포인트

이 Git 관리 방식은 다음 경험을 보여주기 위한 목적도 있습니다.

- 기능 단위 브랜치 관리 경험
- 커밋 메시지 규칙 적용 경험
- Pull Request 기반 협업 흐름 이해
- 문서와 코드 변경 이력 관리 경험
- 안정 브랜치와 작업 브랜치 분리 경험
## PR merge 후 정리

GitHub에서 Pull Request를 merge한 뒤에는 로컬 `main`을 최신화합니다.

```powershell
git checkout main
git pull
```

merge가 끝난 작업 브랜치는 로컬에서 삭제합니다.

```powershell
git branch -d 브랜치명
```

원격 브랜치 정리 상태는 다음 명령으로 확인합니다.

```powershell
git fetch --prune
git branch -a
```

정상 상태는 `main`과 `origin/main`만 남아 있는 상태입니다.

```text
* main
  remotes/origin/HEAD -> origin/main
  remotes/origin/main
```

## 자주 쓰는 확인 명령

변경 파일 확인:

```powershell
git status
```

변경 내용 확인:

```powershell
git diff
```

`git diff` 화면에서 `:`가 보이면 `q`를 눌러 빠져나옵니다.

전체 변경 파일 스테이징:

```powershell
git add .
```

실무에서도 `git add .`를 자주 사용하지만, 실행 전 `git status`와 `git diff`로 원하지 않는 파일이 포함되지 않았는지 확인합니다.

