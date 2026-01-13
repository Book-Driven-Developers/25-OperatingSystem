package os.virtualmemory;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class PageFault {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("""
                Usage:
                  java PageFaultMini heap <sizeMB>
                  java PageFaultMini mmap <sizeMB>

                Examples:
                  java -Xms256m -Xmx256m PageFaultMini heap 512
                  java -Xms256m -Xmx256m PageFaultMini mmap 2048
                """);
            return;
        }

        String mode = args[0];
        int sizeMB = Integer.parseInt(args[1]);
        long bytes = sizeMB * 1024L * 1024L;

        System.out.println("PID = " + ProcessHandle.current().pid());
        System.out.println("Mode = " + mode + ", Size = " + sizeMB + "MB");
        System.out.println();

        if (mode.equalsIgnoreCase("heap")) {
            heapTouch(bytes);
        } else if (mode.equalsIgnoreCase("mmap")) {
            mmapTouch(bytes);
        } else {
            System.out.println("Unknown mode: " + mode);
        }

        System.out.println("\n(Watching time) Keep running 20s...");
        Thread.sleep(20_000);
    }

    private static void heapTouch(long bytes) {
        System.out.println("[heap] Allocate + touch");
        byte[] arr = new byte[(int) bytes];

        Instant start = Instant.now();
        long sum = 0;
        for (int i = 0; i < arr.length; i += 4096) { // 4096 = typical page size
            arr[i] = 1;
            sum += arr[i];
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        System.out.println("Touch time = " + ms + " ms, checksum=" + sum);
        System.out.println("Expected: mostly minor(soft) page faults (no disk read).");
    }

    private static void mmapTouch(long bytes) throws Exception {
        System.out.println("[mmap] Map file + touch twice");

        Path tmp = Files.createTempFile("pf-mini-", ".bin");
        tmp.toFile().deleteOnExit();

        try (RandomAccessFile raf = new RandomAccessFile(tmp.toFile(), "rw");
             FileChannel ch = raf.getChannel()) {

            raf.setLength(bytes);
            MappedByteBuffer buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, bytes);

            long t1 = touchMapped(buf, bytes, 1);
            long t2 = touchMapped(buf, bytes, 2);

            System.out.println("Pass1 = " + t1 + " ms");
            System.out.println("Pass2 = " + t2 + " ms");
            System.out.println("Expected: Pass1 slower (disk read = major faults), Pass2 faster (cached).");
        }
    }

    private static long touchMapped(MappedByteBuffer buf, long bytes, int pass) {
        Instant start = Instant.now();
        long sum = 0;
        for (int i = 0; i < bytes; i += 4096) {
            byte v = buf.get(i);
            buf.put(i, (byte) (v + 1));
            sum += v;
        }
        long ms = Duration.between(start, Instant.now()).toMillis();
        System.out.println("pass " + pass + " done, checksum=" + sum);
        return ms;
    }
}
