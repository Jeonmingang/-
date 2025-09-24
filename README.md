# UltimatePixelmonRating 1.9.2 (universal)
- Pixelmon 9.1.13 (MC 1.16.5) 종료 이벤트로 Elo 정산 + 배틀중 락/해제
- Forge EVENT_BUS 등록은 리플렉션으로 처리 → 빌드시 Forge 본체 의존 필요 없음
- CI에서 Pixelmon universal JAR을 자동 다운로드하여 로컬 Maven에 설치 후 빌드

## 로컬 빌드
1) 서버의 `Pixelmon-1.16.5-9.1.13-universal.jar`를 `libs/`에 복사
2) `mvn -B -U package`

## GitHub Actions
- `.github/workflows/build.yml`가 Pixelmon universal을 **Modrinth CDN**에서 내려받아 설치 후 빌드합니다.
