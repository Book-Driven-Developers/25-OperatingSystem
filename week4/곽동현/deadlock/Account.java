package operation_system.cpu_scheduling.thread.deadlock;

public class Account {
    private String id;
    private int money;

    public Account(String id, int money) {
        this.id = id;
        this.money = money;
    }

    public String getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }

    public void withdraw(int amount) {
        this.money += amount;
    }

    public void deposit(int amount) {
        this.money -= amount;
    }
}
