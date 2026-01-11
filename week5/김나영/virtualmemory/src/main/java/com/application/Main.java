package com.application;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static class PageTableEntry {
        int frameNumber; // 프레임 번호
        boolean valid; // 유효 비트

        public PageTableEntry() {
            this.frameNumber = -1; // 아직 할당 안됨
            this.valid = false; // 유효 비트 = 0
        }
    }

    public static class PageTable {
        List<PageTableEntry> entries;
        int nextFrame = 0; // 다음에 할당할 프레임 번호
        int pageFaultCount = 0;

        PageTable(int numPages) {
            entries = new ArrayList<>();
            for (int i = 0; i < numPages; i++) {
                entries.add(new PageTableEntry());
            }
        }

        public int accessPage(int pageNumber) {
            PageTableEntry entry = entries.get(pageNumber);

            if (!entry.valid) {
                // 유효 비트 = 0 -> 페이지 폴트 발생
                System.out.println("페이지 폴트 발생");
                handlePageFault(pageNumber);
                pageFaultCount++;
            } else {
                System.out.println("정상 접근 : 프레임 "+entry.frameNumber);
            }
            return entry.frameNumber;
        }

        public void handlePageFault(int pageNumber) {
            System.out.println("페이지 폴트 처리 중");

            PageTableEntry entry = entries.get(pageNumber);
            entry.frameNumber = nextFrame++; // 프레임 할당
            entry.valid = true; // 유효 비트 1로 변경

            System.out.println("페이지를 프레임 "+entry.frameNumber+"에 로드 완료");
        }

        public void printStatus() {
            System.out.println("\n=== 페이지 테이블 상태 ===");
            System.out.println("페이지  프레임  유효비트");
            System.out.println("----------------------");
            for (int i = 0; i < entries.size(); i++) {
                PageTableEntry e = entries.get(i);
                String frame = e.frameNumber == -1 ? "-" : String.valueOf(e.frameNumber);
                String valid = e.valid ? "1" : "0";
                System.out.printf("  %d      %s       %s\n", i, frame, valid);
            }
            System.out.println("----------------------");
            System.out.println("총 페이지 폴트: " + pageFaultCount + "회\n");
        }

    }
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 페이지 폴트 시뮬레이션 ===\n");

        System.out.println("유효 비트 = 0 : 페이지가 메모리에 없음 → 페이지 폴트!");
        System.out.println("유효 비트 = 1 : 페이지가 메모리에 있음 → 정상 접근\n");

        // 4개의 페이지를 가진 페이지 테이블 생성
        PageTable pageTable = new PageTable(4);

        System.out.println("[ 초기 상태 ]");
        System.out.println("모든 페이지의 유효 비트 = 0");
        pageTable.printStatus();

        Thread.sleep(2000);

        // 시나리오 1: 페이지 1, 2 접근 (첫 접근)
        System.out.println("[ 시나리오 1 ] 새로운 페이지들 첫 접근");
        System.out.println("\n페이지 1 접근:");
        pageTable.accessPage(1);
        System.out.println("\n페이지 2 접근:");
        pageTable.accessPage(2);
        pageTable.printStatus();
        Thread.sleep(2000);

        // 시나리오 2: 여러 페이지 재접근
        System.out.println("[ 시나리오 2 ] 이미 로드된 페이지들 재접근");
        System.out.println("\n페이지 1 재접근:");
        pageTable.accessPage(1);
        System.out.println("\n페이지 2 재접근:");
        pageTable.accessPage(2);
        System.out.println("\n페이지 1 또 재접근:");
        pageTable.accessPage(1);
        pageTable.printStatus();

        // 최종 결과
        System.out.println("=== 실험 결과 ===");
        System.out.println("총 접근: 5회");
        System.out.println("페이지 폴트: " + pageTable.pageFaultCount + "회");
        System.out.println("정상 접근: " + (5 - pageTable.pageFaultCount) + "회");

        System.out.println("\n[ 분석 ]");
        System.out.println("- 페이지 1, 2 첫 접근: 페이지 폴트 발생 (유효 비트 = 0)");
        System.out.println("- 페이지 1, 2 재접근: 정상 접근 (유효 비트 = 1)");
    }
}