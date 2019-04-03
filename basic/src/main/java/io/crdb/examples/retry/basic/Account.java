package io.crdb.examples.retry.basic;

import java.util.UUID;

public class Account {

    private UUID id;

    private int balance;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
