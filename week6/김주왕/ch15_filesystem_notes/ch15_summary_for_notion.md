# 혼공컴운 15장: 파일 시스템 — 요약

## 주요 개념

- 파일 시스템 목적: 파일 저장, 네임스페이스 관리, 메타데이터 유지, 공간 관리.

## 할당 방식 비교

| 방식 | 장점 | 단점 | 사용 사례 |
|---|---:|---|---|
| 연속 (Contiguous) | 단순, 연속 접근 빠름 | 외부 단편화, 파일 확장 어려움 | 초기 파일 시스템, 일부 고성능 요구 |
| 연결 (Linked) | 외부 단편화 없음, 쉬운 확장 | 임의 접근 느림 | 단순 디스크 관리, 일부 임베디드 |
| FAT | 테이블 기반 체인, 구현 단순 | 테이블 손상 시 위험, 느린 대형 디스크 | FAT16/32, 이동식 미디어 |
| 색인 (Indexed/i-node) | 빠른 임의 접근, 유연한 확장(간접 블록) | 구현 복잡성 | UNIX 계열 파일 시스템

## 체크리스트 (학습 / 실습)

- [ ] 블록과 클러스터 개념 이해
- [ ] 자유 블록 관리(bitmaps vs free lists)
- [ ] 연속/연결/FAT/색인 각 방식 구현해 보기
- [ ] 성능 비교: 순차/임의 접근, 공간 사용율

## 실습 가이드라인

- 블록 디바이스 시뮬레이터를 만든다 (`ch15_filesystem_lab`) — 고정 블록 크기, 총 블록 수 설정
- 각 lab에서 파일 생성/삭제/읽기/확장/단편화 측정 코드를 둔다

## 출처

- Velog note: https://velog.io/@mmodestaa/%ED%98%BC%EC%9E%90-%EA%B3%B5%EB%B6%80%ED%95%98%EB%8A%94-%EC%BB%B4%ED%93%A8%ED%84%B0-%EA%B5%AC%EC%A1%B0-%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9C-Section-15.-%ED%8C%8C%EC%9D%BC-%EC%8B%9C%EC%8A%A4%ED%85%9C
- Tistory note: https://kminu.tistory.com/205
- Wikipedia: File system / File Allocation Table / Inode
