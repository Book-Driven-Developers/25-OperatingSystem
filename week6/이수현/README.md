# Chapter15 파일시스템
## 15-1 파일과 디렉터리
### 개념 정리
#### 파일 시스템
+ 파일과 디렉터리를 관리하는 운영체제 내의 프로그램
+ 파일과 디렉터리: 보조기억장치의 데이터 덩어리
---
### 파일에 있는 정보
+ 파일을 이루는 정보
+ 파일을 실행하기 위한 정보 + 부가 정보 (= 속성, 메타 데이터)
> 참고로, 파일을 읽을 때 응용 프로그램은 직접 접근할 수 없으므로, 시스템 호출을 통해 운영체제로부터 도움을 받는다.
#### 파일의 속성
<img width="414" height="224" alt="Image" src="https://github.com/user-attachments/assets/832c9cd1-ec92-4d51-a7cc-7a2856dcf696" />

+ 파일의 유형
  + 확장자로 유형을 특정 지을 수 있음
  <img width="257" height="169" alt="Image" src="https://github.com/user-attachments/assets/2b085c99-2990-4f21-b23c-b37ada2031e1" />
---
### 디렉터리
> 윈도우에서는 '폴더'라고 함
> 디렉터리 또한 파일과 마찬가지로 응용 프로그램이 직접 접근할 수 없음!
<p>
<img width="478" height="223" alt="Image" src="https://github.com/user-attachments/assets/f698f8bb-6c65-4877-89bc-8ed94106cf93" />
<em>루드 디렉터리(/)와 서브 디렉터리들로 구성됨</em>
</p>
+ 트리 구조 디렉터리: 여러 계층으로 파일 및 폴더를 관리함

#### 경로
+ 절대 경로
  + 루트 디렉터리에서 자기 자신까지 이르는 고유한 경로
  + ex) /home/chantro/text.txt
+ 상대 경로
  + 현재 디렉터리에서 자기 자신까지 이르는 경로
  + ex) ./text.txt

#### 디렉터리 엔트리
<img width="468" height="234" alt="Image" src="https://github.com/user-attachments/assets/27e6fe88-3d50-43cd-91ec-a78952e11d2c" />

각 엔트리(행)에 담기는 정보
+ 디렉터리에 포함된 대상의 이름 (파일명)
+ 그 대상이 보조기억장치 내에 저장된 위치를 유추할 수 있는 정보
+ 가끔 파일의 속성을 명시하는 경우도 있음
---
### 파일 vs 디렉터리
파일의 내부에는 파일과 관련된 정보들이 있다면,
디렉터리의 내부에는 해당 디렉터리에 담겨있는 대상과 관련된 정보들이 담겨있다.


## 15-2 파일 시스템
### 들어가기 전: 파티셔닝과 포매팅
<p>
<img width="159" height="80" alt="Image" src="https://github.com/user-attachments/assets/43589c73-54ee-4b16-816e-0372e8693753" />
<em>한번도 사용된 적 없는 새 하드 디스크 연결 시 나타나는 창</em>
</p>

#### 파티셔닝
<img width="518" height="238" alt="Image" src="https://github.com/user-attachments/assets/9ce28f1f-e028-449e-b6f4-9fdad44e5684" />

+ 저장 장치의 논리적인 영역을 구획하는 작업
+ 나뉘어진 논리적인 영역들을 파티션이라고 함

#### 포매팅
+ 파일 시스템을 설정함
+ 어떤 방식으로 파일을 관리할지 결정, 새로운 데이터를 쓸 준비하는 작업
+ 파일 시스템은 포매팅을 할 때 결정됨 -> 파티션마다 다른 종류의 파일 시스템을 설정할 수 있음
+ 포매팅까지 완료된다면, 파일과 디렉터리 생성이 가능해짐
+ 저수준 포매팅(물리적), 논리적 포매팅으로 나누어짐
---
### 파일 할당 방법
> 하드디스크의 가장 작은 저장 단위: 섹터 (블록은 하나 이상의 섹터)
> 
> 운영체제는 파일과 디렉터리를 블록 단위로 읽고 씀 즉, 하나의 파일이 보조기억장치에 저장될 때에는 여러 블록에 걸쳐 저장됨

+ 파일을 보조기억장치에 할당하는 두가지 방법
  <img width="481" height="214" alt="Image" src="https://github.com/user-attachments/assets/83760432-d508-4806-9cc0-8de6665600f8" />
+ 오늘날은 주로 불연속 할당을 사용
---
### 연속 할당
<img width="650" height="293" alt="Image" src="https://github.com/user-attachments/assets/895f01b4-5613-4bac-a756-12a150f347ea" />

+ 보조기억장치 내 연속적인 블록에 파일을 할당함

#### 파일에 접근하기 위해 필요한 정보 (디렉터리 엔트리)
+ 파일 이름
+ 첫번째 블록 주소
+ 블록 단위의 길이 (offset)

#### 연속 할당의 부작용
<img width="708" height="167" alt="Image" src="https://github.com/user-attachments/assets/19ac787b-82f0-4417-a6c4-9c32386d6b5c" />

