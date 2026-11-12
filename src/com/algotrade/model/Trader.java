
package com.algotrade.model;

import java.util.UUID;

public class Trader {

    private final String traderId;
    private final String name;
    private final String email;
    private final Portfolio portfolio;

    public Trader(String name, String email,
                  Portfolio portfolio) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Trader name is required"
            );
        }

        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(
                "Valid email is required"
            );
        }

        if (portfolio == null) {
            throw new IllegalArgumentException(
                "Portfolio is required"
            );
        }

        this.traderId = UUID.randomUUID().toString();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.portfolio = portfolio;
    }

    public String getTraderId() {
        return traderId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

public Trader(
        String traderId,
        String name,
        String email,
        Portfolio portfolio) {

	this.traderId = traderId;
    this.name = name;
    this.email = email;
    this.portfolio = portfolio;
}


    @Override
    public String toString() {
        return "Trader{" +
            "traderId='" + traderId + '\'' +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            '}';
    }
    
}
