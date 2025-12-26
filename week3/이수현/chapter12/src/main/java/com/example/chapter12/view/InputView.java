package com.example.chapter12.view;

import com.example.chapter12.synchronizationTools.SynchronizationTool;
import com.example.chapter12.synchronizationTools.ToolType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

@Configuration
public class InputView {
  @Bean
  public CommandLineRunner runner(OutputView outputView,  ApplicationContext context) {
    return args -> {
      Scanner sc = new Scanner(System.in);

      //동기화 방식 선택
      SynchronizationTool account = selectAccount(sc, outputView);

      outputView.showSuccess("선택된 동기화 도구 = " + account.name());
      outputView.showHelp();

      //명령 루프
      while (true) {
        System.out.print("> ");
        String line = sc.nextLine().trim();
        if (line.isEmpty()) continue;

        String[] parts = line.split(" ");
        String cmd = parts[0].toLowerCase();

        try {
          switch (cmd) {
            case "입금" -> {
              long amt = parseAmount(parts);
              account.deposit(amt);
              outputView.showSuccess("입금 " + amt);
              outputView.showBalance(account.getBalance());
            }
            case "출금" -> {
              long amt = parseAmount(parts);
              account.withdraw(amt);
              outputView.showSuccess("출금 " + amt);
              outputView.showBalance(account.getBalance());
            }
            case "잔액" -> outputView.showBalance(account.getBalance());
            case "도움말" -> outputView.showHelp();
            case "종료" -> {
              outputView.showSuccess("Bye!");
              int exitCode = SpringApplication.exit(context, () -> 0);
              System.exit(exitCode);
              return;
            }
            default -> outputView.showError("알 수 없는 명령어입니다.");
          }
        } catch (Exception e) {
          outputView.showError(e.getMessage());
        }
      }
    };
  }

  private SynchronizationTool selectAccount(Scanner sc, OutputView outputView) {
    while (true) {
      try {
        System.out.println("""
                        동기화 도구 선택:
                          - MUTEX
                          - SEMAPHORE
                          - MONITOR
                        """);
        System.out.print("sync> ");
        String input = sc.nextLine().trim();

        ToolType type = ToolType.from(input);
        return type.create();

      } catch (Exception e) {
        outputView.showError("부적절한 도구명입니다. 다시 시도해주세요.");
      }
    }
  }

  private long parseAmount(String[] parts) {
    if (parts.length < 2) throw new IllegalArgumentException("금액이 필요합니다.");
    long amt = Long.parseLong(parts[1]);
    if (amt <= 0) throw new IllegalArgumentException("입출금 금액은 양수여야합니다.");
    return amt;
  }
}