+ 구현이 단순하지만 **외부 단편화**를 야기할 수 있음
---
### 불연속 할당 (1) 연결 할당
<img width="403" height="202" alt="Image" src="https://github.com/user-attachments/assets/6a11ba85-3a55-49e5-969a-84bf21beb4b2" />

+ 각 블록의 일부에 다음 블록의 주소를 저장하여 각 블록이 다음 블록을 가리키는 형태로 할당
+ 파일이 이루는 데이터 블록을 **연결리스트**로 관리

#### 파일에 접근하기 위해 필요한 정보 (디렉터리 엔트리)
+ 파일 이름
+ 첫번째 블록 주소
+ 블록 단위의 길이

#### 연결 할당의 단점
+ 반드시 첫번째 블록부터 하나씩 읽어들여야 함 -> 파일의 임의 접근 속도가 느릴 수 있음 (중간 블록부터 읽을 수 없으므로)
+ 어떤 블록에 장애 발생 시, 해당 블록 이후 블록은 접근이 어렵다
> 이를 보완한 방법: FAT 파일시스템!
---
### 불연속 할당 (2) 색인 할당
<img width="322" height="202" alt="Image" src="https://github.com/user-attachments/assets/a59200b3-ab43-40e4-b91d-afe0f813004b" />

+ 파일의 모든 블록 주소를 색인 블록이라는 하나의 블록에 모아 관리하는 방식
+ 장점: 연결 할당에 비해, 파일 내 임의의 위치에 접근하기 용이함

#### 파일에 접근하기 위해 필요한 정보 (디렉터리 엔트리)
  <img width="372" height="205" alt="Image" src="https://github.com/user-attachments/assets/f491214e-3f63-447f-a43c-638d63c8acd2" />

+ 파일 이름
+ 색인 블록 주소
---
### FAT 파일 시스템
<p>
<img width="427" height="88" alt="Image" src="https://github.com/user-attachments/assets/e608cb7a-a8e6-4b3e-8481-02453aab0bde" />
<em>FAT 파일시스템의 파티션 모습</em>
</p>

+ **연결 할당 기반** 파일 시스템
+ 연결 할당의 단점을 보완함
+ FAT를 활용하는 파일 시스템

#### 어떻게 보완?
<img width="625" height="511" alt="Image" src="https://github.com/user-attachments/assets/e208c802-0788-4015-ba7a-b21c821e2f22" />

+ 각 블록에 포함된 다음 블록의 주소들을 한데 모아 **테이블(FAT)**로 관리
+ FAT가 **메모리에 캐시**될 경우, 느린 임의 접근 속도를 개선할 수 있음

#### 파일에 접근하기 위해 필요한 정보 (디렉터리 엔트리)
> 파일의 속성까지 가지고 있음
+ 파일 이름
+ 확장자
+ 속성
+ 예약 영역
+ 생성 시간, 마지막 접근 시간, 마지막 수정시간
+ 시작 블록, 파일 크기
---
### 유닉스 파일 시스템
+ **색인 할당 기반** 파일 시스템
+ 색인 블록 == **i-node**
  + 파일의 속성 정보와 15개의 블록 주소 저장 가능
  + 파일마다 i-node를 가지고 있으며, i-node마다 고유 번호가 부여됨

#### 파일 시스템 파티션 구조
<img width="708" height="249" alt="Image" src="https://github.com/user-attachments/assets/a4f306ce-1277-4bc7-90fd-9cadf6b32d8a" />

#### 파일에 접근하기 위해 필요한 정보 (디렉터리 엔트리)
+ i-node 번호
+ 파일 시스템 이름

#### 만약 15개 블록 이상을 차지하는 파일은?
1. 블록 주소 중 12개에는 **직접 블록 주소** 저장
   + 직접 블록: 파일 데이터가 저장된 블록
   <img width="559" height="271" alt="Image" src="https://github.com/user-attachments/assets/bfe1eca2-cdf3-4b8e-98dd-1b4417d5e3e7" />

2. 1번으로 충분하지 않을 경우, 13번째 주소에 **단일 간접 블록 주소** 저장
   + 단일 간접 블록: 파일 데이터를 저장한 **블록의 주소**가 저장된 블록
   <img width="585" height="256" alt="Image" src="https://github.com/user-attachments/assets/662525c3-a554-498c-b1ea-92a82d91e59f" />

3. 2번으로도 충분하지 않은 경우, 14번째 주소에 **이중 간접 블록 주소** 저장
   + 이중 간접 블록: **단일 간접 블록들의 주소**를 저장하는 블록
   <img width="531" height="327" alt="Image" src="https://github.com/user-attachments/assets/4a532eca-d5f0-4869-9edf-a95b1229a57b" />

4. 3번으로도 충분하지 않은 경우, 15번째 주소에 **삼중 간접 블록 주소** 저장
  + 삼중 간접 블록: 이중 간접 블록들의 주소를 저장하는 블록
  <img width="618" height="487" alt="Image" src="https://github.com/user-attachments/assets/09ddd528-9dd8-430b-95ff-a9c2d362986b" />