# 쿠버네티스의 파일 시스템
## 들어가기 전
### 쿠버네티스는 어떻게 로컬 자원을 쓰는가?
+ 로컬에서 쿠버네티스를 돌리면 로컬 컴퓨터의 CPU, RAM, 네트워크 자원을 “공유해서” 사용함
+ 이때 각 Pod들은 공유된 자원을 나눠씀
+ 다만, 직접 OS 자원을 쓰는지, 가상화된 환경을 통해 쓰는지는 설정에 따라 다름
#### 대표적인 K8s 도구들
| 도구       | 자원 사용 방식        |
|----------|-----------------|
| Minikube | VM 안에서 사용 (가상화) |
| kind (Kubernetes in Docker)| Docker 컨테이너로 사용 |
| Docker Desktop K8s |	내부 VM 사용 |
| k3s | 로컬 OS 자원 직접 사용 |

#### CPU/RAM 사용 방식
1. 예시: Minukube
```markdown
로컬 컴퓨터
 └─ VM (쿠버네티스 노드)
     └─ Pod
```
+ VM이 로컬 CPU/RAM 일부를 할당받음
+ 쿠버네티스는 그 VM 자원만 관리함
+ 그래서 minikube start --cpus=4 --memory=8g 같은 옵션이 있음

2. 예시: kind
```markdown
내 노트북
 └─ Docker 컨테이너 (쿠버네티스 노드)
     └─ Pod
```
+ Docker 컨테이너 = 호스트 자원 공유
+ cgroup으로 CPU, 메모리 제한

#### 네트워크 공유 방식
```markdown
[내 노트북 네트워크]
        │
[VM or Docker bridge]
        │
[Pod 네트워크]
```
+ Pod는 **자기만의** 가상 IP를 가짐
+ CNI 플러그인(Calico, Flannel 등)이 네트워크 구성
+ 외부에 접근하려면, NodePort -> LoadBalancer -> Port Forward -> Ingress 같은 방식으로 연결해야 함
---
### Pod의 내부 컨테이너들은 무엇을 공유할까?
+ IP 주소
+ Port 공간
+ localhost
+ Volume(따로 공유 가능)
+ CPU/RAM 할당량
+ Network Namespace

#### 하지만 컨테이너 간 파일 시스템은 공유하지 않음!
+ Pod 내부의 각 컨테이너는 분리된 파일 시스템을 가짐
+ why? 컨테이너 마다의 파일시스템은 컨테이너 이미지에서 제공하기 때문
> 각 컨테이너간 공유, 컨테이너 삭제 시 데이터 유지를 위해 쿠버네티스는 Volume을 사용함

## 쿠버네티스의 저장소
### 쿠버네티스는 디스크 자원도 로컬과 공유할까?
로컬 서버의 디스크 자원을 사용하나, 네트워크와 마찬가지로 바로 공유가 아닌 **"볼륨 추상화 계층"**을 통해 관리함

즉, 쿠버네티스는 물리 서버의 디스크를 직접 쓰지 않고 추상화 계층을 거쳐서 사용함

---
### K8s Volume의 등장 배경: 컨테이너의 기본 원칙
컨테이너 안의 파일 시스템은 
+ 컨테이너 재시작
+ Pod 재생성
+ Node 죽음

일 경우 날아가게 된다. 즉, 임시 저장소이다.

---
### K8s Volume
+ Pod에 마운트되는 저장 공간
+ 데이터를 Pod 생명주기와 분리하기 위해 등장
+ 기본 구조
  ```markdown
    [실제 디스크 / 클라우드 스토리지 / 로컬 디스크]
                      ↓
             PersistentVolume (PV)
                      ↓
          PersistentVolumeClaim (PVC)
                      ↓
                     Pod
                      ↓
                  Container
  ```

---
### 다양한 Volume의 종류
용도 별로 적절한 상황에서 volume의 타입을 선택해 마운트 할 수 있다.
```yaml
volumes:
  - name: data-volume
    awsElasticBlockStore:
      volumeID: <volume-id>
      fsType: ex4
```
<img width="460" height="589" alt="Image" src="https://github.com/user-attachments/assets/16e50dff-f8ab-407f-809f-efb48321eb05" />

#### 1. 임시/비영구 스토리지
|      유형      |    데이터 유지     | Node 종속 |   사용 목적    |        특징        |
|:------------:|:-------------:|:-------:|:----------:|:----------------:|
|   emptyDir   | ❌ Pod 삭제 시 삭제 |    ❌    |  캐시, 임시파일  | Pod가 살아있는 동안만 유지 |
|  configMap	  |      ❌	       |   ❌	    |   설정 파일	   |      읽기 전용       |
|   secret	    |      ❌	       |   ❌	    |   비밀 정보	   |    암호화, 읽기 전용    |
| downwardAPI	 |      ❌	       |   ❌	    | Pod 메타데이터	 |     환경 정보 전달     |

#### 2. Node 로컬 스토리지
|    유형	    | 데이터 유지	 | Node 종속	 |   사용 목적	    |      위험성       |
|:---------:|:-------:|:--------:|:-----------:|:--------------:|
| hostPath	 |   ⭕	    |    ⭕	    |  테스트, 디버깅	  | ❗보안 위험, 이식성 없음 |
|  local	   |   ⭕	    |    ⭕	    | 고성능 로컬 저장소	 |  Node 장애 시 위험  |

#### 3. 네트워크 / 클라우드 기반 영구 스토리지
|     유형     | 데이터 유지	 | Node 종속	 |   사용 예	   |      특징       |
|:----------:|:-------:|:--------:|:---------:|:-------------:|
|    NFS	    |   ⭕	    |    ❌	    |  파일 공유	   | 여러 Pod 공유 가능  |
|   iSCSI	   |   ⭕	    |    ❌	    | 블록 스토리지	  |      고성능      |
|  CephFS	   |   ⭕	    |    ❌	    | 분산 파일시스템	 |   대규모 클러스터    |
| GlusterFS	 |   ⭕	    |    ❌	    | 분산 스토리지	  | deprecated 추세 |

#### 4. 클라우드 프로바이더 전용
| Cloud	 |    Volume 유형	    |      특징      |
|:------:|:----------------:|:------------:|
|  AWS	  |       EBS	       |  단일 AZ, 고성능  |
|  AWS	  |       EFS	       | 여러 Pod 공유 가능 |
|  GCP	  | Persistent Disk	 |    자동 관리     |
| Azure	 |      Disk	       |    VM 디스크    |
| Azure	 |      File	       |    NFS 유사    |

#### 5. CSI 기반 (요즘 표준)
|     유형	     |     설명	     |   장점    |
|:-----------:|:-----------:|:-------:|
| CSI Volume	 | 외부 스토리지 연동	 | 확장성, 표준 |
|  Longhorn	  | 분산 블록 스토리지	 |  쉬운 관리  |
|  OpenEBS	   | 컨테이너 네이티브	  |   고성능   |
|  Portworx	  |   엔터프라이즈	   | HA, 백업  |