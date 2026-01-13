package ch14;

public class PageFaultDemo {
    private static final int ROWS = 10_000;
    private static final int COLS = 10_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("[실험] 2차원 배열 페이지 폴트: %d x %d 배열\n", ROWS, COLS);
        int[][] arr = new int[ROWS][COLS];
        System.out.println("배열 할당 완료. 5초 대기 후 실험 시작");
        Thread.sleep(5000);

        // 행 우선 순회(row-major)
        long rowSum = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                arr[i][j]++;
            }
        }
        long end = System.currentTimeMillis();
        rowSum += arr[ROWS - 1][COLS - 1]; // 값 사용(최적화 방지)
        System.out.printf("행 우선 순회 평균 소요 시간: %d ms\n", end - start);
        System.out.println("5초 대기 후 열 우선 순회 시작");
        Thread.sleep(5000);

        // 열 우선 순회(column-major)
        long colSum = 0;
        start = System.currentTimeMillis();
        for (int j = 0; j < COLS; j++) {
            for (int i = 0; i < ROWS; i++) {
                arr[i][j]++;
            }
        }
        end = System.currentTimeMillis();
        colSum += arr[ROWS - 1][COLS - 1];
        System.out.printf("열 우선 순회 평균 소요 시간: %d ms\n", end - start);
    }
}
